package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.dto.request.StockForm;
import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.service.InventoryService;
import fpt.swp.springmvctt.itp.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/shop")
public class InventoryController {

    private final InventoryService inventoryService;
    private final ProductService productService;

    // GET /shop/inventory  -> templates/shop/inventory.html
    @GetMapping("/inventory")
    public String inventoryPage(Model model) {
        List<Product> products = productService.listMyProducts();
        model.addAttribute("products", products);
        model.addAttribute("form", new StockForm());
        model.addAttribute("active", "/shop/inventory");
        return "shop/inventory";
    }

    // POST /shop/inventory
    @PostMapping("/inventory")
    public String addOrUpdateStock(@ModelAttribute("form") StockForm form,
                                   RedirectAttributes ra) {
        try {
            inventoryService.addOrUpdateStock(form);
            ra.addFlashAttribute("ok", "Đã thêm/cập nhật kho cho sản phẩm #" + form.getProductId());
        } catch (Exception e) {
            ra.addFlashAttribute("err", e.getMessage());
        }
        return "redirect:/shop/inventory";
    }
}
