package fpt.swp.springmvctt.itp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ShopRegistrationForm {
    
    @NotBlank(message = "Tên shop không được để trống")
    @Size(max = 100, message = "Tên shop không được quá 100 ký tự")
    private String shopName;
    
    @Size(max = 500, message = "Mô tả ngắn không được quá 500 ký tự")
    private String shortDescription;
    
    private String description;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "0[0-9]{9}", message = "Số điện thoại phải là 10 số và bắt đầu bằng 0")
    private String phone;
    
    @NotBlank(message = "Vui lòng chọn ít nhất một danh mục")
    private String categories; // Danh sách category được nối bằng dấu phẩy
    
    @Size(max = 500, message = "Link Facebook không được quá 500 ký tự")
    private String fbLink;
    
    // Logo shop (ảnh tròn)
    private MultipartFile logoImage;
    
    // Banner shop (ảnh nền)
    private MultipartFile bannerImage;
    
    @NotNull(message = "Bạn phải đồng ý với điều khoản dịch vụ")
    private Boolean agreeToTerms;
    
}

