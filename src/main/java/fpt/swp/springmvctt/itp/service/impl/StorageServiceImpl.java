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

    @Value("${app.upload-dir:uploads/assets/img}")
    private String uploadDir;

    @Value("${app.dev-assets-dir:src/main/resources/assets/img}")
    private String devResourcesDir;

    @Override
    public String saveProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            Files.createDirectories(Path.of(uploadDir));
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String filename = "p_" + System.currentTimeMillis() + (ext == null ? "" : "." + ext.toLowerCase());
            Path runtimeDest = Path.of(uploadDir, filename).toAbsolutePath().normalize();
            file.transferTo(runtimeDest.toFile());

            // copy sang resources (dev)
            try {
                Files.createDirectories(Path.of(devResourcesDir));
                Path devDest = Path.of(devResourcesDir, filename).toAbsolutePath().normalize();
                if (!Files.exists(devDest)) Files.copy(runtimeDest, devDest);
            } catch (IOException ignored) { }

            return "/assets/img/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Cannot save file", e);
        }
    }
}
