package fpt.swp.springmvctt.itp.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductStoreForm {
    @NotNull
    private Long productId;
    private String serialCode;
    private String secretCode;
    private BigDecimal faceValue;
    @NotNull
    @Min(1)
    private Integer quantity;
    private String infomation;
    private String status = "AVAILABLE";
}
