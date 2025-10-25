package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        List<Product> featured = productService.getFeaturedProducts(8); // lấy 8 sp
        model.addAttribute("featuredProducts", featured);
        return "user/home"; // <== đường dẫn template
    }
}
