package fpt.swp.springmvctt.itp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/home")
public class HomeController {

    // 🏠 Trang chủ người dùng
    @GetMapping("/home")
    public String homepage() {
        // => src/main/resources/templates/user/Homepage.html
        return "user/Homepage";
    }
}
