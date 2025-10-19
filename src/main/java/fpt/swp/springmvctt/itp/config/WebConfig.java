package fpt.swp.springmvctt.itp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${app.upload-dir:uploads/assets/img}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Runtime uploaded images
        String abs = Path.of(uploadDir).toAbsolutePath().normalize().toString().replace("\\","/");
        registry.addResourceHandler("/assets/img/**")
                .addResourceLocations("file:" + abs + "/")
                .addResourceLocations("classpath:/assets/img/");
    }
}
