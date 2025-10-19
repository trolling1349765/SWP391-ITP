package fpt.swp.springmvctt.itp.dto.request;

import lombok.Data;

@Data
public class StockForm {
    private Long productId;
    private String serial;   // map -> product_stores.serial_code
    private String code;     // map -> product_stores.secret_code (mã như id)
    private Integer quantity; // >= 0 (DB là int)
}
