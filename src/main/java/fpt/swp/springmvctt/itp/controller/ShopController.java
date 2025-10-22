package fpt.swp.springmvctt.itp.controller;
import fpt.swp.springmvctt.itp.entity.Shop;
import fpt.swp.springmvctt.itp.repository.ShopRepository;
import fpt.swp.springmvctt.itp.service.ShopService;
import fpt.swp.springmvctt.itp.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Controller
@RequestMapping("/shop")
public class ShopController {
    private final ShopRepository shopRepository;
    private final StorageService storageService;

    @Autowired
    private ShopService shopService;

    public ShopController(ShopRepository shopRepository, StorageService storageService) {
        this.shopRepository = shopRepository;
        this.storageService = storageService;
    }
    //

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

    //
    @GetMapping
    public String viewShop(Model model) {
        Shop shop = new Shop();
        shop.setShopName("EverGift Store");
        shop.setShopCode("EV001");
        shop.setCategory("Gifts & Flowers");
        shop.setStatus("active");
        shop.setEmail("evergift@gmail.com");
        shop.setPhone("0123456789");
        shop.setDescription("This is a demo shop for testing UI.");

        model.addAttribute("shop", shop);
        model.addAttribute("totalProducts", 120);
        model.addAttribute("inStock", 100);
        model.addAttribute("lowStock", 10);
        model.addAttribute("outOfStock", 10);

        return "shop-detail";
    }



    @PostMapping("/upload-image")
    public String uploadShopImage(@RequestParam("file") MultipartFile file,
                                  @RequestParam("shopId") Long shopId) {
        String imageUrl = storageService.uploadImage(file);
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        shop.setImage(imageUrl);
        shopRepository.save(shop);
        return "redirect:/shop/details/" + shopId;
    }
}
