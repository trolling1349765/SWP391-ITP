package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageServiceImpl implements StorageService {

    private final Path uploadRoot;

    public StorageServiceImpl(@Value("${app.upload.root:uploads}") String uploadRoot) {
        this.uploadRoot = Paths.get(uploadRoot).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadRoot, e);
        }
    }

    @Override
    public String uploadImage(MultipartFile file) {
        return store(file, "products");
    }

    @Override
    public String store(MultipartFile file, String subdir) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File rỗng hoặc không tồn tại");
            }


            Path targetDir = uploadRoot;
            if (subdir != null && !subdir.isBlank()) {
                targetDir = uploadRoot.resolve(subdir);
                Files.createDirectories(targetDir);
            }

            // Tạo tên file unique: UUID + extension
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;


            Path targetPath = targetDir.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);


            String relativePath = uploadRoot.relativize(targetPath).toString().replace("\\", "/");
            System.out.println("✅ Uploaded image: " + relativePath);

            return relativePath;

        } catch (IOException e) {
            throw new RuntimeException("❌ Upload failed: " + e.getMessage(), e);
        }
    }
}
