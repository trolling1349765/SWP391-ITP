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
        System.out.println("✅ Created product ID: " + savedProduct.getId() + " - Name: " + savedProduct.getProductName());
        System.out.println("📝 Detailed Description: " + savedProduct.getDetailedDescription());

        // Import serials from Excel file if provided
        System.out.println("🔍 Checking for Excel file...");
        System.out.println("   - form.getSerialFile() = " + (form.getSerialFile() != null ? "NOT NULL" : "NULL"));
        if (form.getSerialFile() != null) {
            System.out.println("   - file.isEmpty() = " + form.getSerialFile().isEmpty());
            System.out.println("   - file.getOriginalFilename() = " + form.getSerialFile().getOriginalFilename());
            System.out.println("   - file.getSize() = " + form.getSerialFile().getSize() + " bytes");
        }

        if (form.getSerialFile() != null && !form.getSerialFile().isEmpty()) {
            try {
                System.out.println("📥 Starting Excel import for product " + savedProduct.getId() + "...");
                ExcelImportForm importForm = new ExcelImportForm();
                importForm.setProductId(savedProduct.getId());
                importForm.setExcelFile(form.getSerialFile());
                importForm.setOverrideExisting(false);

                ImportResult result = excelImportService.importSerialsFromExcel(importForm);
                System.out.println("✅ Import completed!");
                System.out.println("   - Imported: " + result.getImportedCount());
                System.out.println("   - Skipped: " + result.getSkippedCount());
                System.out.println("   - Errors: " + result.getErrors().size());
                System.out.println("   - Warnings: " + result.getWarnings().size());

                if (result.getErrors().size() > 0) {
                    System.out.println("❌ Import errors: " + result.getErrors());
                }
                if (result.getWarnings().size() > 0) {
                    System.out.println("⚠️ Import warnings: " + result.getWarnings());
                }

                // Rebuild product quantity after import
                System.out.println("🔄 Rebuilding product quantity...");
                savedProduct = inventoryService.rebuildProductQuantity(savedProduct.getId());
                System.out.println("✅ Final availableStock: " + savedProduct.getAvailableStock());

            } catch (Exception e) {
                System.err.println("❌ Error importing serials: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠️ No Excel file provided - product created without serials");
        }

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
        return productRepository.findById(id).orElse(null);
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

    // CŨ (giữ lại): mặc định newest -> gọi sang hàm mới
    @Override
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

                // Xóa tất cả serials (product_stores) trước khi xóa sản phẩm
                inventoryService.deleteByProductId(id);

                //xóa sản phẩm
                productRepository.delete(product);
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
