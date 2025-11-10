package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.dto.FavoriteProductDTO;
import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.service.FavoriteProductService;
import fpt.swp.springmvctt.itp.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {
    private final ProductService productService;

    // ✅ chỉ thêm dòng này
    private final FavoriteProductService favoriteService;

    @GetMapping()
    public String home(@ModelAttribute("success") String successMessage, Model model, HttpSession session) {
        System.out.println("Thông báo: " + successMessage);
        List<Product> featured = productService.getFeaturedProducts(8);
        model.addAttribute("featuredProducts", featured);

        // ✅ chỉ thêm đoạn này
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);

        if (user != null) {
            List<FavoriteProductDTO> favorites = favoriteService.getFavorites(user.getEmail());
            Set<Long> favoriteProductIds = favorites.stream()
                    .map(FavoriteProductDTO::getProductId)
                    .collect(Collectors.toSet());
            model.addAttribute("favoriteProductIds", favoriteProductIds);
        }

        return "user/home";
    }
}
