package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.dto.request.ProductForm;
import fpt.swp.springmvctt.itp.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> listMyProducts();
    Product create(ProductForm form);
    void update(Long id, ProductForm form);
    void toggleStatus(Long id);
    Optional<Product> findById(Long id);
    void syncAvailableStock(Long productId);
}
