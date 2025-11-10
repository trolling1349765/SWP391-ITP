package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    List<OrderItem> findByOrderId(Long orderId);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.orderId = :orderId ORDER BY oi.id ASC")
    List<OrderItem> findOrderItemsByOrderId(@Param("orderId") Long orderId);
    
    // Tìm OrderItem theo productStoreId để kiểm tra serial đã bán chưa
    @Query("SELECT oi FROM OrderItem oi WHERE oi.productStoreId = :productStoreId")
    List<OrderItem> findByProductStoreId(@Param("productStoreId") Long productStoreId);
    
    // Kiểm tra xem ProductStore có được bán (có OrderItem với order status = COMPLETED hoặc PENDING)
    @Query("""
        SELECT COUNT(oi) > 0 FROM OrderItem oi 
        WHERE oi.productStoreId = :productStoreId 
        AND (oi.order.status = 'COMPLETED' OR oi.order.status = 'PENDING')
        """)
    boolean isProductStoreSold(@Param("productStoreId") Long productStoreId);
}

