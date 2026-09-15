package sn.auchan.portail.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.auchan.portail.domain.AppAccess;

public interface AppAccessRepository extends JpaRepository<AppAccess, Long> {

    @Query("SELECT a.application.id FROM AppAccess a WHERE a.user.id = :userId")
    Set<Long> findApplicationIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM AppAccess a JOIN FETCH a.application WHERE a.user.id = :userId")
    List<AppAccess> findDetailedByUserId(@Param("userId") Long userId);

    Optional<AppAccess> findByUser_IdAndApplication_Id(Long userId, Long applicationId);

    void deleteByUserId(Long userId);

    void deleteByApplicationId(Long applicationId);
}
