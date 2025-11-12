package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    Page<Shop> findByStatus(String status, Pageable pageable);

    @Query("""
        SELECT s FROM Shop s
           left JOIN s.user u
            WHERE s.status = :status
              AND (:shopName IS NULL OR s.shopName LIKE %:shopName%)
              AND (:username IS NULL OR u.username LIKE %:username%)
              AND (:fromDate IS NULL OR s.createAt >= :fromDate)
              AND (:toDate IS NULL OR s.createAt <= :toDate)
       """)
    Page<Shop> filterShops(@Param("status") String status,
                           @Param("shopName") String shopName,
                           @Param("username") String username,
                           @Param("fromDate") LocalDate fromDate,
                           @Param("toDate") LocalDate toDate,
                           Pageable pageable);

    @Query("""
        SELECT u
    FROM Shop u
    WHERE (:shopName IS NULL OR u.shopName LIKE CONCAT('%', :shopName, '%'))
      AND (:createBy IS NULL OR u.createBy LIKE CONCAT('%', :createBy, '%'))
      AND (:startDate IS NULL OR u.createAt >= :startDate)
      AND (:endDate IS NULL OR u.createAt <= :endDate)
      AND (:fromUpdateDate IS NULL OR u.updateAt >= :fromUpdateDate)
      AND (:toUpdateDate IS NULL OR u.updateAt <= :toUpdateDate)
      AND (:isDelete IS NULL OR u.isDeleted = :isDelete)
      AND (:deleteBy IS NULL OR u.deleteBy LIKE CONCAT('%', :deleteBy, '%'))
      AND (:status IS NULL OR u.status = :status)
      ORDER BY u.id DESC
    """)
    Page<Shop> findByFilter(@Param("shopName") String shopName,
                            @Param("createBy") String createBy,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate,
                            @Param("fromUpdateDate") LocalDate fromUpdateDate,
                            @Param("toUpdateDate") LocalDate toUpdateDate,
                            @Param("isDelete") Boolean isDelete,
                            @Param("deleteBy") String deteleBy,
                            @Param("status") String status,
                            Pageable pageable);

    List<Shop> findTop10ByOrderByIdDesc();
}
