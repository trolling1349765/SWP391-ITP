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

            System.out.println(" Image saved to: " + filePath);
            System.out.println(" Return path: /assets/img/shop/" + filename);
            return "/assets/img/shop/" + filename;
        } catch (IOException e) {
            System.err.println(" Error saving image: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Cannot save file", e);
        }
    }

    @Override
    public String uploadImage(MultipartFile file) {
        return saveProductImage(file);
    }

    @Override
    public String saveShopLogo(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            // Create shops directory under assets/img/shops
            Path shopsDir = Path.of(assetsDir, "shops");
            Files.createDirectories(shopsDir);

            // Generate unique filename with logo prefix
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String filename = "logo_" + System.currentTimeMillis() + (ext == null ? "" : "." + ext.toLowerCase());

            // Save file to shops directory
            Path filePath = shopsDir.resolve(filename).toAbsolutePath().normalize();
            file.transferTo(filePath.toFile());

            // IMPORTANT: Copy to target/classes for immediate display
            Path targetDir = Path.of("target/classes/assets/img/shops");
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(filename);
            Files.copy(filePath, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✅ Shop logo saved to: " + filePath);
            System.out.println("✅ Copied to target: " + targetFile);
            System.out.println("✅ Return path: /assets/img/shops/" + filename);
            
            return "/assets/img/shops/" + filename;
        } catch (IOException e) {
            System.err.println("❌ Error saving shop logo: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Cannot save shop logo", e);
        }
    }

    @Override
    public String saveShopBanner(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            // Create shops directory under assets/img/shops
            Path shopsDir = Path.of(assetsDir, "shops");
            Files.createDirectories(shopsDir);

            // Generate unique filename with banner prefix
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String filename = "banner_" + System.currentTimeMillis() + (ext == null ? "" : "." + ext.toLowerCase());

            // Save file to shops directory
            Path filePath = shopsDir.resolve(filename).toAbsolutePath().normalize();
            file.transferTo(filePath.toFile());

            // IMPORTANT: Copy to target/classes for immediate display
            Path targetDir = Path.of("target/classes/assets/img/shops");
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(filename);
            Files.copy(filePath, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✅ Shop banner saved to: " + filePath);
            System.out.println("✅ Copied to target: " + targetFile);
            System.out.println("✅ Return path: /assets/img/shops/" + filename);
            
            return "/assets/img/shops/" + filename;
        } catch (IOException e) {
            System.err.println("❌ Error saving shop banner: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Cannot save shop banner", e);
        }
    }

    @Override
    public void syncShopImagesFromDatabase(String imagePath) {
        if (imagePath == null || imagePath.isEmpty() || imagePath.equals("null")) {
            System.out.println("⚠️ Image path is null or empty, skipping sync");
            return;
        }
        
        System.out.println("🔍 Starting sync for image path from DB: " + imagePath);
        
        try {
            // Remove leading slash if present: "/assets/img/shops/logo_xxx.jpg" -> "assets/img/shops/logo_xxx.jpg"
            String cleanPath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
            System.out.println("   Cleaned path (removed leading slash): " + cleanPath);
            
            // Extract relative path from assets/img: "assets/img/shops/logo_xxx.jpg" -> "shops/logo_xxx.jpg"
            String relativePath;
            if (cleanPath.startsWith("assets/img/")) {
                relativePath = cleanPath.substring("assets/img/".length());
            } else if (cleanPath.startsWith("shops/")) {
                relativePath = cleanPath;
            } else {
                // Assume it's already a relative path like "shops/logo_xxx.jpg"
                relativePath = cleanPath;
            }
            System.out.println("   Relative path: " + relativePath);
            
            // Build source path: src/main/resources/assets/img/shops/logo_xxx.jpg
            Path sourceFile = Path.of(assetsDir, relativePath).toAbsolutePath().normalize();
            System.out.println("   Source file path: " + sourceFile);
            System.out.println("   Source exists: " + Files.exists(sourceFile));
            
            // Build target path: target/classes/assets/img/shops/logo_xxx.jpg
            Path targetDir = Path.of("target/classes/assets/img", relativePath).getParent();
            if (targetDir == null) {
                targetDir = Path.of("target/classes/assets/img/shops");
            }
            Files.createDirectories(targetDir);
            System.out.println("   Target directory: " + targetDir);
            
            Path targetFile = targetDir.resolve(sourceFile.getFileName());
            System.out.println("   Target file path: " + targetFile);
            
            // Only copy if source exists and target doesn't exist or is older
            if (Files.exists(sourceFile)) {
                boolean needsCopy = !Files.exists(targetFile);
                if (Files.exists(targetFile)) {
                    long sourceTime = Files.getLastModifiedTime(sourceFile).toMillis();
                    long targetTime = Files.getLastModifiedTime(targetFile).toMillis();
                    needsCopy = sourceTime > targetTime;
                    System.out.println("   Source modified: " + sourceTime + ", Target modified: " + targetTime);
                }
                
                if (needsCopy) {
                    Files.copy(sourceFile, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("✅ Successfully synced image: " + imagePath + " -> " + targetFile);
                } else {
                    System.out.println("ℹ️ Image already synced and up-to-date: " + targetFile);
                }
            } else {
                System.out.println("❌ Source image not found: " + sourceFile);
                System.out.println("   Assets directory: " + assetsDir);
                System.out.println("   Full source path: " + sourceFile.toAbsolutePath());
                
                // Try to find the file in alternative locations
                Path altPath1 = Path.of("src/main/resources", cleanPath);
                Path altPath2 = Path.of("src/main/resources/assets/img", relativePath);
                System.out.println("   Trying alternative path 1: " + altPath1 + " (exists: " + Files.exists(altPath1) + ")");
                System.out.println("   Trying alternative path 2: " + altPath2 + " (exists: " + Files.exists(altPath2) + ")");
            }
        } catch (Exception e) {
            System.err.println("❌ Error syncing image " + imagePath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
