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

    // nếu không upload file mới thì giữ đường dẫn này
    private String img;

    // upload ảnh mới
    private MultipartFile file;
}
