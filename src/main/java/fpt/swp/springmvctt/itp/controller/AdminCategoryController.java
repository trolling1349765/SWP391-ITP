package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.entity.Category;
import fpt.swp.springmvctt.itp.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    /**
     * Cho tất cả view trong controller này 1 biến 'currentPath'
     * để Sidebar fragment active (không cần #request).
     */
    @ModelAttribute("currentPath")
    public String currentPath() {
        // Giữ cố định /admin/categories để menu “Categories” luôn active
        // (Nếu muốn chính xác tuyệt đối theo URL hiện tại, thay bằng request.getRequestURI()).
        return "/admin/categories";
    }

    // LIST
    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size,
                       @RequestParam(value = "success", required = false) String successMsg,
                       @RequestParam(value = "error", required = false) String errorMsg,
                       Model model) {

        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        Page<Category> pageData = categoryService.search(q, page, size);

        model.addAttribute("pageData", pageData);
        model.addAttribute("q", q);

        // Để view hiển thị alert (đã có khối th:if trong HTML)
        if (StringUtils.hasText(successMsg)) model.addAttribute("success", successMsg);
        if (StringUtils.hasText(errorMsg))   model.addAttribute("error", errorMsg);

        return "admin/viewCategory";
    }

    // CREATE - form
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("isEdit", false);
        return "admin/addCategory";
    }

    // CREATE - submit
    @PostMapping("/create")
    public String createSubmit(@ModelAttribute Category category, Model model) {
        if (!StringUtils.hasText(category.getCategoryName())) {
            model.addAttribute("category", category);
            model.addAttribute("isEdit", false);
            model.addAttribute("error", "Category name is required.");
            return "admin/addCategory";
        }
        // Trim name cho sạch dữ liệu
        category.setCategoryName(category.getCategoryName().trim());
        categoryService.save(category);
        return "redirect:/admin/categories?success=Created";
    }

    // EDIT - form
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Category c = categoryService.findById(id).orElse(null);
        if (c == null) return "redirect:/admin/categories?error=NotFound";
        model.addAttribute("category", c);
        model.addAttribute("isEdit", true);
        return "admin/addCategory";
    }

    // EDIT - submit
    @PostMapping("/{id}/edit")
    public String editSubmit(@PathVariable Long id, @ModelAttribute Category form, Model model) {
        Category c = categoryService.findById(id).orElse(null);
        if (c == null) return "redirect:/admin/categories?error=NotFound";

        if (!StringUtils.hasText(form.getCategoryName())) {
            model.addAttribute("category", form);
            model.addAttribute("isEdit", true);
            model.addAttribute("error", "Category name is required.");
            return "admin/addCategory";
        }

        c.setCategoryName(form.getCategoryName().trim());
        c.setDescription(form.getDescription());
        categoryService.save(c);
        return "redirect:/admin/categories?success=Updated";
    }

    // DELETE (hard delete; nếu muốn soft-delete thì đổi service)
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        categoryService.deleteById(id);
        return "redirect:/admin/categories?success=Deleted";
    }
}
