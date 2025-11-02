package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.dto.request.AdminDashboardRequest;
import fpt.swp.springmvctt.itp.dto.response.AdminDashboardResponse;
import fpt.swp.springmvctt.itp.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, AdminDashboardRequest request) {
        AdminDashboardResponse resp = adminDashboardService.getDashboardData(request);
        model.addAttribute("dashboard", resp);
        return "admin/AdminDashboard";
    }
}
