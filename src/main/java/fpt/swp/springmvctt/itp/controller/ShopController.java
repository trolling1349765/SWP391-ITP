package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.dto.request.ProductForm;
import fpt.swp.springmvctt.itp.repository.CategoryRepository;
import fpt.swp.springmvctt.itp.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    // /itp/shop/dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("products", productService.listMyProducts()); // list theo shop hiện tại
        return "shop/dashboard"; // Tạo templates/shop/dashboard.html
    }

    // Mở form Add Product
    @GetMapping("/products/new")
    public String addProductForm(Model model) {
        model.addAttribute("form", new ProductForm());
        model.addAttribute("categories", categoryRepository.findAll());
        return "addProduct"; // đúng tên file bạn đang có
    }

    // Submit tạo mới
    @PostMapping("/products")
    public String createProduct(@ModelAttribute("form") ProductForm form,
                                RedirectAttributes ra) {
        productService.create(form);      // service sẽ ép status = HIDDEN
        ra.addFlashAttribute("msg", "Created!");
        return "redirect:/shop/dashboard";
    }
}
