package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.dto.request.ProductForm;
import fpt.swp.springmvctt.itp.dto.request.StockForm;
import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.ProductStore;
import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;
import fpt.swp.springmvctt.itp.service.CategoryService;
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
    private final CategoryService categoryService;
    private static final Long SHOP_ID = 1L; // shop demo

    private void putCurrentPath(Model model, HttpServletRequest request) {
        model.addAttribute("currentPath", request != null ? request.getRequestURI() : "");
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model, HttpServletRequest request) {
        putCurrentPath(model, request);
        List<Product> allProducts = productService.listByShop(SHOP_ID);
        
        // Sắp xếp theo ID tăng dần (từ bé lên lớn)
        allProducts.sort((p1, p2) -> Long.compare(p1.getId(), p2.getId()));
        
        // Tính toán phân trang
        int totalProducts = allProducts.size();
        int totalPages = (int) Math.ceil((double) totalProducts / size);
        page = Math.max(1, Math.min(page, totalPages)); // Đảm bảo page hợp lệ
        
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, totalProducts);
        List<Product> products = allProducts.subList(startIndex, endIndex);
        
        Map<Long, Integer> stockMap = new LinkedHashMap<>();
        for (Product p : products) stockMap.put(p.getId(), p.getAvailableStock());
        
        model.addAttribute("products", products);
        model.addAttribute("stockMap", stockMap);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalProducts", totalProducts);
        
        return "shop/dashboard";
    }

    // Add Product
    @GetMapping("/addProduct")
    public String addProductForm(Model model, HttpServletRequest request) {
        putCurrentPath(model, request);
        model.addAttribute("form", new ProductForm());
        model.addAttribute("categories", categoryService.findAll());
        return "shop/addProduct";
    }

    @PostMapping("/addProduct")
    public String addProductSubmit(@ModelAttribute("form") ProductForm form, 
                                    @RequestParam(required = false) String serial,
                                    @RequestParam(required = false) String secretCode,
                                    @RequestParam(required = false) Integer stockQuantity,
                                    RedirectAttributes ra) {
        Product created = productService.createProduct(SHOP_ID, form); // HIDDEN
        
        // Nếu có serial, tạo stock luôn
        if (serial != null && !serial.isBlank() && stockQuantity != null && stockQuantity > 0) {
            StockForm stockForm = new StockForm();
            stockForm.setProductId(created.getId());
            stockForm.setSerial(serial);
            stockForm.setCode(secretCode);
            stockForm.setQuantity(stockQuantity);
            inventoryService.addOrUpdateStock(stockForm);
            ra.addFlashAttribute("ok", "Đã tạo sản phẩm #" + created.getId() + " và thêm " + stockQuantity + " vào kho (HIDDEN)");
        } else {
            ra.addFlashAttribute("ok", "Đã tạo sản phẩm #" + created.getId() + " (HIDDEN)");
        }
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
        model.addAttribute("categories", categoryService.findAll());
        return "shop/updateProduct";
    }

    @PostMapping("/updateProduct/{id}")
    public String updateProductSubmit(@PathVariable Long id, 
                                      @ModelAttribute("form") ProductForm form,
                                      @RequestParam(required = false) ProductStatus status,
                                      RedirectAttributes ra) {
        productService.updateProduct(id, form);
        
        // Cập nhật status nếu có
        if (status != null) {
            productService.changeStatus(id, status);
            ra.addFlashAttribute("ok", "Đã cập nhật sản phẩm #" + id + " → Status: " + status);
        } else {
            ra.addFlashAttribute("ok", "Đã cập nhật sản phẩm #" + id);
        }
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
        
        // Sắp xếp theo ID tăng dần (từ bé lên lớn)
        products.sort((p1, p2) -> Long.compare(p1.getId(), p2.getId()));
        
        // Tạo stockMap để hiển thị tồn kho trong template
        Map<Long, Integer> stockMap = new LinkedHashMap<>();
        for (Product p : products) stockMap.put(p.getId(), p.getAvailableStock());
        
        model.addAttribute("products", products);
        model.addAttribute("stockMap", stockMap);
        model.addAttribute("form", new StockForm());
        return "shop/inventory";
    }

    // Add Serial form
    @GetMapping("/addSerial")
    public String addSerialForm(Model model, HttpServletRequest request) {
        putCurrentPath(model, request);
        List<Product> products = productService.listByShop(SHOP_ID);
        
        // Sắp xếp theo ID tăng dần (từ bé lên lớn)
        products.sort((p1, p2) -> Long.compare(p1.getId(), p2.getId()));
        
        model.addAttribute("products", products);
        model.addAttribute("form", new StockForm());
        return "shop/addSerial";
    }

    @PostMapping("/addSerial")
    public String addSerialSubmit(@ModelAttribute("form") StockForm form, RedirectAttributes ra) {
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

    // Chi tiết sản phẩm (đủ thuộc tính)
    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product p = productService.get(id);
        List<ProductStore> serials = inventoryService.listSerials(id);
        model.addAttribute("productDetail", p);
        model.addAttribute("product", p);
        model.addAttribute("serials", serials);
        return "shop/ProductDetail";
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
