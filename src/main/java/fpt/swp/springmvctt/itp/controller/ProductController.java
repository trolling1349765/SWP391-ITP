package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.dto.FavoriteProductDTO;
import fpt.swp.springmvctt.itp.entity.Category;
import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.repository.CategoryRepository;
import fpt.swp.springmvctt.itp.service.FavoriteProductService;
import fpt.swp.springmvctt.itp.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    // ✅ chỉ thêm dòng này
    private final FavoriteProductService favoriteService;

    @GetMapping("/products")
    public String showAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "newest") String sort,
            Model model,
            HttpSession session // ✅ thêm session vào để lấy thông tin user đăng nhập
    ) {

        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 12);

        Page<Product> productPage = productService.getProductsPage(safePage, safeSize, categoryId, sort);

        int totalPages = Math.max(productPage.getTotalPages(), 1);
        if (safePage > totalPages && totalPages > 0) {
            safePage = totalPages;
            productPage = productService.getProductsPage(safePage, safeSize, categoryId, sort);
            totalPages = Math.max(productPage.getTotalPages(), 1);
        }

        List<Product> products = productPage.getContent();

        // Lấy tất cả categories để hiển thị filter
        List<Category> categories = categoryRepository.findAll();

        // Tạo map id -> name dùng cho view (không join DB)
        Map<Long, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getCategoryName));

        // ✅ Truyền thêm user session vào model để Thymeleaf check
        Object user = session.getAttribute("user");
        model.addAttribute("sessionUser", user);

        // ✅ chỉ thêm khối này (không đụng dòng nào khác)
        if (user instanceof User u) {
            List<FavoriteProductDTO> favorites = favoriteService.getFavorites(u.getEmail());
            Set<Long> favoriteProductIds = favorites.stream()
                    .map(FavoriteProductDTO::getProductId)
                    .collect(Collectors.toSet());
            model.addAttribute("favoriteProductIds", favoriteProductIds);
        }

        model.addAttribute("products", products);
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", totalPages);

        int window = 3;
        int startPage = Math.max(1, safePage - 1);
        int endPage = Math.min(totalPages, startPage + window - 1);
        startPage = Math.max(1, endPage - window + 1);

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        model.addAttribute("categories", categories);
        model.addAttribute("categoryNameMap", categoryNameMap);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("sort", sort);
        model.addAttribute("size", safeSize);

        return "user/ProductList";
    }

    /**
     * Xem chi tiết sản phẩm (Customer)
     */
    @GetMapping("/product/{id}")
    public String viewProductDetail(@PathVariable Long id, Model model, HttpSession session, HttpServletRequest request) {
        Product product = productService.getProductById(id);

        if (product == null) {
            throw new RuntimeException("Sản phẩm không tồn tại!");
        }

        // ✅ cũng truyền sessionUser vào trang chi tiết (nếu cần tim ở đó sau này)
        model.addAttribute("product", product);
        model.addAttribute("sessionUser", session.getAttribute("user"));

        model.addAttribute("requestURI", request.getRequestURI());

        // ✅ THÊM MỚI: Lấy danh sách sản phẩm yêu thích của user (nếu có)
        Object userObj = session.getAttribute("user");
        if (userObj instanceof User user) {
            List<FavoriteProductDTO> favorites = favoriteService.getFavorites(user.getEmail());
            Set<Long> favoriteProductIds = favorites.stream()
                    .map(FavoriteProductDTO::getProductId)
                    .collect(Collectors.toSet());
            model.addAttribute("favoriteProductIds", favoriteProductIds);
        }

        return "user/product-detail";
    }

}
