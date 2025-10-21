package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.dto.request.ProductForm;
import fpt.swp.springmvctt.itp.dto.request.ExcelImportForm;
import fpt.swp.springmvctt.itp.dto.response.ImportResult;
import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;
import fpt.swp.springmvctt.itp.entity.enums.ProductType;
import fpt.swp.springmvctt.itp.repository.ProductRepository;
import fpt.swp.springmvctt.itp.service.ProductService;
import fpt.swp.springmvctt.itp.service.StorageService;
import fpt.swp.springmvctt.itp.service.InventoryService;
import fpt.swp.springmvctt.itp.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final StorageService storageService;
    private final InventoryService inventoryService;
    private final ExcelImportService excelImportService;

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
        
        // Import serials from Excel file if provided
        if (form.getSerialFile() != null && !form.getSerialFile().isEmpty()) {
            try {
                ExcelImportForm importForm = new ExcelImportForm();
                importForm.setProductId(savedProduct.getId());
                importForm.setExcelFile(form.getSerialFile());
                importForm.setOverrideExisting(false);
                
                   ImportResult result = excelImportService.importSerialsFromExcel(importForm);
                   System.out.println("Imported " + result.getImportedCount() + " serials for product " + savedProduct.getId());
                   if (result.getErrors().size() > 0) {
                       System.out.println("Import errors: " + result.getErrors());
                   }
                   if (result.getWarnings().size() > 0) {
                       System.out.println("Import warnings: " + result.getWarnings());
                   }
                
                // Rebuild product quantity after import
                savedProduct = inventoryService.rebuildProductQuantity(savedProduct.getId());
                
            } catch (Exception e) {
                System.err.println("Error importing serials: " + e.getMessage());
                e.printStackTrace();
            }
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
    public Product changeStatus(Long productId, ProductStatus status) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        p.setStatus(status);
        return productRepository.save(p);
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

    @Override
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        
        // Xóa tất cả serials (product_stores) trước khi xóa sản phẩm
        inventoryService.deleteByProductId(id);
        
        //xóa sản phẩm
        productRepository.delete(product);
    }
}
