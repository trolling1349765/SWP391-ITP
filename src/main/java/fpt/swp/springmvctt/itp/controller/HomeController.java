package fpt.swp.springmvctt.itp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/home")
public class HomeController {

    // 🏠 Trang chủ người dùng
//    @GetMapping
//    public String homepage(@RequestParam String message, Model model) {
//        model.addAttribute("message", message);
//        // => src/main/resources/templates/user/Homepage.html
//        return "user/Homepage";
//    }
    @GetMapping()
    public String home(@ModelAttribute("success") String successMessage, Model model) {
        System.out.println("Thông báo: " + successMessage);
        return "user/Homepage";
    }

}
