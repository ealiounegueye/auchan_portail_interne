package sn.auchan.portail.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sn.auchan.portail.domain.Department;
import sn.auchan.portail.dto.DepartmentRequest;
import sn.auchan.portail.dto.DepartmentResponse;
import sn.auchan.portail.repository.BusinessAppRepository;
import sn.auchan.portail.repository.DepartmentRepository;
import sn.auchan.portail.repository.UserAccountRepository;

@Service
public class DepartmentService {

    private final DepartmentRepository departments;
    private final BusinessAppRepository applications;
    private final UserAccountRepository users;

    public DepartmentService(
            DepartmentRepository departments,
            BusinessAppRepository applications,
            UserAccountRepository users
    ) {
        this.departments = departments;
        this.applications = applications;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departments.findAllByOrderBySortOrderAscNameAsc().stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        Department department = new Department();
        apply(department, request, null);
        return DepartmentResponse.from(departments.save(department));
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = departments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Direction introuvable"));
        apply(department, request, id);
        return DepartmentResponse.from(departments.save(department));
    }

    @Transactional
    public void delete(Long id) {
        Department department = departments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Direction introuvable"));
        long usedByApps = applications.countByOwnerDepartmentIgnoreCase(department.getName());
        long usedByUsers = users.countByDepartmentIgnoreCase(department.getName());
        if (usedByApps > 0 || usedByUsers > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cette direction est encore utilisée par des applications ou des collaborateurs"
            );
        }
        departments.delete(department);
    }

    private void apply(Department department, DepartmentRequest request, Long currentId) {
        String name = request.name().trim();
        String code = request.code() == null || request.code().isBlank()
                ? slugify(name).toUpperCase(Locale.ROOT)
                : slugify(request.code()).toUpperCase(Locale.ROOT);
        if (code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le code de la direction est invalide");
        }
        boolean nameTaken = currentId == null
                ? departments.existsByNameIgnoreCase(name)
                : departments.existsByNameIgnoreCaseAndIdNot(name, currentId);
        boolean codeTaken = currentId == null
                ? departments.existsByCodeIgnoreCase(code)
                : departments.existsByCodeIgnoreCaseAndIdNot(code, currentId);
        if (nameTaken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une direction porte déjà ce nom");
        }
        if (codeTaken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une direction utilise déjà ce code");
        }
        department.setName(name);
        department.setCode(code);
        department.setDescription(blankToNull(request.description()));
        department.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    static String slugify(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return normalized;
    }
}
