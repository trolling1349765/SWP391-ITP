package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import fpt.swp.springmvctt.itp.repository.ShopRepository;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public String users(
            @RequestParam(required = false, defaultValue = "") String username,
            @RequestParam(required = false, defaultValue = "") String email,
            @RequestParam(required = false, defaultValue = "") LocalDate fromDate,
            @RequestParam(required = false, defaultValue = "") LocalDate toDate,
            @RequestParam(required = false, defaultValue = "") LocalDate fromUpdateDate,
            @RequestParam(required = false, defaultValue = "") LocalDate toUpdateDate,
            @RequestParam(required = false, defaultValue = "") String deleted,
            @RequestParam(required = false, defaultValue = "") String deleteBy,
            @RequestParam(required = false, defaultValue = "") String status,
            @RequestParam(required = false, defaultValue = "") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        if (page < 0) {
            model.addAttribute("errorMessage", "Page number can not be negative.");
            page = 0;
        }
        Boolean delete = deleted.isEmpty() ? null : deleted.equals("true");

        Page<User> userpage = userService.findByFilter(
                username,
                email,
                fromDate,
                toDate,
                fromUpdateDate,
                toUpdateDate,
                delete,
                deleteBy,
                status,
                role,
                page,
                size);
        if (userpage.isEmpty() && page >= userpage.getTotalPages()) {
            page = userpage.getTotalPages() - 1;
            userpage = userService.findByFilter(
                    username,
                    email,
                    fromDate,
                    toDate,
                    fromUpdateDate,
                    toUpdateDate,
                    delete,
                    deleteBy,
                    status,
                    role,
                    page,
                    size);
        }
        if (userpage.getContent().isEmpty()) {
            model.addAttribute("errorMessage", "Bad request. No users found.");
            return "redirect:/admin/users";
        }

        model.addAttribute("users", userpage);
        model.addAttribute("currentPage", userpage.getNumber());
        model.addAttribute("totalPages", userpage.getTotalPages());
        model.addAttribute("totalItems", userpage.getTotalElements());

        model.addAttribute("username", username);
        model.addAttribute("email", email);
        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("fromUpdateDate", fromUpdateDate);
        model.addAttribute("toUpdateDate", toUpdateDate);
        model.addAttribute("deleted", deleted);
        model.addAttribute("deleteBy", deleteBy);
        model.addAttribute("role", role);
        return "admin/users";
    }

    @GetMapping("/users/{id}")
    public String user(@PathVariable("id") Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "admin/user";
    }

    @DeleteMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, Model model) {
        userService.delete(id);
        return "redirect:/admin/users";
    }
}
