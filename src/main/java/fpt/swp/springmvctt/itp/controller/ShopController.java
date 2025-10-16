package fpt.swp.springmvctt.itp.controller;
import fpt.swp.springmvctt.itp.entity.Shop;
import fpt.swp.springmvctt.itp.repository.ShopRepository;
import fpt.swp.springmvctt.itp.service.StorageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/shop")
public class ShopController {
    private final ShopRepository shopRepository;
    private final StorageService storageService;

    public ShopController(ShopRepository shopRepository, StorageService storageService) {
        this.shopRepository = shopRepository;
        this.storageService = storageService;
    }

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

        return "shop/ShopDetails";
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
