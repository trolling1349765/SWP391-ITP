package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.dto.FavoriteProductDTO;
import fpt.swp.springmvctt.itp.entity.Category;
import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.repository.CategoryRepository;
import fpt.swp.springmvctt.itp.service.FavoriteProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/favorites")
public class FavoriteProductController {

    private final FavoriteProductService favoriteService;
    private final CategoryRepository categoryRepository;

    /** ✅ HIỂN THỊ DANH SÁCH YÊU THÍCH */
    @GetMapping
    public String getFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "default") String sort,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để xem danh sách yêu thích!");
            return "redirect:/login";
        }

        List<FavoriteProductDTO> allFavorites = favoriteService.getFavorites(user.getEmail());

        // Bộ lọc Category
        if (categoryId != null) {
            allFavorites = allFavorites.stream()
                    .filter(f -> f.getCategoryId() != null && f.getCategoryId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        // Bộ lọc Search
        if (search != null && !search.trim().isEmpty()) {
            String lower = search.toLowerCase();
            allFavorites = allFavorites.stream()
                    .filter(f ->
                            (f.getProductName() != null && f.getProductName().toLowerCase().contains(lower)) ||
                                    (f.getShopName() != null && f.getShopName().toLowerCase().contains(lower))
                    )
                    .collect(Collectors.toList());
        }

        // Sắp xếp
        switch (sort) {
            case "priceAsc" -> allFavorites.sort(
                    Comparator.comparing(FavoriteProductDTO::getPrice, Comparator.nullsLast(BigDecimal::compareTo))
            );
            case "priceDesc" -> allFavorites.sort(
                    Comparator.comparing(FavoriteProductDTO::getPrice, Comparator.nullsLast(BigDecimal::compareTo)).reversed()
            );
            case "newest" -> allFavorites.sort(
                    Comparator.comparing(FavoriteProductDTO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
            );
        }

        // Phân trang
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 12);
        int start = (safePage - 1) * safeSize;
        int end = Math.min(start + safeSize, allFavorites.size());
        List<FavoriteProductDTO> favoritesPage = allFavorites.subList(start, end);
        Page<FavoriteProductDTO> favoritePage = new PageImpl<>(favoritesPage, PageRequest.of(safePage - 1, safeSize), allFavorites.size());

        int totalPages = Math.max(favoritePage.getTotalPages(), 1);
        int window = 3;
        int startPage = Math.max(1, safePage - 1);
        int endPage = Math.min(totalPages, startPage + window - 1);
        startPage = Math.max(1, endPage - window + 1);

        // Category map
        List<Category> categories = categoryRepository.findAll();
        Map<Long, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getCategoryName));

        model.addAttribute("favorites", favoritesPage);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryNameMap", categoryNameMap);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("size", safeSize);
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "user/favProduct";
    }

    /** ✅ THÊM SẢN PHẨM YÊU THÍCH */
    @PostMapping("/add/{productId}")
    public String addFavorite(
            @PathVariable Long productId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để thêm sản phẩm yêu thích!");
            return "redirect:/login";
        }

        try {
            favoriteService.addFavorite(user.getEmail(), productId);
            redirectAttributes.addFlashAttribute("success", "Đã thêm sản phẩm vào danh sách yêu thích!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể thêm sản phẩm yêu thích: " + e.getMessage());
        }

        return "redirect:/products";
    }

    /** ✅ XÓA SẢN PHẨM YÊU THÍCH */
    @PostMapping("/remove/{productId}")
    public String removeFavorite(
            @PathVariable Long productId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để xóa sản phẩm yêu thích!");
            return "redirect:/login";
        }

        try {
            favoriteService.removeFavorite(user.getEmail(), productId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi danh sách yêu thích!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa sản phẩm yêu thích: " + e.getMessage());
        }

        return "redirect:/favorites";
    }
}
