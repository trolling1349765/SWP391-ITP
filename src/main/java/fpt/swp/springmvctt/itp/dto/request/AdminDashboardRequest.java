package fpt.swp.springmvctt.itp.dto.request;

import lombok.Data;

/**
 * Dùng để filter dashboard (tương lai).
 * Giờ để trống cũng được.
 */
@Data
public class AdminDashboardRequest {
    private Integer year;   // để tính revenue theo năm
    private Integer month;  // nếu sau này muốn filter theo tháng
}
