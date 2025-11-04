package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.dto.request.ShopRegistrationForm;
import fpt.swp.springmvctt.itp.entity.Category;
import fpt.swp.springmvctt.itp.entity.Shop;
import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.service.CategoryService;
import fpt.swp.springmvctt.itp.service.ShopService;
import fpt.swp.springmvctt.itp.service.StorageService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopRegisterController {

    private final ShopService shopService;
    private final CategoryService categoryService;
    private final StorageService storageService;

    /**
     * Hiển thị form đăng ký shop
     */
    @GetMapping("/register")
    public String showRegisterForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        
        // Kiểm tra đã đăng nhập chưa
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để đăng ký shop");
            return "redirect:/login";
        }

        // Kiểm tra user đã có shop chưa
        if (user.getShop() != null) {
            redirectAttributes.addFlashAttribute("error", "Bạn đã có shop rồi!");
            return "redirect:/shop/dashboard";
        }

        // Lấy danh sách categories
        List<Category> categories = categoryService.findAll();
        model.addAttribute("allCategories", categories);
        model.addAttribute("form", new ShopRegistrationForm());
        model.addAttribute("user", user);
        
        return "shop/shop-register";
    }

    /**
     * Xử lý đăng ký shop
     */
    @PostMapping("/register")
    public String registerShop(
            @Valid @ModelAttribute("form") ShopRegistrationForm form,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        
        // Kiểm tra đã đăng nhập
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để đăng ký shop");
            return "redirect:/login";
        }

        // Kiểm tra validation
        if (bindingResult.hasErrors()) {
            List<Category> categories = categoryService.findAll();
            model.addAttribute("allCategories", categories);
            model.addAttribute("user", user);
            return "shop/shop-register";
            
        }

        // Kiểm tra đồng ý điều khoản
        if (form.getAgreeToTerms() == null || !form.getAgreeToTerms()) {
            bindingResult.rejectValue("agreeToTerms", "error.agreeToTerms", 
                "Bạn phải đồng ý với điều khoản dịch vụ");
            List<Category> categories = categoryService.findAll();
            model.addAttribute("allCategories", categories);
            model.addAttribute("user", user);
            return "shop/shop-register";
        }

        try {
            // ===== BACKEND VALIDATION =====
            
            // Validate image size (giống shop-profile)
            if (form.getLogoImage() != null && !form.getLogoImage().isEmpty()) {
                if (form.getLogoImage().getSize() > 5 * 1024 * 1024) { // 5MB
                    model.addAttribute("error", "Logo không được vượt quá 5MB");
                    List<Category> categories = categoryService.findAll();
                    model.addAttribute("allCategories", categories);
                    model.addAttribute("user", user);
                    return "shop/shop-register";
                }
            }
            
            if (form.getBannerImage() != null && !form.getBannerImage().isEmpty()) {
                if (form.getBannerImage().getSize() > 5 * 1024 * 1024) { // 5MB
                    model.addAttribute("error", "Ảnh bìa không được vượt quá 5MB");
                    List<Category> categories = categoryService.findAll();
                    model.addAttribute("allCategories", categories);
                    model.addAttribute("user", user);
                    return "shop/shop-register";
                }
            }
            
            // ===== CREATE SHOP =====
            
            // Tạo shop mới với status INACTIVE (chờ admin duyệt)
            Shop shop = new Shop();
            shop.setShopName(form.getShopName().trim());
            shop.setShortDescription(form.getShortDescription() != null ? form.getShortDescription().trim() : null);
            shop.setDescription(form.getDescription() != null ? form.getDescription().trim() : null);
            shop.setPhone(form.getPhone().trim());
            shop.setEmail(user.getEmail().trim().toLowerCase()); // Lấy email từ user đang đăng nhập
            shop.setCategory(form.getCategories().trim());
            shop.setFacebookLink(form.getFbLink() != null ? form.getFbLink().trim() : null);
            shop.setStatus("INACTIVE"); // Chờ admin duyệt
            shop.setUser(user);
            shop.setCreateAt(LocalDate.now());
            shop.setCreateBy(user.getUsername());
            
            // Generate shop code
            String shopCode = "SH" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            shop.setShopCode(shopCode);

            // Upload logo nếu có (lưu local + copy to target)
            if (form.getLogoImage() != null && !form.getLogoImage().isEmpty()) {
                try {
                    String logoUrl = storageService.saveShopLogo(form.getLogoImage());
                    shop.setImageUrl(logoUrl); // Logo shop (avatar)
                    System.out.println("✅ Logo uploaded: " + logoUrl);
                } catch (Exception e) {
                    System.err.println("❌ Error uploading logo: " + e.getMessage());
                    model.addAttribute("error", "Lỗi khi upload logo: " + e.getMessage());
                    List<Category> categories = categoryService.findAll();
                    model.addAttribute("allCategories", categories);
                    model.addAttribute("user", user);
                    return "shop/shop-register";
                }
            }

            // Upload banner nếu có (lưu local + copy to target)
            if (form.getBannerImage() != null && !form.getBannerImage().isEmpty()) {
                try {
                    String bannerUrl = storageService.saveShopBanner(form.getBannerImage());
                    shop.setImage(bannerUrl); // Banner shop
                    System.out.println("✅ Banner uploaded: " + bannerUrl);
                } catch (Exception e) {
                    System.err.println("❌ Error uploading banner: " + e.getMessage());
                    model.addAttribute("error", "Lỗi khi upload banner: " + e.getMessage());
                    List<Category> categories = categoryService.findAll();
                    model.addAttribute("allCategories", categories);
                    model.addAttribute("user", user);
                    return "shop/shop-register";
                }
            }

            // Lưu shop
            shopService.save(shop);
            
            System.out.println("✅ Shop registered successfully: " + shop.getShopName() + " (ID: " + shop.getId() + ")");

            redirectAttributes.addFlashAttribute("success", 
                "Đăng ký shop thành công! Vui lòng chờ admin phê duyệt.");
            return "redirect:/";

        } catch (Exception e) {
            System.err.println("❌ Error registering shop: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            List<Category> categories = categoryService.findAll();
            model.addAttribute("allCategories", categories);
            model.addAttribute("user", user);
            return "shop/shop-register";
        }
    }

    /**
     * Hủy đăng ký
     */
    @GetMapping("/register/cancel")
    public String cancelRegister(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("info", "Đã hủy đăng ký shop");
        return "redirect:/";
    }
}

