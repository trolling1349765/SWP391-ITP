package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.dto.request.StockForm;
import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.ProductStore;
import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;
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

    @Override
    public Product addOrUpdateStock(StockForm form) {
        if (form.getProductId() == null) throw new IllegalArgumentException("productId is required");
        if (form.getSerial() == null || form.getSerial().isBlank()) throw new IllegalArgumentException("serial_code is required");
        if (form.getQuantity() == null || form.getQuantity() < 0) throw new IllegalArgumentException("quantity must be >= 0");

        Product p = productRepository.findById(form.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + form.getProductId()));

        ProductStore ps = productStoreRepository
                .findByProductIdAndSerialCode(form.getProductId(), form.getSerial())
                .orElse(null);

        if (ps == null) {
            ps = new ProductStore();
            ps.setProductId(form.getProductId());
            ps.setShopId(p.getShopId());
            ps.setSerialCode(form.getSerial());
            ps.setSecretCode(form.getCode());
            ps.setQuantity(0);
            ps.setStatus(ProductStatus.HIDDEN); // serial mới luôn ẩn
        }

        int base = ps.getQuantity() == null ? 0 : ps.getQuantity();
        int add  = form.getQuantity() == null ? 0 : form.getQuantity();
        ps.setQuantity(base + add);
        if (form.getCode() != null) ps.setSecretCode(form.getCode());

        productStoreRepository.save(ps);
        return rebuildProductQuantity(p.getId());
    }

    @Override
    public Product setSerialQuantity(Long productId, String serialCode, Long absoluteQty) {
        if (absoluteQty == null || absoluteQty < 0) throw new IllegalArgumentException("absoluteQty must be >= 0");
        productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        ProductStore ps = productStoreRepository
                .findByProductIdAndSerialCode(productId, serialCode)
                .orElseThrow(() -> new IllegalArgumentException("Serial not found: " + serialCode));

        ps.setQuantity(absoluteQty.intValue());
        productStoreRepository.save(ps);
        return rebuildProductQuantity(productId);
    }

    @Override
    public Product rebuildProductQuantity(Long productId) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        long sum = 0L;
        for (ProductStore s : productStoreRepository.findByProductIdOrderByIdDesc(productId)) {
            sum += (s.getQuantity() == null ? 0 : s.getQuantity());
        }
        p.setAvailableStock((int) Math.max(0L, Math.min(Integer.MAX_VALUE, sum)));
        return productRepository.save(p);
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
        // Xóa tất cả serials (ProductStore)
        List<ProductStore> serials = productStoreRepository.findByProductIdOrderByIdDesc(productId);
        productStoreRepository.deleteAll(serials);
        System.out.println("Deleted " + serials.size() + " serials for product ID: " + productId);
    }
}
