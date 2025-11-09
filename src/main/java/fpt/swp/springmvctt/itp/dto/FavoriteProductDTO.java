package fpt.swp.springmvctt.itp.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteProductDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private LocalDateTime createdAt;
}
