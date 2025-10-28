package fpt.swp.springmvctt.itp.config;
import fpt.swp.springmvctt.itp.filter.AdminFilter;
import fpt.swp.springmvctt.itp.filter.SellerFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/img/**")
                .addResourceLocations("classpath:/assets/img/");

        registry.addResourceHandler("/assets/ajax/**")
                .addResourceLocations("classpath:/assets/ajax/");
        registry.addResourceHandler("/assets/css/**")
                .addResourceLocations("classpath:/assets/css/");
    }

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }
    @Bean
    public FilterRegistrationBean<AdminFilter> adminFilter() {
        FilterRegistrationBean<AdminFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AdminFilter());
        registration.setUrlPatterns(Arrays.asList("/admin/*", "/shop/registers"));
        registration.setOrder(1); // thứ tự ưu tiên
        return registration;
    }

    @Bean
    public FilterRegistrationBean<SellerFilter> sellerFilter() {
        FilterRegistrationBean<SellerFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SellerFilter());
        registration.setUrlPatterns(Arrays.asList("/shop/*"));
        registration.setOrder(2); // thứ tự ưu tiên
        return registration;
    }
}