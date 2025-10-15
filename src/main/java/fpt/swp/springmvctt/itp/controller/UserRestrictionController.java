package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.entity.UserRestriction;
import fpt.swp.springmvctt.itp.service.UserRestrictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/user-restriction")
public class UserRestrictionController {

    @Autowired
    private UserRestrictionService userRestrictionService;

    @GetMapping
    public String getUserRestrictions(Model model) {
        model.addAttribute("userRestrictions", userRestrictionService.findAll());
        return "admin/restriction-list";
    }

    @GetMapping("/{id}")
    public String getUserRestrictionById(@PathVariable("id") Long id, Model model) {
        UserRestriction userRestriction = userRestrictionService.findById(id);
        model.addAttribute("userRestriction", userRestriction);
        return "admin/restriction-detail";
    }

    // ✅ Thêm mới (POST)
    @PostMapping()
    public String create(@ModelAttribute("newRestriction") UserRestriction restriction) {
        userRestrictionService.save(restriction);
        return "redirect:/admin/user-restriction";
    }

    // ✅ Cập nhật (PUT)
    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute UserRestriction restriction) {
        userRestrictionService.update(id, restriction);
        return "redirect:/admin/user-restriction";
    }

    // ✅ Xoá (DELETE)
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        userRestrictionService.delete(id);
        return "redirect:/admin/user-restriction";
    }
}
