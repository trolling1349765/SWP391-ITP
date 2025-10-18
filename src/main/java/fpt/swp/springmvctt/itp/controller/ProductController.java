package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.dto.request.ProductForm;
import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.service.CategoryService;
import fpt.swp.springmvctt.itp.service.ProductService;
import fpt.swp.springmvctt.itp.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/shop")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final StorageService storageService;

    // GET /shop/addProduct - Hiển thị form add product
    @GetMapping("/addProduct")
    public String showAddProductForm(Model model) {
        model.addAttribute("form", new ProductForm());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("active", "/shop/addProduct");
        return "shop/addProduct";
    }

    // POST /shop/addProduct - Xử lý add product
    @PostMapping("/addProduct")
    public String addProduct(@ModelAttribute("form") ProductForm form,
                             RedirectAttributes ra) {
        try {
            // Upload ảnh nếu có
            if (form.getFile() != null && !form.getFile().isEmpty()) {
                String imgPath = storageService.store(form.getFile(), form.getImgSubdir());
                form.setImg(imgPath);
            }

            Product product = productService.create(form);
            ra.addFlashAttribute("ok", "Đã thêm sản phẩm: " + product.getProductName());
            return "redirect:/shop/dashboard";
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Lỗi: " + e.getMessage());
            return "redirect:/shop/addProduct";
        }
    }

    // GET /shop/updateProduct/{id} - Hiển thị form update
    @GetMapping("/updateProduct/{id}")
    public String showUpdateProductForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            Product product = productService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            ProductForm form = new ProductForm();
            form.setProductName(product.getProductName());
            form.setDescription(product.getDescription());
            form.setPrice(product.getPrice());
            form.setAvailableStock(product.getAvailableStock());
            form.setImg(product.getImg());
            form.setStatus(product.getStatus());
            if (product.getCategory() != null) {
                form.setCategoryId(product.getCategory().getId());
            }

            model.addAttribute("form", form);
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("active", "/shop/dashboard");
            return "shop/updateProduct";
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Lỗi: " + e.getMessage());
            return "redirect:/shop/dashboard";
        }
    }

    // POST /shop/updateProduct/{id} - Xử lý update
    @PostMapping("/updateProduct/{id}")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute("form") ProductForm form,
                                RedirectAttributes ra) {
        try {
            // Upload ảnh mới nếu có
            if (form.getFile() != null && !form.getFile().isEmpty()) {
                String imgPath = storageService.store(form.getFile(), form.getImgSubdir());
                form.setImg(imgPath);
            }

            productService.update(id, form);
            ra.addFlashAttribute("ok", "Đã cập nhật sản phẩm #" + id);
            return "redirect:/shop/dashboard";
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Lỗi: " + e.getMessage());
            return "redirect:/shop/updateProduct/" + id;
        }
    }

    // POST /shop/toggleStatus/{id} - Toggle ACTIVE/HIDDEN
    @PostMapping("/toggleStatus/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productService.toggleStatus(id);
            ra.addFlashAttribute("ok", "Đã thay đổi trạng thái sản phẩm #" + id);
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Lỗi: " + e.getMessage());
        }
        return "redirect:/shop/dashboard";
    }
}
