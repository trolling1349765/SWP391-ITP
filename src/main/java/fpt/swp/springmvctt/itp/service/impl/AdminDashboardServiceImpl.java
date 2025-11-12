package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.dto.request.AdminDashboardRequest;
import fpt.swp.springmvctt.itp.dto.response.*;
import fpt.swp.springmvctt.itp.entity.Role;
import fpt.swp.springmvctt.itp.entity.Shop;
import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.repository.OrderRepository;
import fpt.swp.springmvctt.itp.repository.ShopRepository;
import fpt.swp.springmvctt.itp.repository.UserRepository;
import fpt.swp.springmvctt.itp.repository.ProductRepository;
import fpt.swp.springmvctt.itp.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository; // ✅ thêm vào

    @Override
    public AdminDashboardResponse getDashboardData(AdminDashboardRequest request) {

        int year = (request != null && request.getYear() != null)
                ? request.getYear()
                : LocalDateTime.now().getYear();

        long totalUsers = userRepository.count();
        long totalShops = shopRepository.count();
        long totalOrders = orderRepository.count();
        long totalProducts = productRepository.countAllProducts(); // ✅ thêm logic đếm sản phẩm

        // ===== 1. thống kê role =====
        List<Object[]> rawRoleCounts = userRepository.countUsersByRole();
        List<RoleCountResponse> roleStats = new ArrayList<>();
        for (Object[] row : rawRoleCounts) {
            String roleName = (String) row[0];
            Long cnt = (Long) row[1];
            roleStats.add(new RoleCountResponse(
                    roleName != null ? roleName : "KHÔNG RÕ",
                    cnt != null ? cnt : 0L
            ));
        }

        // ===== 2. user gần đây =====
        List<User> recentUserEntities = userRepository.findTop10ByOrderByIdDesc();
        List<UserSimpleResponse> recentUsers = new ArrayList<>();
        for (User u : recentUserEntities) {
            LocalDate createDate = u.getCreateAt();
            LocalDateTime createdAt = (createDate != null)
                    ? createDate.atStartOfDay()
                    : null;

            recentUsers.add(new UserSimpleResponse(
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    (u.getRole() != null && u.getRole().getName() != null)
                            ? u.getRole().getName()
                            : "N/A",
                    u.getStatus() != null ? u.getStatus() : "N/A",
                    createdAt
            ));
        }

        // ===== 3. shop gần đây =====
        List<Shop> recentShopEntities = shopRepository.findTop10ByOrderByIdDesc();
        List<ShopSimpleResponse> recentShops = new ArrayList<>();
        for (Shop s : recentShopEntities) {
            LocalDate shopCreateDate = s.getCreateAt();
            LocalDateTime shopCreatedAt = (shopCreateDate != null)
                    ? shopCreateDate.atStartOfDay()
                    : null;

            recentShops.add(new ShopSimpleResponse(
                    s.getId(),
                    s.getShopName(),
                    s.getEmail(),
                    s.getStatus() != null ? s.getStatus().toString() : "N/A",
                    shopCreatedAt
            ));
        }

        // ===== 4. khiếu nại demo =====
        long pending = 3L;
        long processing = 2L;
        List<ComplaintSummaryResponse> complaintDetails = List.of(
                new ComplaintSummaryResponse("Khiếu nại chờ xử lý", pending, "PENDING"),
                new ComplaintSummaryResponse("Khiếu nại đang xử lý", processing, "PROCESSING")
        );

        // ===== 5. trả về kết quả =====
        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalShops(totalShops)
                .totalOrders(totalOrders)
                .totalProducts(totalProducts) // ✅ thêm dòng này
                .roleStats(roleStats)
                .totalComplaints(pending + processing)
                .pendingComplaints(pending)
                .processingComplaints(processing)
                .complaintDetails(complaintDetails)
                .recentUsers(recentUsers)
                .recentShops(recentShops)
                .build();
    }
}
