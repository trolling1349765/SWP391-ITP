package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByShopIdOrderByIdDesc(Long shopId);
    List<Product> findByShopIdAndStatus(Long shopId, ProductStatus status);
    
    // Fix method name to use categoryId instead of category
    Page<Product> findByStatusAndCategoryIdOrderByIdDesc(String status, Long categoryId, Pageable pageable);
    
    // Add missing methods used by ProductServiceImpl
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    Page<Product> findByStatusAndCategoryId(ProductStatus status, Long categoryId, Pageable pageable);
}
