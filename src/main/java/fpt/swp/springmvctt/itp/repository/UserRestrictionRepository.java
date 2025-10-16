package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.UserRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface UserRestrictionRepository extends JpaRepository<UserRestriction, Long> {

    @Query(value = """
    SELECT * FROM user_restrictions
    WHERE (:status IS NULL OR status = :status)
    AND (:startDate IS NULL OR create_at >= :startDate)
    AND (:endDate IS NULL OR create_at <= :endDate)
""", nativeQuery = true)
    List<UserRestriction> findByFilter(
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query(value = """
    SELECT * FROM user_restrictions ur
    JOIN users u
    WHERE (:username IS NULL OR u.username LIKE %:username%)
    AND (:status IS NULL OR ur.status = :status)
    AND (:startDate IS NULL OR ur.create_at >= :startDate)
    AND (:endDate IS NULL OR ur.create_at <= :endDate)
""", nativeQuery = true)
    List<UserRestriction> findByFilter(
            @Param("username") String username,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

}
