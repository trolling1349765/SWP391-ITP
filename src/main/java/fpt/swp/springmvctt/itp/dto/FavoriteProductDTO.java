package fpt.swp.springmvctt.itp.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteProductDTO {
    private Long id;
    private Long productId;

    // thông tin sản phẩm
    private String productName;
    private String productImage;
    private BigDecimal price;


    // thông tin danh mục & shop
    private Long categoryId;
    private String shopName;

    // metadata
    private LocalDateTime createdAt;
}
