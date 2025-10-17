package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.entity.UserRestriction;
import fpt.swp.springmvctt.itp.service.UserRestrictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
            @RequestParam(required = false) String deleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<UserRestriction> restrictionPage = userRestrictionService.findByFilter(search, status, fromDate, toDate, deleted, page, size);

        model.addAttribute("userRestrictions", restrictionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", restrictionPage.getTotalPages());
        model.addAttribute("totalItems", restrictionPage.getTotalElements());

        // giữ lại giá trị filter
        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("deleted", deleted);
        // giữ lại giá trị search
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
