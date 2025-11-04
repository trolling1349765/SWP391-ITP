package fpt.swp.springmvctt.itp.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    // trả về URL FE dùng: "/assets/img/{filename}"
    String saveProductImage(MultipartFile file);
    
    // Upload image method (general)
    String uploadImage(MultipartFile file);
    
    // Save shop logo (avatar) - returns /assets/img/shops/logo_{timestamp}.ext
    String saveShopLogo(MultipartFile file);
    
    // Save shop banner - returns /assets/img/shops/banner_{timestamp}.ext
    String saveShopBanner(MultipartFile file);
    
    // Sync shop images from database paths to target/classes for immediate display
    void syncShopImagesFromDatabase(String imagePath);
}
