package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByShopIdOrderByIdDesc(Long shopId);
    List<Product> findByShopIdAndStatus(Long shopId, ProductStatus status);
    
    // Phân trang sản phẩm theo shop và status
    Page<Product> findByShopIdAndStatus(Long shopId, ProductStatus status, Pageable pageable);

    // Fix method name to use categoryId instead of category
    Page<Product> findByStatusAndCategoryIdOrderByIdDesc(String status, Long categoryId, Pageable pageable);
    
    // Add missing methods used by ProductServiceImpl
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    Page<Product> findByStatusAndCategoryId(ProductStatus status, Long categoryId, Pageable pageable);

    // Eager load shop và category khi lấy product detail
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.shop LEFT JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithShop(@Param("id") Long id);
}
