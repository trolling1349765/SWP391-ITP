package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.dto.FavoriteProductDTO;
import fpt.swp.springmvctt.itp.service.FavoriteProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/favorites")
public class FavoriteProductController {

    private final FavoriteProductService favoriteService;

    @GetMapping
    public String getFavorites(Model model, Principal principal) {
        List<FavoriteProductDTO> favorites = favoriteService.getFavorites(principal.getName());
        model.addAttribute("favorites", favorites);
        return "user/favorites"; // file Thymeleaf
    }

    @PostMapping("/add/{productId}")
    public String addFavorite(@PathVariable Long productId, Principal principal) {
        favoriteService.addFavorite(principal.getName(), productId);
        return "redirect:/favorites";
    }

    @PostMapping("/remove/{productId}")
    public String removeFavorite(@PathVariable Long productId, Principal principal) {
        favoriteService.removeFavorite(principal.getName(), productId);
        return "redirect:/favorites";
    }
}
