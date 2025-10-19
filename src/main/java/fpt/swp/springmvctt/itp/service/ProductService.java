package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.dto.request.ProductForm;
import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;

import java.util.List;

public interface ProductService {
    Product createProduct(Long shopId, ProductForm form); // default HIDDEN
    Product updateProduct(Long productId, ProductForm form);
    Product changeStatus(Long productId, ProductStatus status);
    Product get(Long id);
    List<Product> listByShop(Long shopId);
    void delete(Long id);
}
