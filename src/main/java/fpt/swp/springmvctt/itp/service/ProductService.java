package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.dto.request.ProductForm;
import fpt.swp.springmvctt.itp.entity.Product;
import org.springframework.data.domain.Page;
import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;

import java.util.List;

public interface ProductService {
    Product createProduct(Long shopId, ProductForm form); // default HIDDEN
    Product updateProduct(Long productId, ProductForm form);
    String saveImage(org.springframework.web.multipart.MultipartFile file); // Upload ảnh riêng
    Product changeStatus(Long productId, ProductStatus status);
    Product get(Long id);
    List<Product> listByShop(Long shopId);
    void delete(Long id);
    List<Product> getAllProducts();
    Product getProductById(Long id);

    // Featured
    List<Product> getFeaturedProducts(int limit);

    // CŨ (giữ lại): Phân trang + (optional) lọc theo categoryId, mặc định newest
    Page<Product> getProductsPage(int page, int size, Long categoryId);

    // MỚI (thêm): Phân trang + lọc categoryId + sort (newest | priceAsc | priceDesc)
    Page<Product> getProductsPage(int page, int size, Long categoryId, String sort);
}
