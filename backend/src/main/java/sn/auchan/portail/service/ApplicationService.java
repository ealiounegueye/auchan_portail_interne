package sn.auchan.portail.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sn.auchan.portail.domain.AppAccess;
import sn.auchan.portail.domain.BusinessApp;
import sn.auchan.portail.domain.Category;
import sn.auchan.portail.domain.Favorite;
import sn.auchan.portail.domain.Role;
import sn.auchan.portail.domain.UserAccount;
import sn.auchan.portail.dto.ApplicationRequest;
import sn.auchan.portail.dto.ApplicationResponse;
import sn.auchan.portail.dto.GeneratedDocument;
import sn.auchan.portail.dto.StoredDocumentFile;
import sn.auchan.portail.repository.AppAccessRepository;
import sn.auchan.portail.repository.BusinessAppRepository;
import sn.auchan.portail.repository.CategoryRepository;
import sn.auchan.portail.repository.FavoriteRepository;
import sn.auchan.portail.repository.UserAccountRepository;

@Service
public class ApplicationService {

    private final BusinessAppRepository applications;
    private final CategoryRepository categories;
    private final FavoriteRepository favorites;
    private final UserAccountRepository users;
    private final AppAccessRepository accesses;
    private final DocumentService documents;
    private final DocumentFileStorageService documentFiles;

