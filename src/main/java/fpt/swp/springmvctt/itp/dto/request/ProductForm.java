// fpt/swp/springmvctt/itp/dto/request/ProductForm.java
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
    private Integer availableStock;
    private String status;
    private String img;

    private MultipartFile file;
    private String imgUrl;
}
