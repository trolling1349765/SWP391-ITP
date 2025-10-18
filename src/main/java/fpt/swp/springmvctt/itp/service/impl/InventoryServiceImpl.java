package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.dto.request.StockForm;
import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.ProductStore;
import fpt.swp.springmvctt.itp.entity.Shop;
import fpt.swp.springmvctt.itp.repository.ProductRepository;
import fpt.swp.springmvctt.itp.repository.ProductStoreRepository;
import fpt.swp.springmvctt.itp.repository.ShopRepository;
import fpt.swp.springmvctt.itp.service.InventoryService;
import fpt.swp.springmvctt.itp.service.ProductService;
import fpt.swp.springmvctt.itp.service.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductStoreRepository productStoreRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final ProductService productService;
    private final ShopContext shopContext;

    @Override
    @Transactional
    public void addOrUpdateStock(StockForm form) {
        // Lấy shopId (fallback = 1L nếu có vấn đề)
        long sid;
        try {
            sid = shopContext.currentShopId();
        } catch (RuntimeException ex) {
            sid = 1L;
        }
        Long shopId = sid;

        Product product = productRepository.findById(form.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + form.getProductId()));

        if (!product.getShop().getId().equals(shopId)) {
            throw new IllegalStateException("No permission to modify this product");
        }

        Shop shop = shopRepository.getReferenceById(shopId);

        ProductStore ps = productStoreRepository
                .findByProduct_IdAndShop_IdAndSerialCode(product.getId(), shop.getId(), form.getSerialCode());

        if (ps == null) {
            ps = new ProductStore();
            ps.setProduct(product);
            ps.setShop(shop);
            ps.setSerialCode(form.getSerialCode());
            ps.setSecretCode(form.getSecretCode());
        } else {
            if (ps.getSecretCode() != null && form.getSecretCode() != null
                    && !ps.getSecretCode().equals(form.getSecretCode())) {
                throw new IllegalArgumentException("Secret code không khớp với serial đã tồn tại");
            }
            if (ps.getSecretCode() == null && form.getSecretCode() != null) {
                ps.setSecretCode(form.getSecretCode());
            }
        }

        ps.setFaceValue(form.getFaceValue());
        ps.setInfomation(form.getInfomation());
        ps.setQuantity(form.getQuantity() == null ? "1" : form.getQuantity());
        ps.setStatus(form.getStatus() == null ? "AVAILABLE" : form.getStatus());

        productStoreRepository.save(ps);
        productService.syncAvailableStock(product.getId());
    }
}
