package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.entity.UserRestriction;
import fpt.swp.springmvctt.itp.service.UserRestrictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
