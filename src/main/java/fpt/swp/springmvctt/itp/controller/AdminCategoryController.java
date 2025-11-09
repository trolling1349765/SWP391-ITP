package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.entity.Category;
import fpt.swp.springmvctt.itp.service.CategoryService;
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

    // LIST
    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size,
                       Model model) {
        Page<Category> pageData = categoryService.search(q, page, size);
        model.addAttribute("pageData", pageData);
        model.addAttribute("q", q);
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
        c.setCategoryName(form.getCategoryName());
        c.setDescription(form.getDescription());
        categoryService.save(c);
        return "redirect:/admin/categories?success=Updated";
    }

    // DELETE
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        categoryService.deleteById(id);
        return "redirect:/admin/categories?success=Deleted";
    }
}
