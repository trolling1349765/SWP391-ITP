package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.repository.ProductRepository;
import fpt.swp.springmvctt.itp.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public List<Product> getFeaturedProducts(int limit) {
        Pageable topN = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
        return productRepository.findByStatus("ACTIVE", topN).getContent();
    }

    // CŨ (giữ lại): mặc định newest -> gọi sang hàm mới
    @Override
    public Page<Product> getProductsPage(int page, int size, Long categoryId) {
        return getProductsPage(page, size, categoryId, "newest");
    }

    // MỚI: hỗ trợ sort linh hoạt
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
            return productRepository.findByStatus("ACTIVE", pageable);
        }
        return productRepository.findByStatusAndCategory_Id("ACTIVE", categoryId, pageable);
    }
}
