package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/login")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping()
    public String loginPage() {
        return "user/login";
    }

    @PostMapping()
    public String access(
            @RequestParam(required = true) String username,
            @RequestParam(required = true) String password,
            Model model,
            HttpSession session
    ) {
        User user = userService.Login(username, password);
        if (user != null) {
            session.setAttribute("user", user);
            return "redirect:/home";
        }
            model.addAttribute("error", "invalid username or password");
            return "user/login";
    }
}
