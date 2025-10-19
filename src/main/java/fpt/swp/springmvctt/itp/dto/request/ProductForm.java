package fpt.swp.springmvctt.itp.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@Data
public class ProductForm {
    private String productName;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    private String img;
    private MultipartFile file;
}
