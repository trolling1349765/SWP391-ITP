package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ShopRepository extends JpaRepository<Shop, Long> {
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

}
