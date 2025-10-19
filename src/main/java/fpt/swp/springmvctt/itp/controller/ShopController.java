package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.dto.request.ProductForm;
import fpt.swp.springmvctt.itp.dto.request.StockForm;
import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.ProductStore;
import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;
import fpt.swp.springmvctt.itp.service.InventoryService;
import fpt.swp.springmvctt.itp.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ProductService productService;
    private final InventoryService inventoryService;
    private static final Long SHOP_ID = 1L; // shop demo

    private void putCurrentPath(Model model, HttpServletRequest request) {
        model.addAttribute("currentPath", request != null ? request.getRequestURI() : "");
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpServletRequest request) {
        putCurrentPath(model, request);
        List<Product> products = productService.listByShop(SHOP_ID);
        Map<Long, Integer> stockMap = new LinkedHashMap<>();
        for (Product p : products) stockMap.put(p.getId(), p.getAvailableStock());
        model.addAttribute("products", products);
        model.addAttribute("stockMap", stockMap);
        return "shop/dashboard";
    }

    // Add Product
    @GetMapping("/addProduct")
    public String addProductForm(Model model, HttpServletRequest request) {
        putCurrentPath(model, request);
        model.addAttribute("form", new ProductForm());
        return "shop/addProduct";
    }

    @PostMapping("/addProduct")
    public String addProductSubmit(@ModelAttribute("form") ProductForm form, RedirectAttributes ra) {
        Product created = productService.createProduct(SHOP_ID, form); // HIDDEN
        ra.addFlashAttribute("ok", "Đã tạo sản phẩm #" + created.getId() + " (HIDDEN)");
        return "redirect:/shop/dashboard";
    }

    // Update Product (tên/mô tả/giá/ảnh)
    @GetMapping("/updateProduct/{id}")
    public String updateProductForm(@PathVariable Long id, Model model, HttpServletRequest request) {
        putCurrentPath(model, request);
        Product product = productService.get(id);
        ProductForm form = new ProductForm();
        form.setProductName(product.getProductName());
        form.setDescription(product.getDescription());
        form.setPrice(product.getPrice());
        form.setCategoryId(product.getCategoryId());
        form.setImg(product.getImage());
        model.addAttribute("productId", id);
        model.addAttribute("product", product);
        model.addAttribute("availableStock", product.getAvailableStock());
        model.addAttribute("form", form);
        return "shop/updateProduct";
    }

    @PostMapping("/updateProduct/{id}")
    public String updateProductSubmit(@PathVariable Long id, @ModelAttribute("form") ProductForm form,
                                      RedirectAttributes ra) {
        productService.updateProduct(id, form);
        ra.addFlashAttribute("ok", "Đã cập nhật sản phẩm #" + id);
        return "redirect:/shop/dashboard";
    }

    // Đổi trạng thái sản phẩm tay: ACTIVE/HIDDEN/BLOCKED
    @PostMapping("/products/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam ProductStatus status,
                               RedirectAttributes ra) {
        productService.changeStatus(id, status);
        ra.addFlashAttribute("ok", "Đã đổi trạng thái sản phẩm #" + id + " → " + status);
        return "redirect:/shop/dashboard";
    }

    // INVENTORY (serial)
    @GetMapping("/inventory")
    public String inventory(Model model, HttpServletRequest request) {
        putCurrentPath(model, request);
        List<Product> products = productService.listByShop(SHOP_ID);
        model.addAttribute("products", products);
        model.addAttribute("form", new StockForm());
        return "shop/inventory";
    }

    @PostMapping("/inventory")
    public String inventorySubmit(@ModelAttribute("form") StockForm form, RedirectAttributes ra) {
        inventoryService.addOrUpdateStock(form); // tạo/cộng serial + rebuild stock
        ra.addFlashAttribute("ok", "Đã cập nhật kho cho SP #" + form.getProductId());
        return "redirect:/shop/inventory";
    }

    @GetMapping("/products/{id}/serials")
    public String listSerials(@PathVariable Long id, Model model) {
        List<ProductStore> serials = inventoryService.listSerials(id);
        model.addAttribute("product", productService.get(id));
        model.addAttribute("serials", serials);
        return "shop/ProductSerialList";
    }

    @PostMapping("/stocks/{psId}/status")
    public String changeSerialStatus(@PathVariable("psId") Long productStoreId,
                                     @RequestParam("status") ProductStatus status,
                                     @RequestParam("productId") Long productId,
                                     RedirectAttributes ra) {
        inventoryService.changeSerialStatus(productStoreId, status);
        ra.addFlashAttribute("ok", "Đã đổi trạng thái serial → " + status);
        return "redirect:/shop/products/" + productId + "/serials";
    }
}
