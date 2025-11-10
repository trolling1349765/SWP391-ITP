package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.dto.request.StockForm;
import fpt.swp.springmvctt.itp.entity.OrderItem;
import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.ProductStore;
import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;
import fpt.swp.springmvctt.itp.repository.OrderItemRepository;
import fpt.swp.springmvctt.itp.repository.ProductRepository;
import fpt.swp.springmvctt.itp.repository.ProductStoreRepository;
import fpt.swp.springmvctt.itp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final ProductStoreRepository productStoreRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public Product addOrUpdateStock(StockForm form) {
        if (form.getProductId() == null) throw new IllegalArgumentException("productId is required");
        if (form.getSerial() == null || form.getSerial().isBlank()) throw new IllegalArgumentException("serial_code is required");

        Product p = productRepository.findById(form.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + form.getProductId()));

        // Check if serial already exists
        ProductStore existingPs = productStoreRepository
                .findByProductIdAndSerialCode(form.getProductId(), form.getSerial())
                .orElse(null);

        if (existingPs != null) {
            // Serial already exists - just update secret code if provided
            if (form.getCode() != null) {
                existingPs.setSecretCode(form.getCode());
                productStoreRepository.save(existingPs);
            }
            return rebuildProductQuantity(p.getId());
        }

        // Create new serial (each serial = 1 item)
        ProductStore ps = new ProductStore();
        ps.setProductId(form.getProductId());
        ps.setShopId(p.getShopId());
        ps.setSerialCode(form.getSerial());
        ps.setSecretCode(form.getCode());
        ps.setStatus(ProductStatus.HIDDEN); // new serial always hidden
        ps.setFaceValue(p.getPrice()); // copy price from product
        ps.setInfomation(form.getInfomation());

        productStoreRepository.save(ps);
        return rebuildProductQuantity(p.getId());
    }

    @Override
    public Product setSerialQuantity(Long productId, String serialCode, Long absoluteQty) {
        // This method is no longer needed since each serial = 1 item
        // But keeping for backward compatibility
        throw new UnsupportedOperationException("setSerialQuantity is deprecated - each serial represents 1 item");
    }

    @Override
    public Product rebuildProductQuantity(Long productId) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        // ⚠️ QUAN TRỌNG: Đếm chỉ ProductStore thực sự chưa bán (ACTIVE + không có OrderItem)
        // Không đếm những ProductStore đã có OrderItem với order status = COMPLETED/PENDING
        long availableCount = productStoreRepository.countAvailableStock(productId);
        p.setAvailableStock((int) Math.max(0L, Math.min(Integer.MAX_VALUE, availableCount)));
        System.out.println("📊 Rebuilt stock for product " + productId + ": " + availableCount + " serials thực sự chưa bán");
        return productRepository.save(p);
    }
    
    /**
     * Get stock count by batch (grouped by price)
     * Returns Map<Price, Count> for a specific product
     */
    public Map<java.math.BigDecimal, Long> getStockByBatches(Long productId) {
        Map<java.math.BigDecimal, Long> batchMap = new LinkedHashMap<>();
        List<Object[]> batches = productStoreRepository.findBatchesByProductId(productId);
        
        for (Object[] batch : batches) {
            java.math.BigDecimal price = (java.math.BigDecimal) batch[0];
            Long count = (Long) batch[1];
            batchMap.put(price, count);
        }
        
        return batchMap;
    }
    
    /**
     * Get stock count for a specific batch (product_id + price) - only ACTIVE items
     */
    public long getStockForBatch(Long productId, java.math.BigDecimal price) {
        return productStoreRepository.countByProductIdAndFaceValueAndStatus(productId, price, ProductStatus.ACTIVE);
    }

    @Override
    public ProductStore changeSerialStatus(Long productStoreId, ProductStatus status) {
        ProductStore ps = productStoreRepository.findById(productStoreId)
                .orElseThrow(() -> new IllegalArgumentException("ProductStore not found: " + productStoreId));
        ps.setStatus(status);
        return productStoreRepository.save(ps);
    }

    @Override @Transactional(readOnly = true)
    public List<ProductStore> listSerials(Long productId) {
        return productStoreRepository.findByProductIdOrderByIdDesc(productId);
    }

    @Override @Transactional(readOnly = true)
    public int availableStockForProduct(Long shopId, Long productId) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        return p.getAvailableStock() == null ? 0 : p.getAvailableStock();
    }

    @Override @Transactional(readOnly = true)
    public Map<Long, Integer> availableStockByProductForShop(Long shopId) {
        Map<Long, Integer> map = new LinkedHashMap<>();
        for (Product p : productRepository.findByShopIdOrderByIdDesc(shopId)) {
            map.put(p.getId(), p.getAvailableStock() == null ? 0 : p.getAvailableStock());
        }
        return map;
    }

    @Override
    public void deleteByProductId(Long productId) {
        // Chỉ xóa ProductStore chưa được bán (chưa có OrderItem reference)
        // Không xóa ProductStore đã được bán vì có foreign key constraint từ order_items
        List<ProductStore> serials = productStoreRepository.findByProductIdOrderByIdDesc(productId);
        int deletedCount = 0;
        int skippedCount = 0;
        
        for (ProductStore ps : serials) {
            // Kiểm tra xem ProductStore có OrderItem reference không
            List<OrderItem> orderItems = 
                orderItemRepository.findByProductStoreId(ps.getId());
            
            if (orderItems.isEmpty()) {
                // Chưa có OrderItem reference → có thể xóa
                productStoreRepository.delete(ps);
                deletedCount++;
            } else {
                // Đã có OrderItem reference → không xóa (đã bán)
                skippedCount++;
                System.out.println("  Skipped ProductStore ID=" + ps.getId() + 
                    " (Serial: " + ps.getSerialCode() + ") - Already sold (" + 
                    orderItems.size() + " order items)");
            }
        }
        
        System.out.println("Deleted " + deletedCount + " serials for product ID: " + productId);
        if (skippedCount > 0) {
            System.out.println("Skipped " + skippedCount + " serials (already sold) for product ID: " + productId);
        }
    }
}
