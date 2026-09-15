package sn.auchan.portail.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.auchan.portail.domain.BusinessApp;

public interface BusinessAppRepository extends JpaRepository<BusinessApp, Long> {

    List<BusinessApp> findAllByOrderByFeaturedDescSortOrderAscNameAsc();

    List<BusinessApp> findByCategoryIdOrderBySortOrderAscNameAsc(Long categoryId);

    @Query("""
            SELECT a FROM BusinessApp a
            JOIN FETCH a.category
            WHERE (:categoryId IS NULL OR a.category.id = :categoryId)
              AND (:q IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(a.description) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(a.ownerDepartment) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY a.sortOrder ASC, a.name ASC
            """)
    List<BusinessApp> search(@Param("q") String q, @Param("categoryId") Long categoryId);

    @Query("SELECT a FROM BusinessApp a JOIN FETCH a.category ORDER BY a.sortOrder ASC, a.name ASC")
    List<BusinessApp> findAllDetailed();

    @Query("select coalesce(max(a.sortOrder), 0) from BusinessApp a")
    int findMaxSortOrder();

    @Query("SELECT a FROM BusinessApp a JOIN FETCH a.category WHERE a.id = :id")
    Optional<BusinessApp> findDetailedById(@Param("id") Long id);

    long countByCategory_Id(Long categoryId);

    long countByOwnerDepartmentIgnoreCase(String ownerDepartment);
}
