package sn.auchan.portail.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sn.auchan.portail.domain.AppAccess;
import sn.auchan.portail.domain.BusinessApp;
import sn.auchan.portail.domain.Role;
import sn.auchan.portail.domain.UserAccount;
import sn.auchan.portail.dto.AccessRequest;
import sn.auchan.portail.dto.AppAccessGrant;
import sn.auchan.portail.dto.UserRequest;
import sn.auchan.portail.dto.UserResponse;
import sn.auchan.portail.repository.AppAccessRepository;
import sn.auchan.portail.repository.BusinessAppRepository;
import sn.auchan.portail.repository.UserAccountRepository;

@Service
public class UserService {

    private final UserAccountRepository users;
    private final BusinessAppRepository applications;
    private final AppAccessRepository accesses;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserAccountRepository users,
            BusinessAppRepository applications,
            AppAccessRepository accesses,
            PasswordEncoder passwordEncoder
    ) {
        this.users = users;
        this.applications = applications;
        this.accesses = accesses;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll(String actorEmail) {
        UserAccount actor = requireUser(actorEmail);
        if (actor.getRole() == Role.ADMIN) {
            return users.findAllWithManager().stream().map(this::toResponse).toList();
        }
        return teamOf(actor).stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserResponse create(UserRequest request, String actorEmail) {
        requireAdmin(actorEmail);
        if (users.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email existe déjà");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le mot de passe est obligatoire");
        }
        UserAccount user = new UserAccount();
        applyIdentity(user, request, true);
        UserAccount saved = users.save(user);
        replaceAccess(saved, request.allowedApplicationIds(), request.applicationGrants());
        return toResponse(saved);
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request, String actorEmail) {
        requireAdmin(actorEmail);
        UserAccount user = users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        applyIdentity(user, request, false);
        UserAccount saved = users.save(user);
        replaceAccess(saved, request.allowedApplicationIds(), request.applicationGrants());
        return toResponse(saved);
    }

    @Transactional
    public UserResponse updateAccess(Long id, AccessRequest request, String actorEmail) {
        UserAccount actor = requireUser(actorEmail);
        UserAccount user = users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        assertCanManageAccess(actor, user);
        if (actor.getRole() == Role.MANAGER) {
            user.setManager(actor);
        }
        boolean restricted = Boolean.TRUE.equals(request.restrictedAccess());
        if (user.getRole() == Role.ADMIN) {
            restricted = false;
        }
        user.setRestrictedAccess(restricted);
        UserAccount saved = users.save(user);
        replaceAccess(saved, request.allowedApplicationIds(), request.applicationGrants());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse toResponse(UserAccount user) {
        List<AppAccessGrant> grants = user.isRestrictedAccess()
                ? accesses.findDetailedByUserId(user.getId()).stream().map(AppAccessGrant::from).toList()
                : List.of();
        List<Long> allowed = grants.stream().map(AppAccessGrant::applicationId).toList();
        return UserResponse.from(user, allowed, grants);
    }

    private void applyIdentity(UserAccount user, UserRequest request, boolean creating) {
        Role requestedRole = request.role() == null ? Role.USER : request.role();
        user.setRole(requestedRole);
        if (request.managerId() == null) {
            user.setManager(null);
        } else {
            if (user.getId() != null && request.managerId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un utilisateur ne peut pas être son propre responsable");
            }
            UserAccount manager = users.findById(request.managerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Responsable introuvable"));
            if (manager.getRole() != Role.MANAGER && manager.getRole() != Role.ADMIN) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le responsable doit avoir le rôle Responsable ou Administrateur");
            }
            user.setManager(manager);
        }

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setDepartment(request.department());
        user.setActive(request.active() == null || request.active());

        boolean restricted = Boolean.TRUE.equals(request.restrictedAccess());
        if (requestedRole == Role.ADMIN) {
            restricted = false;
        }
        user.setRestrictedAccess(restricted);

        if (creating || (request.password() != null && !request.password().isBlank())) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
    }

    private void replaceAccess(UserAccount user, List<Long> allowedApplicationIds, List<AppAccessGrant> grants) {
        accesses.deleteByUserId(user.getId());
        if (!user.isRestrictedAccess()) {
            return;
        }
        Map<Long, AppAccessGrant> byId = new LinkedHashMap<>();
        if (grants != null) {
            for (AppAccessGrant grant : grants) {
                if (grant != null && grant.applicationId() != null) {
                    byId.put(grant.applicationId(), grant);
                }
            }
        }
        if (allowedApplicationIds != null) {
            for (Long appId : allowedApplicationIds) {
                byId.putIfAbsent(appId, new AppAccessGrant(appId, true, false, true));
            }
        }
        if (byId.isEmpty()) {
            return;
        }
        for (AppAccessGrant grant : byId.values()) {
            BusinessApp app = applications.findById(grant.applicationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application introuvable"));
            AppAccess access = new AppAccess();
            access.setUser(user);
            access.setApplication(app);
            access.setCanViewDocumentation(grant.documentation());
            access.setCanViewTechnicalSheet(grant.technicalSheet());
            access.setCanViewUserGuide(grant.userGuide());
            accesses.save(access);
        }
    }

    private List<UserAccount> teamOf(UserAccount manager) {
        Map<Long, UserAccount> team = new LinkedHashMap<>();
        for (UserAccount user : users.findByDepartmentIgnoreCase(manager.getDepartment())) {
            if (user.getRole() == Role.USER && !user.getId().equals(manager.getId())) {
                team.put(user.getId(), user);
            }
        }
        for (UserAccount user : users.findByManager_Id(manager.getId())) {
            if (user.getRole() == Role.USER) {
                team.put(user.getId(), user);
            }
        }
        return new ArrayList<>(team.values());
    }

    private void assertCanManageAccess(UserAccount actor, UserAccount target) {
        if (actor.getRole() == Role.ADMIN) {
            return;
        }
        if (actor.getRole() == Role.MANAGER && target.getRole() == Role.USER) {
            boolean sameDepartment = actor.getDepartment().equalsIgnoreCase(target.getDepartment());
            boolean reportsToActor = target.getManager() != null && actor.getId().equals(target.getManager().getId());
            if (sameDepartment || reportsToActor) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez gérer que les accès des collaborateurs de votre équipe");
    }

    private UserAccount requireAdmin(String email) {
        UserAccount actor = requireUser(email);
        if (actor.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Les comptes sont gérés par Bird. Ici, seuls les accès aux applications se paramètrent.");
        }
        return actor;
    }

    private UserAccount requireUser(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }
}
