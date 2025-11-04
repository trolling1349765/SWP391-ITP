package fpt.swp.springmvctt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication(scanBasePackages = "fpt.swp.springmvctt")
//@EnableJpaRepositories(basePackages = "fpt.swp.springmvctt.itp.repository")
//@EntityScan(basePackages ="fpt.swp.springmvctt.itp.entity")
@SpringBootApplication
public class ItpApplication {
    public static void main(String[] args) {
        SpringApplication.run(ItpApplication.class, args);
    }
}
