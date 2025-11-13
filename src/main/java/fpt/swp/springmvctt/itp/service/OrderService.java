package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.dto.request.CheckoutForm;
import fpt.swp.springmvctt.itp.entity.Order;
import fpt.swp.springmvctt.itp.entity.User;

import java.util.List;

public interface OrderService {
    
    /**
     * Tạo đơn hàng mới (chờ xử lý)
     * Kiểm tra balance, hold tiền, tạo order với status "PENDING"
     */
    Order createOrder(CheckoutForm form, User buyer);
    
    /**
     * Lấy danh sách đơn hàng của user
     */
    List<Order> getOrdersByUserId(Long userId);
    
    /**
     * Lấy danh sách đơn hàng đã bán của shop (theo sellerUserId)
     */
    List<Order> getOrdersBySellerUserId(Long sellerUserId);
    
    /**
     * Lấy đơn hàng theo ID
     */
    Order getOrderById(Long orderId);
    
    /**
     * Lấy đơn hàng theo order code
     */
    Order getOrderByCode(String orderCode);
    
    /**
     * Xử lý đơn hàng: hold tiền 20s, sau đó transfer cho seller
     * Chạy async để không block request
     */
    void processOrderAsync(Long orderId);
    
    /**
     * Transfer tiền cho seller sau khi hold thành công
     */
    void transferToSeller(Long orderId);
}

