package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.entity.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();
    Product getProductById(Long id);

    // Featured
    List<Product> getFeaturedProducts(int limit);

    // CŨ (giữ lại): Phân trang + (optional) lọc theo categoryId, mặc định newest
    Page<Product> getProductsPage(int page, int size, Long categoryId);

    // MỚI (thêm): Phân trang + lọc categoryId + sort (newest | priceAsc | priceDesc)
    Page<Product> getProductsPage(int page, int size, Long categoryId, String sort);
}
