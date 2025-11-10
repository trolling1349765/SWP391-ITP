package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // ========== FEATURE_V5: Order Management Methods ==========
    List<Order> findByUserIdOrderByCreateAtDesc(Long userId);
    
    Optional<Order> findByOrderCode(String orderCode);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.product p LEFT JOIN FETCH p.shop WHERE o.user.id = :userId ORDER BY o.createAt DESC")
    List<Order> findOrdersByUserId(@Param("userId") Long userId);
    
    // ========== MAIN: Revenue Analytics (Team Feature) ==========
    // Tính doanh thu theo tháng trong 1 năm
    @Query("""
           select month(o.createAt) as m, coalesce(sum(o.totalAmount), 0)
           from Order o
           where year(o.createAt) = :year
           group by month(o.createAt)
           """)
    List<Object[]> sumRevenueByMonth(@Param("year") int year);
}
