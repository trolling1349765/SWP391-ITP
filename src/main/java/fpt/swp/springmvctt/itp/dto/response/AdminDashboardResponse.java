package fpt.swp.springmvctt.itp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AdminDashboardResponse {

    private long totalUsers;
    private long totalShops;
    private long totalOrders;

    // ✅ Thêm field mới
    private long totalProducts;

    // phân bố role → số lượng
    private List<RoleCountResponse> roleStats;

    // khiếu nại demo
    private long totalComplaints;
    private long processingComplaints;
    private long pendingComplaints;
    private List<ComplaintSummaryResponse> complaintDetails;

    // recent
    private List<UserSimpleResponse> recentUsers;
    private List<ShopSimpleResponse> recentShops;

    // revenue theo tháng (1..12) → số tiền
    private Map<Integer, BigDecimal> monthlyRevenue;
}
