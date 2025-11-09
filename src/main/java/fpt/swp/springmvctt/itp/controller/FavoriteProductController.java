package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.dto.FavoriteProductDTO;
import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.service.FavoriteProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/favorites")
public class FavoriteProductController {

    private final FavoriteProductService favoriteService;

    /** ✅ Trang xem danh sách sản phẩm yêu thích */
    @GetMapping
    public String getFavorites(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để xem danh sách yêu thích!");
            return "redirect:/login";
        }

        List<FavoriteProductDTO> favorites = favoriteService.getFavorites(user.getEmail());
        model.addAttribute("favorites", favorites);
        return "user/favProduct"; // ✅ trỏ đúng tên file bạn có (favProduct.html)
    }

    /** ✅ Thêm sản phẩm vào danh sách yêu thích */
    @PostMapping("/add/{productId}")
    public String addFavorite(@PathVariable Long productId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để thêm sản phẩm yêu thích!");
            return "redirect:/login";
        }

        try {
            favoriteService.addFavorite(user.getEmail(), productId);
            redirectAttributes.addFlashAttribute("success", "Đã thêm sản phẩm vào danh sách yêu thích!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi thêm sản phẩm yêu thích!");
        }

        return "redirect:/products";
    }

    /** ✅ Xóa sản phẩm khỏi danh sách yêu thích */
    @PostMapping("/remove/{productId}")
    public String removeFavorite(@PathVariable Long productId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để xóa sản phẩm yêu thích!");
            return "redirect:/login";
        }

        try {
            favoriteService.removeFavorite(user.getEmail(), productId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi danh sách yêu thích!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa sản phẩm yêu thích!");
        }

        return "redirect:/favorites";
    }
}
