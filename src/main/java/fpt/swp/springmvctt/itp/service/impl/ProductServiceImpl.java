package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.dto.request.ProductForm;
import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.Shop;
import fpt.swp.springmvctt.itp.repository.CategoryRepository;
import fpt.swp.springmvctt.itp.repository.ProductRepository;
import fpt.swp.springmvctt.itp.repository.ShopRepository;
import fpt.swp.springmvctt.itp.service.ProductService;
import fpt.swp.springmvctt.itp.service.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final ShopContext shopContext;
    private final fpt.swp.springmvctt.itp.repository.ProductStoreRepository productStoreRepository;

    @Override
    public List<Product> listMyProducts() {
        Long shopId = shopContext.currentShopId();         // đã có fallback ở ShopContextImpl
        return productRepository.findByShop_IdOrderByIdDesc(shopId);
    }

    @Override
    @Transactional
    public Product create(ProductForm form) {
        Long shopId = shopContext.currentShopId();

        Product p = new Product();
        p.setProductName(form.getProductName());
        p.setDescription(form.getDescription());
        p.setPrice(form.getPrice());
        p.setAvailableStock(form.getAvailableStock());
        p.setStatus("HIDDEN");              // tạo mới luôn HIDDEN
        p.setImg(form.getImg());

        if (form.getCategoryId() != null) {
            categoryRepository.findById(form.getCategoryId()).ifPresent(p::setCategory);
        }

        Shop shop = shopRepository.getReferenceById(shopId);
        p.setShop(shop);

        return productRepository.save(p);
    }

    @Override
    @Transactional
    public void update(Long id, ProductForm form) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        p.setProductName(form.getProductName());
        p.setDescription(form.getDescription());
        p.setPrice(form.getPrice());
        p.setAvailableStock(form.getAvailableStock());

        if (form.getImg() != null && !form.getImg().isBlank()) {
            p.setImg(form.getImg());
        }

        if (form.getCategoryId() != null) {
            p.setCategory(categoryRepository.findById(form.getCategoryId()).orElse(null));
        } else {
            p.setCategory(null);
        }

        // BaseEntity có getUpdateAt()
        boolean firstUpdate = (p.getUpdateAt() == null);
        if (firstUpdate) {
            p.setStatus("HIDDEN");
        } else if (form.getStatus() != null && !form.getStatus().isBlank()) {
            p.setStatus(form.getStatus());
        }

        productRepository.save(p);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        p.setStatus("ACTIVE".equalsIgnoreCase(p.getStatus()) ? "HIDDEN" : "ACTIVE");
        productRepository.save(p);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    @Transactional
    public void syncAvailableStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        Long shopId = product.getShop().getId();
        int totalAvailable = productStoreRepository.sumQuantityForProductNotSold(productId, shopId);

        product.setAvailableStock(totalAvailable);

        // Tự động set ACTIVE nếu có stock, HIDDEN nếu hết stock
        if (totalAvailable > 0 && "HIDDEN".equals(product.getStatus())) {
            product.setStatus("ACTIVE");
        } else if (totalAvailable == 0 && "ACTIVE".equals(product.getStatus())) {
            product.setStatus("HIDDEN");
        }

        productRepository.save(product);
    }
}
