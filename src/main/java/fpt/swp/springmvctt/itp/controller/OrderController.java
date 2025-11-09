package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.dto.request.CheckoutForm;
import fpt.swp.springmvctt.itp.entity.Order;
import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;
import fpt.swp.springmvctt.itp.repository.OrderItemRepository;
import fpt.swp.springmvctt.itp.repository.ProductStoreRepository;
import fpt.swp.springmvctt.itp.repository.UserRepository;
import fpt.swp.springmvctt.itp.service.OrderService;
import fpt.swp.springmvctt.itp.service.ProductService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;
import java.util.HashMap;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;
    private final OrderItemRepository orderItemRepository;
    private final ProductStoreRepository productStoreRepository;
    private final UserRepository userRepository;

    /**
     * Hiển thị trang checkout
     */
    @GetMapping("/checkout/{productId}")
    public String checkoutPage(@PathVariable Long productId, 
                               @RequestParam(defaultValue = "1") Integer quantity,
                               Model model, 
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        // Kiểm tra đăng nhập (tất cả role đều có thể mua)
        User user = (User) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để mua hàng");
            return "redirect:/login";
        }

        try {
            // Lấy product
            Product product = productService.get(productId);
            
            // Kiểm tra product status
            if (product.getStatus() != fpt.swp.springmvctt.itp.entity.enums.ProductStatus.ACTIVE) {
                redirectAttributes.addFlashAttribute("error", "Sản phẩm không khả dụng");
                return "redirect:/products";
            }

            // Tính tổng tiền
            BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(quantity));

            // Lấy số lượng ACTIVE serials thực tế
            long availableStock = productStoreRepository.countByProductIdAndStatus(
                product.getId(), 
                ProductStatus.ACTIVE
            );

            // Lấy balance của user
            BigDecimal balance = user.getBalance();

            model.addAttribute("product", product);
            model.addAttribute("form", new CheckoutForm());
            model.addAttribute("quantity", quantity);
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("balance", balance);
            model.addAttribute("user", user);
            model.addAttribute("availableStock", availableStock);

            return "order/checkout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm");
            return "redirect:/products";
        }
    }

    /**
     * Xử lý đặt hàng
     */
    @PostMapping("/checkout/submit")
    public String submitOrder(@Valid @ModelAttribute("form") CheckoutForm form,
                              BindingResult bindingResult,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        // Kiểm tra đăng nhập (tất cả role đều có thể mua)
        User user = (User) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để mua hàng");
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin đã nhập");
            return "redirect:/orders/checkout/" + form.getProductId();
        }

        try {
            System.out.println(" Bắt đầu xử lý đơn hàng cho user: " + user.getUsername());
            
            // Tạo order (acquire lock, check stock, deduct money)
            Order order = orderService.createOrder(form, user);
            
            System.out.println(" Order đã được tạo: " + order.getOrderCode() + " (Status: " + order.getStatus() + ")");
            
            // ============================================================
            // Bắt đầu xử lý async (15s hold + 20s hold)
            // ============================================================
            System.out.println(" Bắt đầu xử lý async: Hold 15s (queue, DB) → Hold 20s (money transfer)...");
            orderService.processOrderAsync(order.getId());
            
            // ============================================================
            // Cập nhật balance trong session sau khi mua
            // ============================================================
            User updatedUser = userRepository.findById(user.getId())
                    .orElse(user);
            session.setAttribute("user", updatedUser);
            System.out.println(" Balance đã được cập nhật trong session: " + updatedUser.getBalance());
            System.out.println(" Redirect ngay về /orders/history. Frontend sẽ poll để cập nhật trạng thái real-time.");
            
            redirectAttributes.addFlashAttribute("success", 
                "Đặt hàng thành công! Mã đơn hàng: " + order.getOrderCode() + 
                ".Đơn hàng đang được xử lý, tiền sẽ được chuyển cho người bán.");
            
            return "redirect:/orders/history";
        } catch (IllegalStateException e) {
            // Lỗi về balance, stock, hoặc đã hoàn tiền
            String errorMessage = e.getMessage();
            
            // Reload user để đảm bảo balance được cập nhật nếu đã hoàn tiền
            User reloadedUser = userRepository.findById(user.getId()).orElse(user);
            session.setAttribute("user", reloadedUser);
            
            // Kiểm tra xem có phải lỗi do hoàn tiền không
            if (errorMessage != null && errorMessage.contains("hoàn lại")) {
                redirectAttributes.addFlashAttribute("error", errorMessage);
                redirectAttributes.addFlashAttribute("refunded", true);
            } else {
                redirectAttributes.addFlashAttribute("error", errorMessage);
            }
            
            return "redirect:/orders/checkout/" + form.getProductId() + "?quantity=" + form.getQuantity();
        } catch (Exception e) {
            // Reload user để đảm bảo balance được cập nhật
            User reloadedUser = userRepository.findById(user.getId()).orElse(user);
            session.setAttribute("user", reloadedUser);
            
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/orders/checkout/" + form.getProductId() + "?quantity=" + form.getQuantity();
        }
    }

    /**
     * Lịch sử mua hàng
     */
    @GetMapping("/history")
    public String orderHistory(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để xem lịch sử mua hàng");
            return "redirect:/login";
        }

        try {
            // ============================================================
            // FIX: Reload user từ database để lấy balance mới nhất
            // ============================================================
            User updatedUser = userRepository.findById(user.getId())
                    .orElse(user);
            session.setAttribute("user", updatedUser); // Cập nhật session
            
            List<Order> orders = orderService.getOrdersByUserId(user.getId());
            model.addAttribute("orders", orders);
            model.addAttribute("user", updatedUser); // Dùng user đã reload
            return "order/history";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * API endpoint để lấy trạng thái orders cho polling
     * Luôn reload từ database để lấy status mới nhất
     */
    @GetMapping("/api/status")
    @ResponseBody
    public Map<String, Object> getOrderStatuses(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> orderStatusMap = new HashMap<>();
        
        User user = (User) session.getAttribute("user");
        if (user != null) {
            try {
                // Reload từ database để đảm bảo lấy status mới nhất
                List<Order> orders = orderService.getOrdersByUserId(user.getId());
                System.out.println("📡 [API] Lấy trạng thái cho " + orders.size() + " đơn hàng của user " + user.getId());
                for (Order order : orders) {
                    String status = order.getStatus();
                    orderStatusMap.put(order.getId().toString(), status);
                    System.out.println("  - Order " + order.getId() + " (" + order.getOrderCode() + "): " + status);
                }
                response.put("success", true);
                response.put("orders", orderStatusMap);
            } catch (Exception e) {
                System.err.println("❌ [API] Lỗi khi lấy trạng thái đơn hàng: " + e.getMessage());
                e.printStackTrace();
                response.put("success", false);
                response.put("error", e.getMessage());
            }
        } else {
            response.put("success", false);
            response.put("error", "User not logged in");
        }
        
        return response;
    }

    /**
     * Chi tiết đơn hàng
     */
    @GetMapping("/detail/{orderId}")
    public String orderDetail(@PathVariable Long orderId, 
                              Model model, 
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập");
            return "redirect:/login";
        }

        try {
            Order order = orderService.getOrderById(orderId);
            
            // Kiểm tra quyền truy cập
            if (!order.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xem đơn hàng này");
                return "redirect:/orders/history";
            }

            // Lấy danh sách serial codes đã mua
            List<fpt.swp.springmvctt.itp.entity.OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
            model.addAttribute("order", order);
            model.addAttribute("orderItems", orderItems);
            return "order/detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng");
            return "redirect:/orders/history";
        }
    }
}

