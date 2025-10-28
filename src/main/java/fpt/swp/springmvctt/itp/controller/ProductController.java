package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.repository.CategoryRepository;
import fpt.swp.springmvctt.itp.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/products")
    public String showAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,   // <= mặc định 12
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "newest") String sort,
            Model model) {

        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 12); // <= luôn <= 12

        Page<Product> productPage = productService.getProductsPage(safePage, safeSize, categoryId, sort);

        int totalPages = Math.max(productPage.getTotalPages(), 1);
        if (safePage > totalPages && totalPages > 0) {
            safePage = totalPages;
            productPage = productService.getProductsPage(safePage, safeSize, categoryId, sort);
            totalPages = Math.max(productPage.getTotalPages(), 1);
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", totalPages);

        int window = 3;
        int startPage = Math.max(1, safePage - 1);
        int endPage = Math.min(totalPages, startPage + window - 1);
        startPage = Math.max(1, endPage - window + 1);

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("sort", sort);
        model.addAttribute("size", safeSize); // giữ size khi chuyển trang

        return "user/ProductList";
    }

}
