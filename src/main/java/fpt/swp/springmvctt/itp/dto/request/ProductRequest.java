package fpt.swp.springmvctt.itp.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Product name max 150 chars")
    private String productName;

    @Size(max = 10_000, message = "Description too long")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be >= 0")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock must be >= 0")
    private Integer availableStock;

    @NotBlank(message = "Status is required") // ví dụ: ACTIVE/HIDDEN
    private String status;

    @Size(max = 500, message = "Image URL too long")
    private String image;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Shop is required")
    private Long shopId;
}
