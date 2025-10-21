package fpt.swp.springmvctt.itp.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockForm {
    private Long productId;
    private String serial;
    private String code;
    private Integer quantity;
    private BigDecimal faceValue;
    private String infomation;
    private String status;
}
