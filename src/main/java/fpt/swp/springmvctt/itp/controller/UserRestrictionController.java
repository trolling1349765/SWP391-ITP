package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.entity.UserRestriction;
import fpt.swp.springmvctt.itp.service.UserRestrictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/user-restriction")
public class UserRestrictionController {

    @Autowired
    private UserRestrictionService userRestrictionService;

    @GetMapping
    public String getUserRestrictions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String search,
            Model model) {

        List<UserRestriction> restrictions = userRestrictionService.findByFilter(search, status, fromDate, toDate);

        model.addAttribute("userRestrictions", restrictions);
        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("search", search);
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
    @PutMapping("/edit/{id}")
    public String update(@PathVariable Long id, @ModelAttribute UserRestriction restriction) {
        userRestrictionService.update(id, restriction);
        return "redirect:/admin/user-restriction";
    }

    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        userRestrictionService.delete(id);
        return "redirect:/admin/user-restriction";
    }
}
