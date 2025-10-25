package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {}
import java.time.LocalDate;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    @Query("""
    SELECT p FROM PaymentTransaction p
    JOIN p.user u
    WHERE (:username IS NULL OR u.username LIKE CONCAT('%', :username, '%'))
      AND (:type IS NULL OR p.type LIKE CONCAT('%', :type, '%'))
      AND (:startDate IS NULL OR p.createAt >= :startDate)
      AND (:endDate IS NULL OR p.createAt <= :endDate)
      AND (:deleted IS NULL OR p.isDeleted = :deleted)
      ORDER BY p.id DESC
""")
    Page<PaymentTransaction> findByFilter(
            @Param("type") String type,
            @Param("username") String username,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);


}
