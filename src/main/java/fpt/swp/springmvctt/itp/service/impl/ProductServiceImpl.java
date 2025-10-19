package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.dto.request.ProductForm;
import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;
import fpt.swp.springmvctt.itp.repository.ProductRepository;
import fpt.swp.springmvctt.itp.service.ProductService;
import fpt.swp.springmvctt.itp.service.StorageService;
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

    @Override
    public Product createProduct(Long shopId, ProductForm form) {
        Product p = new Product();
        p.setShopId(shopId);
        p.setProductName(form.getProductName());
        p.setDescription(form.getDescription());
        p.setPrice(form.getPrice() == null ? BigDecimal.ZERO : form.getPrice());
        p.setCategoryId(form.getCategoryId());
        p.setStatus(ProductStatus.HIDDEN);
        p.setAvailableStock(0);

        if (form.getFile() != null && !form.getFile().isEmpty()) {
            p.setImage(storageService.saveProductImage(form.getFile()));
        } else if (form.getImg() != null && !form.getImg().isBlank()) {
            p.setImage(form.getImg());
        }
        return productRepository.save(p);
    }

    @Override
    public Product updateProduct(Long productId, ProductForm form) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        if (form.getProductName() != null) p.setProductName(form.getProductName());
        if (form.getDescription() != null) p.setDescription(form.getDescription());
        if (form.getPrice() != null) p.setPrice(form.getPrice());
        if (form.getCategoryId() != null) p.setCategoryId(form.getCategoryId());

        if (form.getFile() != null && !form.getFile().isEmpty()) {
            p.setImage(storageService.saveProductImage(form.getFile()));
        } else if (form.getImg() != null && !form.getImg().isBlank()) {
            p.setImage(form.getImg());
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
}
