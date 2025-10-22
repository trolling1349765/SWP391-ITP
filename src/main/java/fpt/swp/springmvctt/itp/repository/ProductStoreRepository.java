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
    
    // Count serials by batch (product_id + price)
    long countByProductIdAndFaceValue(Long productId, java.math.BigDecimal faceValue);
    
    // Get all batches for a product (group by price)
    @Query("SELECT ps.faceValue, COUNT(ps.id) FROM ProductStore ps WHERE ps.productId = :productId GROUP BY ps.faceValue ORDER BY ps.faceValue")
    List<Object[]> findBatchesByProductId(@Param("productId") Long productId);
}
