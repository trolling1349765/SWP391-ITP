package fpt.swp.springmvctt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "fpt.swp.springmvctt")
@EnableJpaRepositories(basePackages = "fpt.swp.springmvctt.itp.repository")
@EntityScan(basePackages = "fpt.swp.springmvctt.itp.entity")
public class ItpApplication {
    public static void main(String[] args) {
        SpringApplication.run(ItpApplication.class, args);
    }
}
