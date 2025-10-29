package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.entity.Shop;
import fpt.swp.springmvctt.itp.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
public class ShopAdminController {

    @Autowired
    ShopService shopService;

    @GetMapping("/registerDetail/{id}")
    public String registerDetail(
            Model model,
            @PathVariable Long id
    ) {
        Shop shop = shopService.findById(id);
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
        // Gọi service để lấy các shop có status "inactive" + điều kiện lọc
        Page<Shop> shops = shopService.filterInactiveShops(shopName, username, fromDate, toDate, page, size);

        if (page < 0) {
            model.addAttribute("errorMessage", "Page number can not be negative.");
            page = 0;
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
    ){
        if (page < 0) {
            model.addAttribute("errorMessage", "Page number can not be negative.");
            page = 0;
        }
        Boolean delete = deleted.isEmpty() ? null : deleted.equals("true");

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
        model.addAttribute("shop", shop);
        return "shop/shop-homepage";
    }

    @DeleteMapping("/shops/delete/{id}")
    public String deleteShop(@PathVariable Long id, Model model) {
        shopService.delete(id);
        return "redirect:/admin/shops";
    }
}
