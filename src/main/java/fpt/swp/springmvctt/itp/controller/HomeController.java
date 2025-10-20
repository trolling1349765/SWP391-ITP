package fpt.swp.springmvctt.itp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    // 🏠 Trang chủ người dùng
    @GetMapping("/home")
    public String homepage() {
        // => src/main/resources/templates/user/Homepage.html
        return "user/Homepage";
    }

    // 🧩 Header fragment (để test riêng nếu cần)
    @GetMapping("/header")
    public String header() {
        // => src/main/resources/templates/Included/Header.html
        return "Included/Header";
    }

    // 🧩 Footer fragment (để test riêng nếu cần)
    @GetMapping("/footer")
    public String footer() {
        // => src/main/resources/templates/Included/Footer.html
        return "Included/Footer";
    }

    // 🧩 Shop Sidebar (nếu bạn muốn kiểm tra riêng)
    @GetMapping("/shop-sidebar")
    public String shopSidebar() {
        // => src/main/resources/templates/Included/ShopSidebar.html
        return "Included/ShopSidebar";
    }

    // 🧩 Admin Sidebar (nếu bạn muốn kiểm tra riêng)
    @GetMapping("/admin-sidebar")
    public String adminSidebar() {
        return "admin/admin-sidebar";
    }
}
