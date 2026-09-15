package sn.auchan.portail.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.auchan.portail.domain.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    @Query("SELECT f FROM Favorite f JOIN FETCH f.application a JOIN FETCH a.category WHERE f.user.id = :userId")
    List<Favorite> findByUserId(@Param("userId") Long userId);

    Optional<Favorite> findByUserIdAndApplicationId(Long userId, Long applicationId);

    boolean existsByUserIdAndApplicationId(Long userId, Long applicationId);

    void deleteByUserIdAndApplicationId(Long userId, Long applicationId);

    void deleteByApplicationId(Long applicationId);
}
