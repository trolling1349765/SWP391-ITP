package fpt.swp.springmvctt.itp.config;

import fpt.swp.springmvctt.itp.entity.Role;
import fpt.swp.springmvctt.itp.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            // Kiểm tra và tạo role ADMIN
            if (roleRepository.findByName("ADMIN").isEmpty()) {
                Role adminRole = new Role();
                adminRole.setName("ADMIN");
                roleRepository.save(adminRole);
                System.out.println("Created role: ADMIN");
            }

            // Kiểm tra và tạo role CUSTOMER
            if (roleRepository.findByName("CUSTOMER").isEmpty()) {
                Role customerRole = new Role();
                customerRole.setName("CUSTOMER");
                roleRepository.save(customerRole);
                System.out.println("Created role: CUSTOMER");
            }

            // Kiểm tra và tạo role SELLER
            if (roleRepository.findByName("SELLER").isEmpty()) {
                Role sellerRole = new Role();
                sellerRole.setName("SELLER");
                roleRepository.save(sellerRole);
                System.out.println("Created role: SELLER");
            }

            System.out.println("Role initialization completed!");
        };
    }
}
