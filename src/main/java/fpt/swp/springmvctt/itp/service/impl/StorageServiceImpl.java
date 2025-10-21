package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    @Value("${app.assets-dir:src/main/resources/assets/img}")
    private String assetsDir;

    @Override
    public String saveProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            // Create assets directory if not exists
            Files.createDirectories(Path.of(assetsDir));
            
            // Generate unique filename
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String filename = "p_" + System.currentTimeMillis() + (ext == null ? "" : "." + ext.toLowerCase());
            
            // Save file to assets directory
            Path filePath = Path.of(assetsDir, filename).toAbsolutePath().normalize();
            file.transferTo(filePath.toFile());
            
            System.out.println("Image saved to: " + filePath);
            return "/assets/img/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Cannot save file", e);
        }
    }
}
