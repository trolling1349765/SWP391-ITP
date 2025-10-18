package fpt.swp.springmvctt.itp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1) Tài nguyên tĩnh trong classpath (CSS/JS/ảnh sẵn có)
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/assets/");


        String uploadRoot = Paths.get("uploads").toAbsolutePath().toString().replace("\\", "/");
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadRoot + "/");
    }
}
