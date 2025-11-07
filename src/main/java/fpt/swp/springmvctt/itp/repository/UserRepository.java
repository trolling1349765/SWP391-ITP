package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByShopId(Long shopId);
    boolean existsByShopId(Long shopId);
    User findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query("""
        SELECT DISTINCT u
    FROM User u
    LEFT JOIN FETCH u.role
    WHERE (:username IS NULL OR u.username LIKE CONCAT('%', :username, '%'))
      AND (:status IS NULL OR u.status = :status)
      AND (:startDate IS NULL OR u.createAt >= :startDate)
      AND (:endDate IS NULL OR u.createAt <= :endDate)
      AND (:fromUpdateDate IS NULL OR u.updateAt >= :fromUpdateDate)
      AND (:toUpdateDate IS NULL OR u.updateAt <= :toUpdateDate)
      AND (:isDelete IS NULL OR u.isDeleted = :isDelete)
      AND (:deleteBy IS NULL OR u.deleteBy LIKE CONCAT('%', :deleteBy,'%'))
      AND (:email IS NULL OR u.email LIKE CONCAT('%', :email, '%'))
      AND (:role IS NULL OR u.role.name = :role)
      ORDER BY u.id DESC
    """)
    Page<User> findByFilter(@Param("username") String username,
                            @Param("email") String email,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate,
                            @Param("fromUpdateDate") LocalDate fromUpdateDate,
                            @Param("toUpdateDate") LocalDate toUpdateDate,
                            @Param("isDelete") Boolean isDelete,
                            @Param("deleteBy") String deteleBy,
                            @Param("status") String status,
                            @Param("role") String role,
                            Pageable pageable);
}
