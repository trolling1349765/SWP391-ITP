package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.UserRestriction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface UserRestrictionRepository extends JpaRepository<UserRestriction, Long> {


    @Query("""
    SELECT ur
    FROM UserRestriction ur
    JOIN ur.user u
    WHERE (:username IS NULL OR u.username LIKE CONCAT('%', :username, '%'))
      AND (:status IS NULL OR ur.status = :status)
      AND (:startDate IS NULL OR ur.createAt >= :startDate)
      AND (:endDate IS NULL OR ur.createAt <= :endDate)
""")
    Page<UserRestriction> findByFilter(
            @Param("username") String username,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
            );

    @Query("""
    SELECT ur
    FROM UserRestriction ur
    JOIN ur.user u
    WHERE (:username IS NULL OR u.username LIKE CONCAT('%', :username, '%'))
      AND (:status IS NULL OR ur.status = :status)
      AND (:startDate IS NULL OR ur.createAt >= :startDate)
      AND (:endDate IS NULL OR ur.createAt <= :endDate)
      AND (:deleted IS NULL OR ur.isDeleted = :deleted)
      ORDER BY ur.id DESC
""")
    Page<UserRestriction> findByFilter(
            @Param("username") String username,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("deleted") Boolean deleted,
            Pageable pageable
    );


}
