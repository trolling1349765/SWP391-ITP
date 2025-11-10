package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.ProductStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductStoreRepository extends JpaRepository<ProductStore, Long> {
    Optional<ProductStore> findByProductIdAndSerialCode(Long productId, String serialCode);
    List<ProductStore> findByProductIdOrderByIdDesc(Long productId);
    long countByProductId(Long productId); // Count serials for a product
    
    // Check if serial code exists
    boolean existsBySerialCode(String serialCode);
    
    // Count ACTIVE serials by batch (product_id + price)
    long countByProductIdAndFaceValueAndStatus(Long productId, java.math.BigDecimal faceValue, fpt.swp.springmvctt.itp.entity.enums.ProductStatus status);
    
    // Get all batches for a product (group by price) - only ACTIVE items
    @Query("SELECT ps.faceValue, COUNT(ps.id) FROM ProductStore ps WHERE ps.productId = :productId AND ps.status = 'ACTIVE' GROUP BY ps.faceValue ORDER BY ps.faceValue")
    List<Object[]> findBatchesByProductId(@Param("productId") Long productId);
    
    // Get available ACTIVE serials for a product (for purchase) - with lock for concurrency
    @Query("SELECT ps FROM ProductStore ps WHERE ps.productId = :productId AND ps.status = 'ACTIVE' ORDER BY ps.id ASC")
    List<ProductStore> findAvailableSerialsByProductId(@Param("productId") Long productId);
    
    // Count available ACTIVE serials
    long countByProductIdAndStatus(Long productId, fpt.swp.springmvctt.itp.entity.enums.ProductStatus status);
}
