package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.Shop;
import fpt.swp.springmvctt.itp.repository.ProductRepository;
import fpt.swp.springmvctt.itp.service.ShopService;
import fpt.swp.springmvctt.itp.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class ShopAdminController {

    @Autowired
    ShopService shopService;
    
    @Autowired
    StorageService storageService;
    
    @Autowired
    ProductRepository productRepository;

    @GetMapping("/registerDetail/{id}")
    public String registerDetail(
            Model model,
            @PathVariable Long id
    ) {
        Shop shop = shopService.findById(id);
        
        // Sync images from database to target/classes for immediate display
        if (shop != null) {
            System.out.println("Loading shop ID: " + id);
            System.out.println("Shop name: " + shop.getShopName());
            System.out.println("ImageUrl from DB: " + shop.getImageUrl());
            System.out.println("Image from DB: " + shop.getImage());
            
            if (shop.getImageUrl() != null && !shop.getImageUrl().isEmpty()) {
                System.out.println("   Syncing logo (imageUrl)...");
                storageService.syncShopImagesFromDatabase(shop.getImageUrl());
            } else {
                System.out.println("Shop imageUrl is null or empty");
            }
            
            if (shop.getImage() != null && !shop.getImage().isEmpty()) {
                System.out.println("   Syncing banner (image)...");
                storageService.syncShopImagesFromDatabase(shop.getImage());
            } else {
                System.out.println("Shop image is null or empty");
            }
            
            // Calculate product statistics
            List<Product> products = productRepository.findByShopIdOrderByIdDesc(shop.getId());
            int totalProducts = products.size();
            int inStock = 0;
            int lowStock = 0;
            int outOfStock = 0;
            
            for (Product p : products) {
                int stock = p.getAvailableStock() != null ? p.getAvailableStock() : 0;
                if (stock == 0) {
                    outOfStock++;
                } else if (stock <= 10) {
                    lowStock++;
                } else {
                    inStock++;
                }
            }
            
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("inStock", inStock);
            model.addAttribute("lowStock", lowStock);
            model.addAttribute("outOfStock", outOfStock);
        }
        
        model.addAttribute("shop", shop);
        return "shop/shop-detail";
    }

    @GetMapping("/registers")
    public String showRegister(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String shopName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model
    ) {
        if (page < 0) {
            model.addAttribute("errorMessage", "Page number can not be negative.");
            page = 0;
        }
        // Gọi service để lấy các shop có status "inactive" + điều kiện lọc
        Page<Shop> shops = shopService.filterInactiveShops(shopName, username, fromDate, toDate, page, size);

        if (page >= shops.getTotalPages()) {
            page = Math.max(0, Math.min(page, shops.getTotalPages() - 1));
            model.addAttribute("errorMessage", "Page number too big.");
            shops = shopService.filterInactiveShops(shopName, username, fromDate, toDate, page, size);
        }

        model.addAttribute("currentPage", shops.getNumber());
        model.addAttribute("totalPages", shops.getTotalPages());
        model.addAttribute("totalItems", shops.getTotalElements());

        // Gửi dữ liệu sang view
        model.addAttribute("shops", shops);
        model.addAttribute("shopName", shopName);
        model.addAttribute("username", username);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        return "admin/register";
    }

    @DeleteMapping("/registers/delete/{id}")
    public String deleteRegister(@PathVariable Long id, Model model) {
        shopService.delete(id);
        return "redirect:/admin/registers";
    }

    @GetMapping("/registers/activate/{id}")
    public String activateShop(@PathVariable Long id, Model model) {
        shopService.activateShop(id);
        return "redirect:/admin/registers";
    }

    /**
     * Reject shop mới và unlock luôn để user có thể đăng ký lại
     */
    @PostMapping("/registers/rejectAndUnlock/{id}")
    public String rejectAndUnlock(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Shop shop = shopService.findById(id);
        if (shop != null && (shop.getIsDeleted() == null || !shop.getIsDeleted())) {
            shopService.rejectAndUnlock(id);
            redirectAttributes.addFlashAttribute("success", 
                "Shop đã bị từ chối và user đã được unlock. User có thể đăng ký shop mới.");
        } else {
            redirectAttributes.addFlashAttribute("error", 
                "Chỉ có thể reject shop chưa bị từ chối.");
        }
        return "redirect:/admin/registers";
    }

    @GetMapping("/shops")
    public String shops(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "") String shopName,
            @RequestParam(required = false, defaultValue = "") String createBy,
            @RequestParam(required = false, defaultValue = "") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false, defaultValue = "") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, defaultValue = "") LocalDate fromUpdateDate,
            @RequestParam(required = false, defaultValue = "") LocalDate toUpdateDate,
            @RequestParam(required = false, defaultValue = "") String deleted,
            @RequestParam(required = false, defaultValue = "") String deleteBy,
            @RequestParam(required = false, defaultValue = "") String status,
            Model model
    ) {
        if (page < 0) {
            model.addAttribute("errorMessage", "Page number can not be negative.");
            page = 0;
        }
        // Default: chỉ hiển thị shop chưa bị xóa (deleted = false)
        // Nếu user chọn filter "All" (deleted = "") thì hiển thị tất cả
        // Nếu user chọn "YES" (deleted = "true") thì chỉ hiển thị shop đã bị xóa
        // Nếu user chọn "NO" (deleted = "false") thì chỉ hiển thị shop chưa bị xóa
        Boolean delete = null;
        if (!deleted.isEmpty()) {
            if (deleted.equals("true")) {
                delete = true; // Chỉ hiển thị shop đã bị xóa
            } else if (deleted.equals("false")) {
                delete = false; // Chỉ hiển thị shop chưa bị xóa
            }
            // Nếu deleted = "" hoặc giá trị khác, delete = null (hiển thị tất cả)
        } else {
            // Mặc định: chỉ hiển thị shop chưa bị xóa
            delete = false;
        }

        Page<Shop> shops = shopService.findByFilter(
                shopName,
                createBy,
                fromDate,
                toDate,
                fromUpdateDate,
                toUpdateDate,
                delete,
                deleteBy,
                status,
                page,
                size
        );

        if (page >= shops.getTotalPages()) {
            page = Math.max(0, Math.min(page, shops.getTotalPages() - 1));
            shops = shopService.findByFilter(
                    shopName,
                    createBy,
                    fromDate,
                    toDate,
                    fromUpdateDate,
                    toUpdateDate,
                    delete,
                    deleteBy,
                    status,
                    page,
                    size
            );
        }

        model.addAttribute("currentPage", shops.getNumber());
        model.addAttribute("totalPages", shops.getTotalPages());
        model.addAttribute("totalItems", shops.getTotalElements());

        // Gửi dữ liệu sang view
        model.addAttribute("shops", shops);
        model.addAttribute("shopName", shopName);
        model.addAttribute("createBy", createBy);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("fromUpdateDate", fromUpdateDate);
        model.addAttribute("toUpdateDate", toUpdateDate);
        model.addAttribute("deleted", deleted);
        model.addAttribute("deleteBy", deleteBy);
        model.addAttribute("status", status);

        return "admin/shops";
    }

    @GetMapping("/shops/shopDetail/{id}")
    public String shopDetail(@PathVariable Long id, Model model) {
        Shop shop = shopService.findById(id);
        
        // Sync images from database to target/classes for immediate display
        if (shop != null) {
            System.out.println("Loading shop ID: " + id);
            System.out.println("Shop name: " + shop.getShopName());
            System.out.println("ImageUrl from DB: " + shop.getImageUrl());
            System.out.println("Image from DB: " + shop.getImage());
            
            if (shop.getImageUrl() != null && !shop.getImageUrl().isEmpty()) {
                System.out.println("   Syncing logo (imageUrl)...");
                storageService.syncShopImagesFromDatabase(shop.getImageUrl());
            } else {
                System.out.println("Shop imageUrl is null or empty");
            }
            
            if (shop.getImage() != null && !shop.getImage().isEmpty()) {
                System.out.println("   Syncing banner (image)...");
                storageService.syncShopImagesFromDatabase(shop.getImage());
            } else {
                System.out.println("Shop image is null or empty");
            }
            
            // Calculate product statistics
            List<Product> products = productRepository.findByShopIdOrderByIdDesc(shop.getId());
            int totalProducts = products.size();
            int inStock = 0;
            int lowStock = 0;
            int outOfStock = 0;
            
            for (Product p : products) {
                int stock = p.getAvailableStock() != null ? p.getAvailableStock() : 0;
                if (stock == 0) {
                    outOfStock++;
                } else if (stock <= 10) {
                    lowStock++;
                } else {
                    inStock++;
                }
            }
            
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("inStock", inStock);
            model.addAttribute("lowStock", lowStock);
            model.addAttribute("outOfStock", outOfStock);
        }
        
        model.addAttribute("shop", shop);
        return "shop/shop-detail";
    }

    @DeleteMapping("/shops/delete/{id}")
    public String deleteShop(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        shopService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Shop đã được xóa thành công (soft delete).");
        // Redirect về trang shops với filter deleted=false để không hiển thị shop vừa xóa
        return "redirect:/admin/shops?deleted=false";
    }

    /**
     * Unlock user để cho phép đăng ký shop mới
     * Giữ shop cũ trong DB làm bằng chứng (chỉ xóa relationship)
     * Chỉ dùng cho shop đã bị từ chối (isDeleted = true)
     */
    @PostMapping("/shops/allowReRegistration/{id}")
    public String allowReRegistration(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Shop shop = shopService.findById(id);
        if (shop != null && shop.getIsDeleted() != null && shop.getIsDeleted()) {
            shopService.allowReRegistration(id);
            redirectAttributes.addFlashAttribute("success", 
                "Đã mở khóa user. User có thể đăng ký shop mới. Shop cũ vẫn được giữ lại làm bằng chứng.");
        } else {
            redirectAttributes.addFlashAttribute("error", 
                "Chỉ có thể unlock shop đã bị từ chối (isDeleted = true).");
        }
        return "redirect:/admin/shops?deleted=true";
    }
}
