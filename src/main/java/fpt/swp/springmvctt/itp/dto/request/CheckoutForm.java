package fpt.swp.springmvctt.itp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutForm {
    
    @NotNull(message = "Sản phẩm không được để trống")
    private Long productId;
    
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
    
    private String messageToSeller; // Lời nhắn cho người bán (optional)
}

