package fpt.swp.springmvctt.itp.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadImage(MultipartFile file);
    String store(MultipartFile file, String subdir);
}