    public ApplicationService(
            BusinessAppRepository applications,
            CategoryRepository categories,
            FavoriteRepository favorites,
            UserAccountRepository users,
            AppAccessRepository accesses,
            DocumentService documents,
            DocumentFileStorageService documentFiles
    ) {
        this.applications = applications;
        this.categories = categories;
        this.favorites = favorites;
        this.users = users;
        this.accesses = accesses;
        this.documents = documents;
        this.documentFiles = documentFiles;
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findAll() {
        return applications.findAllDetailed().stream()
                .map(app -> ApplicationResponse.from(app, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> search(String query, Long categoryId, String email) {
        String q = (query == null || query.isBlank()) ? null : query.trim();
        UserAccount user = requireUser(email);
        Set<Long> favoriteIds = favoriteIds(user);
        Set<Long> allowedIds = allowedApplicationIds(user);
        Map<Long, AppAccess> grants = documentGrants(user);
        return applications.search(q, categoryId).stream()
                .filter(app -> allowedIds == null || allowedIds.contains(app.getId()))
                .map(app -> toResponse(app, favoriteIds.contains(app.getId()), user, grants))
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse get(Long id, String email) {
        UserAccount user = requireUser(email);
        BusinessApp app = requireVisibleApp(id, user);
        boolean favorite = favorites.existsByUserIdAndApplicationId(user.getId(), id);
        return toResponse(app, favorite, user, documentGrants(user));
    }

    @Transactional(readOnly = true)
    public GeneratedDocument exportDocument(Long id, String kind, String format, String email) {
        UserAccount user = requireUser(email);
        BusinessApp app = requireVisibleApp(id, user);
        if (!canViewDocument(user, id, kind)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à ce document");
        }
        GeneratedDocument uploaded = uploadedAsGenerated(app, kind);
        if (uploaded != null) {
            return uploaded;
        }
        return documents.generate(app, kind, format);
    }

    @Transactional(readOnly = true)
    public StoredDocumentFile downloadUploadedFile(Long id, String kind, String email) {
        UserAccount user = requireUser(email);
        BusinessApp app = requireVisibleApp(id, user);
        if (!canViewDocument(user, id, kind)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à ce document");
        }
        StoredDocumentFile file = loadUploaded(app, kind);
        if (file == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucun fichier n’a été importé pour ce document");
        }
        return file;
    }

    private GeneratedDocument uploadedAsGenerated(BusinessApp app, String kind) {
        StoredDocumentFile file = loadUploaded(app, kind);
        if (file == null) {
            return null;
        }
        try {
            return new GeneratedDocument(
                    file.resource().getContentAsByteArray(),
                    file.contentType(),
                    file.originalName()
            );
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Impossible de lire le fichier importé");
        }
    }

    private StoredDocumentFile loadUploaded(BusinessApp app, String kind) {
        String storedFile = storedFileOf(app, kind);
        if (storedFile == null || storedFile.isBlank()) {
            return null;
        }
        String originalName = originalNameOf(app, kind);
        String contentType = contentTypeOf(app, kind);
        return new StoredDocumentFile(
                documentFiles.load(storedFile),
                originalName == null || originalName.isBlank() ? storedFile : originalName,
                documentFiles.contentType(storedFile, contentType)
        );
    }

    @Transactional
    public ApplicationResponse create(ApplicationRequest request) {
        BusinessApp app = new BusinessApp();
        apply(app, request);
        if (request.sortOrder() == null) {
            app.setSortOrder(applications.findMaxSortOrder() + 1);
        }
        return ApplicationResponse.from(applications.save(app), false);
    }

    @Transactional
    public List<ApplicationResponse> reorder(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La liste des applications est vide");
        }
        if (ids.size() != new HashSet<>(ids).size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ordre d’applications invalide");
        }
        List<BusinessApp> all = applications.findAllDetailed();
        if (ids.size() != all.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L’ordre doit contenir toutes les applications");
        }
        Set<Long> existing = all.stream().map(BusinessApp::getId).collect(Collectors.toSet());
        if (ids.stream().anyMatch(id -> !existing.contains(id))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ordre d’applications invalide");
        }
        Map<Long, BusinessApp> byId = all.stream().collect(Collectors.toMap(BusinessApp::getId, Function.identity()));
        for (int i = 0; i < ids.size(); i++) {
            byId.get(ids.get(i)).setSortOrder(i + 1);
        }
        applications.saveAll(byId.values());
        return ids.stream().map(id -> ApplicationResponse.from(byId.get(id), false)).toList();
    }

    @Transactional
    public ApplicationResponse update(Long id, ApplicationRequest request) {
        BusinessApp app = applications.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application introuvable"));
        apply(app, request);
        return ApplicationResponse.from(applications.save(app), false);
    }

    @Transactional
    public void delete(Long id) {
        BusinessApp app = applications.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application introuvable"));
        documentFiles.delete(app.getDocumentationStoredFile());
        documentFiles.delete(app.getTechnicalSheetStoredFile());
        documentFiles.delete(app.getUserGuideStoredFile());
        favorites.deleteByApplicationId(id);
        accesses.deleteByApplicationId(id);
        applications.deleteById(id);
    }

    @Transactional
    public void toggleFavorite(Long applicationId, String email) {
        UserAccount user = requireUser(email);
        BusinessApp app = requireVisibleApp(applicationId, user);
        favorites.findByUserIdAndApplicationId(user.getId(), applicationId)
                .ifPresentOrElse(favorites::delete, () -> {
                    Favorite favorite = new Favorite();
                    favorite.setUser(user);
                    favorite.setApplication(app);
                    favorites.save(favorite);
                });
    }

    private void apply(BusinessApp app, ApplicationRequest request) {
        Category category = categories.findById(request.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Catégorie introuvable"));
        app.setName(request.name());
        app.setDescription(request.description());
        app.setUrl(request.url());
        app.setIcon(request.icon() == null || request.icon().isBlank() ? "monitor" : request.icon().trim());
        app.setLogoUrl(blankToNull(request.logoUrl()));
        app.setCategory(category);
        app.setOwnerDepartment(request.ownerDepartment());
        app.setStatus(request.status() == null ? "ACTIVE" : request.status());
        app.setFeatured(Boolean.TRUE.equals(request.featured()));
        if (request.sortOrder() != null) {
            app.setSortOrder(request.sortOrder());
        }
        app.setLongDescription(blankToNull(request.longDescription()));
        app.setModop(blankToNull(request.modop()));
        app.setTechnicalSheetContent(blankToNull(request.technicalSheetContent()));
        app.setUserGuideContent(blankToNull(request.userGuideContent()));
        documentFiles.replace(app.getDocumentationStoredFile(), blankToNull(request.documentationStoredFile()));
        app.setDocumentationUrl(blankToNull(request.documentationUrl()));
        app.setDocumentationStoredFile(blankToNull(request.documentationStoredFile()));
        app.setDocumentationFileName(blankToNull(request.documentationFileName()));
        app.setDocumentationContentType(blankToNull(request.documentationContentType()));
        documentFiles.replace(app.getTechnicalSheetStoredFile(), blankToNull(request.technicalSheetStoredFile()));
        app.setTechnicalSheetUrl(blankToNull(request.technicalSheetUrl()));
        app.setTechnicalSheetStoredFile(blankToNull(request.technicalSheetStoredFile()));
        app.setTechnicalSheetFileName(blankToNull(request.technicalSheetFileName()));
        app.setTechnicalSheetContentType(blankToNull(request.technicalSheetContentType()));
        documentFiles.replace(app.getUserGuideStoredFile(), blankToNull(request.userGuideStoredFile()));
        app.setUserGuideUrl(blankToNull(request.userGuideUrl()));
        app.setUserGuideStoredFile(blankToNull(request.userGuideStoredFile()));
        app.setUserGuideFileName(blankToNull(request.userGuideFileName()));
        app.setUserGuideContentType(blankToNull(request.userGuideContentType()));
        app.setSupportUrl(blankToNull(request.supportUrl()));
        app.setSupportContact(blankToNull(request.supportContact()));
        app.setVersion(blankToNull(request.version()));
        app.setAudience(blankToNull(request.audience()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Set<Long> favoriteIds(UserAccount user) {
        return favorites.findByUserId(user.getId()).stream()
                .map(fav -> fav.getApplication().getId())
                .collect(Collectors.toSet());
    }

    /**
     * {@code null} means unrestricted (full catalogue). A set means whitelist.
     */
    private Set<Long> allowedApplicationIds(UserAccount user) {
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER || !user.isRestrictedAccess()) {
            return null;
        }
        return accesses.findApplicationIdsByUserId(user.getId());
    }

    private BusinessApp requireVisibleApp(Long id, UserAccount user) {
        BusinessApp app = applications.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application introuvable"));
        Set<Long> allowedIds = allowedApplicationIds(user);
        if (allowedIds != null && !allowedIds.contains(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à cette application");
        }
        return app;
    }

    private UserAccount requireUser(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    private ApplicationResponse toResponse(BusinessApp app, boolean favorite, UserAccount user, Map<Long, AppAccess> grants) {
        return ApplicationResponse.from(
                app,
                favorite,
                canViewDocumentation(user, app.getId(), grants),
                canViewTechnicalSheet(user, app.getId(), grants),
                canViewUserGuide(user, app.getId(), grants)
        );
    }

    private boolean canViewDocumentation(UserAccount user, Long applicationId, Map<Long, AppAccess> grants) {
        if (unrestrictedDocuments(user)) {
            return true;
        }
        AppAccess access = grants.get(applicationId);
        return access != null && access.isCanViewDocumentation();
    }

    private boolean canViewTechnicalSheet(UserAccount user, Long applicationId, Map<Long, AppAccess> grants) {
        if (unrestrictedDocuments(user)) {
            return true;
        }
        AppAccess access = grants.get(applicationId);
        return access != null && access.isCanViewTechnicalSheet();
    }

    private boolean canViewUserGuide(UserAccount user, Long applicationId, Map<Long, AppAccess> grants) {
        if (unrestrictedDocuments(user)) {
            return true;
        }
        AppAccess access = grants.get(applicationId);
        return access != null && access.isCanViewUserGuide();
    }

    private boolean canViewDocument(UserAccount user, Long applicationId, String kind) {
        if (kind == null) {
            return false;
        }
        Map<Long, AppAccess> grants = documentGrants(user);
        return switch (kind.toLowerCase()) {
            case "documentation", "doc", "modop", "mode-operatoire" -> canViewDocumentation(user, applicationId, grants);
            case "fiche-technique", "fiche", "technical" -> canViewTechnicalSheet(user, applicationId, grants);
            case "guide", "guide-utilisateur", "user-guide" -> canViewUserGuide(user, applicationId, grants);
            default -> false;
        };
    }

    private boolean unrestrictedDocuments(UserAccount user) {
        return user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER || !user.isRestrictedAccess();
    }

    private String storedFileOf(BusinessApp app, String kind) {
        return switch (normalizeKind(kind)) {
            case "documentation" -> app.getDocumentationStoredFile();
            case "fiche-technique" -> app.getTechnicalSheetStoredFile();
            case "guide" -> app.getUserGuideStoredFile();
            default -> null;
        };
    }

    private String originalNameOf(BusinessApp app, String kind) {
        return switch (normalizeKind(kind)) {
            case "documentation" -> app.getDocumentationFileName();
            case "fiche-technique" -> app.getTechnicalSheetFileName();
            case "guide" -> app.getUserGuideFileName();
            default -> null;
        };
    }

    private String contentTypeOf(BusinessApp app, String kind) {
        return switch (normalizeKind(kind)) {
            case "documentation" -> app.getDocumentationContentType();
            case "fiche-technique" -> app.getTechnicalSheetContentType();
            case "guide" -> app.getUserGuideContentType();
            default -> null;
        };
    }

    private String normalizeKind(String kind) {
        if (kind == null) {
            return "";
        }
        return switch (kind.toLowerCase()) {
            case "documentation", "doc", "modop", "mode-operatoire" -> "documentation";
            case "fiche-technique", "fiche", "technical" -> "fiche-technique";
            case "guide", "guide-utilisateur", "user-guide" -> "guide";
            default -> kind.toLowerCase();
        };
    }

    private Map<Long, AppAccess> documentGrants(UserAccount user) {
        if (unrestrictedDocuments(user)) {
            return Map.of();
        }
        return accesses.findDetailedByUserId(user.getId()).stream()
                .collect(Collectors.toMap(access -> access.getApplication().getId(), access -> access, (left, right) -> left));
    }
}
