package fpt.swp.springmvctt.itp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ShopSimpleResponse {
    private Long id;
    private String shopName;
    private String email;
    private String status;
    private LocalDateTime createAt;
}
