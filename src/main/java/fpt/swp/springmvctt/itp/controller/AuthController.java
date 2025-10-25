package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.service.UserService;
import fpt.swp.springmvctt.itp.util.ValidateUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("user", new User());
        return "login/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        @RequestParam(value = "rememberMe", required = false) String rememberMe,
                        HttpSession session,
                        HttpServletResponse response,
                        Model model,
                        RedirectAttributes redirectAttributes) {

        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Email không được để trống!");
            return "login/login";
        }

        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Mật khẩu không được để trống!");
            return "login/login";
        }

        if (!ValidateUtil.isValidEmail(email)) {
            model.addAttribute("error", "Email không hợp lệ!");
            return "login/login";
        }

        var user = userService.login(email, password);
        if (user.isPresent()) {
            session.setAttribute("user", user.get());

            // ✅ Thêm phần GHI NHỚ ĐĂNG NHẬP (Remember Me)
            if (rememberMe != null) { // nếu người dùng có tick chọn checkbox rememberMe
                Cookie cookie = new Cookie("rememberMe", user.get().getEmail());
                cookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
                cookie.setPath("/"); // áp dụng cho toàn bộ app
                response.addCookie(cookie);
            }

            redirectAttributes.addFlashAttribute("success", "Đăng nhập thành công!");
            session.setAttribute("user", user.get());
            session.setAttribute("role", user.get().getRole().getName());
            return "redirect:/home";
        }

        model.addAttribute("error", "Email hoặc mật khẩu không đúng!");
        return "login/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "login/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           BindingResult bindingResult,
                           @RequestParam String confirmPassword,
                           Model model) {

        // Validation
        if (bindingResult.hasErrors()) {
            return "login/register";
        }

        if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email đã được sử dụng!");
            return "login/register";
        }

        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("error", "Tên đăng nhập đã được sử dụng!");
            return "login/register";
        }

        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "login/register";
        }

        if (!ValidateUtil.isValidPassword(user.getPassword())) {
            model.addAttribute("error", "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số!");
            return "login/register";
        }

        try {
            userService.register(user);
            model.addAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "login/login";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi đăng ký: " + e.getMessage());
            return "login/register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "login/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Model model) {
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Email không được để trống!");
            return "login/forgot-password";
        }

        if (!ValidateUtil.isValidEmail(email)) {
            model.addAttribute("error", "Email không hợp lệ!");
            return "login/forgot-password";
        }

        if (!userService.existsByEmail(email)) {
            model.addAttribute("error", "Email không tồn tại trong hệ thống!");
            return "login/forgot-password";
        }

        try {
            userService.sendPasswordResetEmail(email);
            model.addAttribute("success", "Hướng dẫn đặt lại mật khẩu đã được gửi đến email của bạn!");
            return "login/forgot-password";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "login/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        if (userService.isValidPasswordResetToken(token)) {
            model.addAttribute("token", token);
            return "login/reset-password-form";
        } else {
            model.addAttribute("error", "Token không hợp lệ hoặc đã hết hạn");
            return "login/forgot-password";
        }
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("token") String token,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            model.addAttribute("token", token);
            return "login/reset-password-form";
        }

        if (userService.resetPassword(token, newPassword)) {
            model.addAttribute("success", "Đặt lại mật khẩu thành công!");
            return "login/login";
        } else {
            model.addAttribute("error", "Token không hợp lệ hoặc đã hết hạn");
            return "login/forgot-password";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Đăng xuất thành công!");
        return "redirect:/home";
    }
}
