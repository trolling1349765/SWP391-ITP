package fpt.swp.springmvctt.itp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    // 🧭 Admin Dashboard chính
    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        // → src/main/resources/templates/admin/AdminDashboard.html
        return "admin/AdminDashboard";
    }
}
