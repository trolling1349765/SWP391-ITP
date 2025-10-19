package fpt.swp.springmvctt.itp.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    // trả về URL FE dùng: "/assets/img/{filename}"
    String saveProductImage(MultipartFile file);
}
