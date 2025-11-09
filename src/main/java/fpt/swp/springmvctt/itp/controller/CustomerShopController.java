package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.Shop;
import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;
import fpt.swp.springmvctt.itp.repository.ProductRepository;
import fpt.swp.springmvctt.itp.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/customer/shop")
@RequiredArgsConstructor
public class CustomerShopController {

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;

    /**
     * Xem danh sách tất cả các shop (chỉ shop đang ACTIVE)
     */
    @GetMapping("/list")
    public String listShops(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Shop> shopsPage = shopRepository.findByStatus("ACTIVE", pageable);

        model.addAttribute("shops", shopsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", shopsPage.getTotalPages());
        model.addAttribute("totalItems", shopsPage.getTotalElements());

        return "user/shop-list";
    }

    /**
     * Xem chi tiết shop và các sản phẩm của shop đó
     */
    @GetMapping("/{shopId}")
    public String viewShopDetail(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String sort,
            Model model) {

        // Tìm shop
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop không tồn tại!"));

        // Chỉ cho xem shop ACTIVE
        if (!"ACTIVE".equalsIgnoreCase(shop.getStatus())) {
            throw new RuntimeException("Shop này hiện không hoạt động!");
        }

        // Lấy danh sách sản phẩm ACTIVE của shop với phân trang
        Pageable pageable;
        if ("priceAsc".equals(sort)) {
            pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        } else if ("priceDesc".equals(sort)) {
            pageable = PageRequest.of(page, size, Sort.by("price").descending());
        } else {
            // Mặc định: sản phẩm mới nhất
            pageable = PageRequest.of(page, size, Sort.by("id").descending());
        }

        Page<Product> productsPage = productRepository.findByShopIdAndStatus(shopId, ProductStatus.ACTIVE, pageable);

        // Thống kê sản phẩm
        List<Product> allProducts = productRepository.findByShopIdOrderByIdDesc(shopId);
        long totalProducts = allProducts.size();
        long activeProducts = allProducts.stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .count();
        long inStock = allProducts.stream()
                .filter(p -> p.getAvailableStock() > 10)
                .count();
        long lowStock = allProducts.stream()
                .filter(p -> p.getAvailableStock() > 0 && p.getAvailableStock() <= 10)
                .count();
        long outOfStock = allProducts.stream()
                .filter(p -> p.getAvailableStock() == 0)
                .count();

        model.addAttribute("shop", shop);
        model.addAttribute("products", productsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productsPage.getTotalPages());
        model.addAttribute("totalItems", productsPage.getTotalElements());
        model.addAttribute("sort", sort);

        // Thống kê
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("inStock", inStock);
        model.addAttribute("lowStock", lowStock);
        model.addAttribute("outOfStock", outOfStock);

        return "user/shop-detail";
    }
}

