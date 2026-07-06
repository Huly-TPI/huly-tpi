package com.huly.backend.infrastructure.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StoreImageResourceConfig  implements WebMvcConfigurer {

    @Value("${app.uploads.store-dir:uploads/store}")
    private String uploadsDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/store/images/**")
                .addResourceLocations("file:" + uploadsDir + "/");
    }
    
}
