package sn.auchan.portail.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.auchan.portail.domain.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    @Query("SELECT u FROM UserAccount u LEFT JOIN FETCH u.manager WHERE LOWER(u.email) = LOWER(:email)")
    Optional<UserAccount> findByEmailIgnoreCase(@Param("email") String email);

    boolean existsByEmailIgnoreCase(String email);

    List<UserAccount> findByManager_Id(Long managerId);

    List<UserAccount> findByDepartmentIgnoreCase(String department);

    long countByDepartmentIgnoreCase(String department);

    @Query("SELECT DISTINCT u FROM UserAccount u LEFT JOIN FETCH u.manager")
    List<UserAccount> findAllWithManager();
}
