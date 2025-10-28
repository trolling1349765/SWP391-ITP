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
            // Create shop directory under assets/img/shop
            Path shopDir = Path.of(assetsDir, "shop");
            Files.createDirectories(shopDir);

            // Generate unique filename
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String filename = "p_" + System.currentTimeMillis() + (ext == null ? "" : "." + ext.toLowerCase());

            // Save file to shop directory
            Path filePath = shopDir.resolve(filename).toAbsolutePath().normalize();
            file.transferTo(filePath.toFile());

            System.out.println("✅ Image saved to: " + filePath);
            System.out.println("✅ Return path: /assets/img/shop/" + filename);
            return "/assets/img/shop/" + filename;
        } catch (IOException e) {
            System.err.println("❌ Error saving image: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Cannot save file", e);
        }
    }

    @Override
    public String uploadImage(MultipartFile file) {
        return saveProductImage(file);
    }
}
