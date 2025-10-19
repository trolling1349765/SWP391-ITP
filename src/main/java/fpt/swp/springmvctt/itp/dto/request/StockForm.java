package fpt.swp.springmvctt.itp.dto.request;

import lombok.Data;

@Data
public class StockForm {
    private Long productId;
    private String serial;
    private String code;
    private Integer quantity;
}
