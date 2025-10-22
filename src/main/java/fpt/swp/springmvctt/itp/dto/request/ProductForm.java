package fpt.swp.springmvctt.itp.dto.request;

import fpt.swp.springmvctt.itp.entity.enums.ProductType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class ProductForm {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được quá 255 ký tự")
    private String productName;
    
    @Size(max = 1000, message = "Mô tả ngắn không được quá 1000 ký tự")
    private String description;                // Mô tả ngắn
    
    @Size(max = 2000, message = "Mô tả chi tiết không được quá 2000 ký tự")
    private String detailedDescription;        // Mô tả chi tiết
    
    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.01", message = "Giá sản phẩm phải lớn hơn 0")
    @DecimalMax(value = "999999999.99", message = "Giá sản phẩm không được quá 999,999,999.99")
    private BigDecimal price;
    
    @NotNull(message = "Vui lòng chọn phân loại sản phẩm")
    private Long categoryId;
    
    @NotNull(message = "Vui lòng chọn kiểu sản phẩm")
    private ProductType productType;           // Loại sản phẩm (VIETTEL, EMAIL, etc.)
    
    @DecimalMin(value = "0.01", message = "Mệnh giá phải lớn hơn 0")
    private BigDecimal faceValue;              // Mệnh giá (cho thẻ điện thoại - tùy chọn)
    
    private String img;
    
    private MultipartFile file;
    
    @NotNull(message = "Vui lòng upload file Excel chứa danh sách sản phẩm")
    private MultipartFile serialFile;          // File Excel chứa serial và số lượng
}
