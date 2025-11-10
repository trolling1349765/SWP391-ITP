package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.dto.request.ProductForm;
import fpt.swp.springmvctt.itp.dto.request.ExcelImportForm;
import fpt.swp.springmvctt.itp.dto.response.ImportResult;
import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.ProductStore;
import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;
import fpt.swp.springmvctt.itp.entity.enums.ProductType;
import fpt.swp.springmvctt.itp.repository.ProductRepository;
import fpt.swp.springmvctt.itp.repository.ProductStoreRepository;
import fpt.swp.springmvctt.itp.service.ProductService;
import fpt.swp.springmvctt.itp.service.StorageService;
import fpt.swp.springmvctt.itp.service.InventoryService;
import fpt.swp.springmvctt.itp.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductStoreRepository productStoreRepository;
    private final StorageService storageService;
    private final InventoryService inventoryService;
    private final ExcelImportService excelImportService;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product createProduct(Long shopId, ProductForm form) {
        Product p = new Product();
        p.setShopId(shopId);
        p.setProductName(form.getProductName());
        p.setDescription(form.getDescription());
        p.setDetailedDescription(form.getDetailedDescription());
        p.setPrice(form.getPrice() == null ? BigDecimal.ZERO : form.getPrice());
        p.setCategoryId(form.getCategoryId());
        p.setProductType(form.getProductType() == null ? ProductType.OTHER : form.getProductType());
        p.setStatus(ProductStatus.HIDDEN);
        p.setAvailableStock(0);

        if (form.getFile() != null && !form.getFile().isEmpty()) {
            String imagePath = storageService.saveProductImage(form.getFile());
            p.setImage(imagePath);
            System.out.println("Created product with image: " + imagePath);
        } else if (form.getImg() != null && !form.getImg().isBlank()) {
            p.setImage(form.getImg());
            System.out.println("Created product with existing image: " + form.getImg());
        }

        // Save product first to get ID
        Product savedProduct = productRepository.save(p);
        System.out.println(" Created product ID: " + savedProduct.getId() + " - Name: " + savedProduct.getProductName());
        System.out.println(" Detailed Description: " + savedProduct.getDetailedDescription());

        // Excel import will be handled in updateProduct, not during creation
        System.out.println(" Product created successfully. Serial codes can be added later via updateProduct.");

        return savedProduct;
    }

    @Override
    public Product updateProduct(Long productId, ProductForm form) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        if (form.getProductName() != null) p.setProductName(form.getProductName());
        if (form.getDescription() != null) p.setDescription(form.getDescription());
        if (form.getDetailedDescription() != null) p.setDetailedDescription(form.getDetailedDescription());
        if (form.getPrice() != null) p.setPrice(form.getPrice());
        if (form.getCategoryId() != null) p.setCategoryId(form.getCategoryId());
        if (form.getProductType() != null) p.setProductType(form.getProductType());

        if (form.getFile() != null && !form.getFile().isEmpty()) {
            String imagePath = storageService.saveProductImage(form.getFile());
            p.setImage(imagePath);
            System.out.println("Updated product with new image: " + imagePath);
        } else if (form.getImg() != null && !form.getImg().isBlank()) {
            p.setImage(form.getImg());
            System.out.println("Updated product keeping existing image: " + form.getImg());
        }
        return productRepository.save(p);
    }

    @Override
    public Product getProductById(Long id) {
        // Sử dụng findByIdWithShop để eager load shop information
        return productRepository.findByIdWithShop(id).orElse(null);
    }

    @Override
    public Product changeStatus(Long productId, ProductStatus status) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        // Update product status
        p.setStatus(status);
        Product savedProduct = productRepository.save(p);

        // CASCADE: Update all serials to match product status
        List<ProductStore> serials =
            productStoreRepository.findByProductIdOrderByIdDesc(productId);

        for (ProductStore serial : serials) {
            serial.setStatus(status);
        }

        if (!serials.isEmpty()) {
            productStoreRepository.saveAll(serials);
            System.out.println("Updated " + serials.size() + " serials to status: " + status);
        }

        return savedProduct;
    }

    @Override @Transactional(readOnly = true)
    public Product get(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    @Override @Transactional(readOnly = true)
    public List<Product> listByShop(Long shopId) {
        return productRepository.findByShopIdOrderByIdDesc(shopId);
    }

    public List<Product> getFeaturedProducts(int limit) {
        Pageable topN = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
        return productRepository.findByStatus(ProductStatus.ACTIVE, topN).getContent();
    }

    // "Xóa" sản phẩm = Ẩn sản phẩm (status = HIDDEN) để customer không thấy
    // Shop vẫn thấy để có thể bật lại bán tiếp
    @Override
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        // Chỉ cần đổi status thành HIDDEN để ẩn với customer
        product.setStatus(ProductStatus.HIDDEN);
        productRepository.save(product);
        System.out.println("Product ID: " + id + " đã được ẩn (status = HIDDEN)");
    }

    @Override
    public Page<Product> getProductsPage(int page, int size, Long categoryId) {
        return getProductsPage(page, size, categoryId, "newest");
    }

    // MỚI: hỗ trợ sort linh hoạt
    @Override
    public String saveImage(org.springframework.web.multipart.MultipartFile file) {
        return storageService.saveProductImage(file);
    }

    @Override
    public Page<Product> getProductsPage(int page, int size, Long categoryId, String sort) {
        int pageIndex = Math.max(page - 1, 0);

        // Map sort string -> Sort
        Sort sortSpec;
        if ("priceAsc".equalsIgnoreCase(sort)) {
            sortSpec = Sort.by(Sort.Direction.ASC, "price");
        } else if ("priceDesc".equalsIgnoreCase(sort)) {
            sortSpec = Sort.by(Sort.Direction.DESC, "price");
        } else {
            // newest (mặc định)
            sortSpec = Sort.by(Sort.Direction.DESC, "id");
        }

        Pageable pageable = PageRequest.of(pageIndex, size, sortSpec);

        if (categoryId == null) {
            // Dùng method mới để Sort qua Pageable
            return productRepository.findByStatus(ProductStatus.ACTIVE, pageable);
        }
        return productRepository.findByStatusAndCategoryId(ProductStatus.ACTIVE, categoryId, pageable);
    }
}
