
package fpt.swp.springmvctt.itp.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockForm {
    private Long productId;
    private String serialCode;
    private String secretCode;
    private String quantity;
    private BigDecimal faceValue;
    private String status;
    private String infomation;
}
