package fpt.swp.springmvctt.itp.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import fpt.swp.springmvctt.itp.repository.ShopRepository;

@Controller
@RequestMapping("/dev")
@RequiredArgsConstructor
public class UserController {

    private final ShopRepository shopRepository;

    // /dev/set-shop?shopId=1
    @GetMapping("/set-shop")
    public String setShop(@RequestParam Long shopId, HttpSession session, RedirectAttributes ra) {
        return shopRepository.findById(shopId).map(s -> {
            session.setAttribute("CURRENT_SHOP_ID", s.getId());
            ra.addFlashAttribute("msg", "Đã set CURRENT_SHOP_ID=" + s.getId());
            return "redirect:/shop/dashboard";
        }).orElse("redirect:/?error=shopNotFound");
    }

    @GetMapping("/clear-shop")
    public String clearShop(HttpSession session) {
        session.removeAttribute("CURRENT_SHOP_ID");
        return "redirect:/";
    }
}
