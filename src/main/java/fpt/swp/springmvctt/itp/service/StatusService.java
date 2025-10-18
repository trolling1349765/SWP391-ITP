package fpt.swp.springmvctt.itp.service;
import fpt.swp.springmvctt.itp.repository.ProductRepository;
import fpt.swp.springmvctt.itp.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StatusService {
    private static final Set<String> SHOP_STATUS = Set.of("ACTIVE", "BLOCKED");
    private static final Set<String> PRODUCT_STATUS = Set.of("ACTIVE", "INACTIVE", "OUT_OF_STOCK", "HIDDEN");

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void updateShopStatus(Long shopId, String newStatus, String actor) {
        if (!SHOP_STATUS.contains(newStatus)) {
            throw new IllegalArgumentException("Invalid shop status");
        }
        var shop = shopRepository.findById(shopId).orElseThrow();
        shop.setStatus(newStatus);
        shop.setUpdateBy(actor);
        shop.setUpdateAt(LocalDateTime.now());
        // shopRepository.save(shop);
    }

    @Transactional
    public void updateProductStatus(Long productId, String newStatus, String actor) {
        if (!PRODUCT_STATUS.contains(newStatus)) {
            throw new IllegalArgumentException("Invalid product status");
        }
        var p = productRepository.findById(productId).orElseThrow();
        p.setStatus(newStatus);
        p.setUpdateBy(actor);
        p.setUpdateAt(LocalDateTime.now());
    }
}
