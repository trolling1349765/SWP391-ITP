package fpt.swp.springmvctt.itp.dto.request;

import fpt.swp.springmvctt.itp.entity.enums.ProductType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@Data
public class ProductForm {
    private String productName;
    private String description;                // Mô tả ngắn
    private String detailedDescription;        // Mô tả chi tiết
    private BigDecimal price;
    private Long categoryId;
    private ProductType productType;           // Loại sản phẩm (VIETTEL, EMAIL, etc.)
    private BigDecimal faceValue;              // Mệnh giá (cho thẻ điện thoại)
    private String img;
    private MultipartFile file;
    private MultipartFile serialFile;          // File Excel chứa serial và số lượng
}
