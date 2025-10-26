package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {

    // 🏠 Trang chủ người dùng
//    @GetMapping
//    public String homepage(@RequestParam String message, Model model) {
//        model.addAttribute("message", message);
//        // => src/main/resources/templates/user/home.html
//        return "user/Homepage";
//    }
    private final ProductService productService;

    @GetMapping()
    public String home(@ModelAttribute("success") String successMessage, Model model, HttpSession session) {
        System.out.println("Thông báo: " + successMessage);
        List<Product> featured = productService.getFeaturedProducts(8); // lấy 8 sp
        model.addAttribute("featuredProducts", featured);
        
        // Add user session to model for template
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        
        return "user/home";
    }

}
