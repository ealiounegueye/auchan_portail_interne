package sn.auchan.portail.service;

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
import sn.auchan.portail.domain.Category;
import sn.auchan.portail.dto.CategoryRequest;
import sn.auchan.portail.dto.CategoryResponse;
import sn.auchan.portail.repository.BusinessAppRepository;
import sn.auchan.portail.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categories;
    private final BusinessAppRepository applications;

    public CategoryService(CategoryRepository categories, BusinessAppRepository applications) {
        this.categories = categories;
        this.applications = applications;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categories.findAllByOrderBySortOrderAsc().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        Category category = new Category();
        apply(category, request, null);
        if (request.sortOrder() == null) {
            category.setSortOrder(categories.findMaxSortOrder() + 1);
        }
        return CategoryResponse.from(categories.save(category));
    }

    @Transactional
    public List<CategoryResponse> reorder(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La liste des catégories est vide");
        }
        if (ids.size() != new HashSet<>(ids).size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ordre de catégories invalide");
        }
        List<Category> all = categories.findAllByOrderBySortOrderAsc();
        if (ids.size() != all.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L’ordre doit contenir toutes les catégories");
        }
        Set<Long> existing = all.stream().map(Category::getId).collect(Collectors.toSet());
        if (ids.stream().anyMatch(id -> !existing.contains(id))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ordre de catégories invalide");
        }
        Map<Long, Category> byId = all.stream().collect(Collectors.toMap(Category::getId, Function.identity()));
        for (int i = 0; i < ids.size(); i++) {
            byId.get(ids.get(i)).setSortOrder(i + 1);
        }
        categories.saveAll(byId.values());
        return ids.stream().map(id -> CategoryResponse.from(byId.get(id))).toList();
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categories.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catégorie introuvable"));
        apply(category, request, id);
        return CategoryResponse.from(categories.save(category));
    }

    @Transactional
    public void delete(Long id) {
        if (!categories.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Catégorie introuvable");
        }
        if (applications.countByCategory_Id(id) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Des applications utilisent encore cette catégorie"
            );
        }
        categories.deleteById(id);
    }

    private void apply(Category category, CategoryRequest request, Long currentId) {
        String name = request.name().trim();
        String slug = request.slug() == null || request.slug().isBlank()
                ? DepartmentService.slugify(name)
                : DepartmentService.slugify(request.slug());
        if (slug.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le slug de la catégorie est invalide");
        }
        boolean nameTaken = currentId == null
                ? categories.existsByNameIgnoreCase(name)
                : categories.existsByNameIgnoreCaseAndIdNot(name, currentId);
        boolean slugTaken = currentId == null
                ? categories.existsBySlugIgnoreCase(slug)
                : categories.existsBySlugIgnoreCaseAndIdNot(slug, currentId);
        if (nameTaken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une catégorie porte déjà ce nom");
        }
        if (slugTaken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une catégorie utilise déjà ce slug");
        }
        category.setName(name);
        category.setSlug(slug);
        category.setIcon(request.icon().trim());
        category.setColor(request.color().trim());
        if (request.sortOrder() != null) {
            category.setSortOrder(request.sortOrder());
        }
    }
}
