package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.dto.request.CheckoutForm;
import fpt.swp.springmvctt.itp.entity.*;
import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;
import fpt.swp.springmvctt.itp.repository.*;
import fpt.swp.springmvctt.itp.service.OrderService;
import fpt.swp.springmvctt.itp.service.ProductService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final ProductStoreRepository productStoreRepository;
    private final ProductService productService;
    private final EntityManager entityManager;
    private final OrderItemRepository orderItemRepository;
    
    // Queue/Lock per product để xử lý concurrent requests
    // Khi có 5 người cùng mua product_id=1, chúng sẽ xếp hàng và xử lý tuần tự
    private final ConcurrentHashMap<Long, ReentrantLock> productLocks = new ConcurrentHashMap<>();

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE) // Highest isolation để tránh race condition
    public Order createOrder(CheckoutForm form, User buyer) {
        Long productId = form.getProductId();
        
        // ============================================================
        // QUEUE MECHANISM: Xếp hàng theo productId
        // ============================================================
        // Khi có 5 người cùng mua product_id=1:
        // - Request 1: acquire lock → process → commit → release lock
        // - Request 2-5: đợi lock → khi lock release, request tiếp theo sẽ acquire
        // => Xử lý tuần tự, đảm bảo thứ tự (FIFO)
        // ============================================================
        ReentrantLock productLock = productLocks.computeIfAbsent(productId, k -> new ReentrantLock(true)); // fair lock = FIFO
        
        System.out.println("🔒 [Product " + productId + "] Request từ user " + buyer.getUsername() + 
                          " - Đang đợi lock... (Queue position: " + (productLock.getQueueLength() + 1) + ")");
        
        productLock.lock(); // Blocking wait - đợi đến lượt
        System.out.println("✅ [Product " + productId + "] User " + buyer.getUsername() + " đã acquire lock");
        
        try {
            // ============================================================
            // CLOSE DATABASE: Lock product ngay từ đầu
            // ============================================================
            // Khi request đầu tiên lock, các request khác sẽ không thấy được
            // product cho đến khi lock được release (sau khi commit)
            // ============================================================
            // 1. Lấy product với lock để tránh race condition
            Product product = entityManager.find(Product.class, productId, LockModeType.PESSIMISTIC_WRITE);
            if (product == null) {
                throw new IllegalArgumentException("Sản phẩm không tồn tại");
            }

            // 2. Kiểm tra product status - chỉ cho mua ACTIVE
            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new IllegalStateException("Sản phẩm không khả dụng. Chỉ có thể mua sản phẩm đang hoạt động.");
            }

            // 3. Kiểm tra số lượng tồn kho (ACTIVE serials)
            long availableCount = productStoreRepository.countByProductIdAndStatus(
                product.getId(), 
                ProductStatus.ACTIVE
            );
            if (availableCount < form.getQuantity()) {
                throw new IllegalStateException("Sản phẩm không đủ số lượng. Tồn kho hiện tại: " + availableCount);
            }

            // 4. Tính tổng tiền
            BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(form.getQuantity()));

            // 5. Kiểm tra balance của buyer với lock
            User buyerWithLock = entityManager.find(User.class, buyer.getId(), LockModeType.PESSIMISTIC_WRITE);
            if (buyerWithLock == null) {
                throw new IllegalArgumentException("Người dùng không tồn tại");
            }
            
            if (buyerWithLock.getBalance().compareTo(totalAmount) < 0) {
                throw new IllegalStateException(
                    "Tài khoản quý khách không đủ tiền. Số dư hiện tại: " + 
                    buyerWithLock.getBalance() + " VND. Cần nạp thêm: " + 
                    totalAmount.subtract(buyerWithLock.getBalance()) + " VND"
                );
            }

            // 6. Lấy seller user từ shop
            Shop shop = shopRepository.findById(product.getShopId())
                    .orElseThrow(() -> new IllegalArgumentException("Shop không tồn tại"));
            
            if (shop.getUser() == null) {
                throw new IllegalStateException("Shop không có người sở hữu");
            }
            
            User seller = shop.getUser();

            // 7. Lấy serial codes với lock TRƯỚC KHI trừ tiền để tránh trừ tiền rồi mới phát hiện hết hàng
            // Query: Lấy các serial codes khác nhau từ product_stores có cùng product_id
            // Ví dụ: product_id = 1, quantity = 2 → Lấy 2 serial codes khác nhau (SERIAL001, SERIAL002)
            System.out.println("🔍 Lấy serial codes cho product_id=" + product.getId() + ", quantity=" + form.getQuantity());
            
            Query serialQuery = entityManager.createQuery(
                "SELECT ps FROM ProductStore ps WHERE ps.productId = :productId AND ps.status = :status ORDER BY ps.id ASC"
            );
            serialQuery.setParameter("productId", product.getId());
            serialQuery.setParameter("status", ProductStatus.ACTIVE);
            serialQuery.setLockMode(LockModeType.PESSIMISTIC_WRITE); // Lock để tránh race condition
            serialQuery.setMaxResults(form.getQuantity()); // Chỉ lấy đúng số lượng cần
            
            @SuppressWarnings("unchecked")
            List<ProductStore> serialsToSell = serialQuery.getResultList();
            
            System.out.println("📦 Tìm thấy " + serialsToSell.size() + " serial codes ACTIVE cho product_id=" + product.getId());
            for (ProductStore ps : serialsToSell) {
                System.out.println("   - Serial Code: " + ps.getSerialCode() + " (ID: " + ps.getId() + ")");
            }
            
            // Kiểm tra số lượng serial codes TRƯỚC KHI trừ tiền
            if (serialsToSell.size() < form.getQuantity()) {
                // Chưa trừ tiền, chỉ cần throw exception
                throw new IllegalStateException(
                    "Xin lỗi, sản phẩm không đủ số lượng. Tồn kho hiện tại: " + serialsToSell.size() + 
                    " sản phẩm. Vui lòng chọn lại số lượng hoặc quay lại sau."
                );
            }

            // 8. Trừ tiền từ buyer (hold tiền) - CHỈ KHI ĐÃ XÁC NHẬN ĐỦ HÀNG
            buyerWithLock.setBalance(buyerWithLock.getBalance().subtract(totalAmount));
            userRepository.save(buyerWithLock);
            System.out.println("💰 Đã trừ tiền từ buyer: " + totalAmount + " VND. Balance còn lại: " + buyerWithLock.getBalance());

            // 9. Tạo order code
            String orderCode = "ORD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // 10. Tạo order với status PENDING
            Order order = new Order();
            order.setProduct(product);
            order.setUser(buyerWithLock);
            order.setQuantity(form.getQuantity());
            order.setUnitPrice(product.getPrice());
            order.setTotalAmount(totalAmount);
            order.setStatus("PENDING"); // Đang chờ xử lý
            order.setOrderCode(orderCode);
            order.setMessageToSeller(form.getMessageToSeller());
            order.setSellerUserId(seller.getId());
            order.setCreateAt(LocalDate.now());
            order.setCreateBy(buyerWithLock.getUsername());
            
            order = orderRepository.save(order);

            // 11. Tạo OrderItem cho mỗi serial code và mark ProductStore as BLOCKED (đã bán)
            // Lưu ý: Nếu có lỗi ở bước này, cần rollback: hoàn tiền và restore stock
            // Mỗi serial code là một record riêng trong product_stores, sẽ được lưu vào order_items
            List<OrderItem> orderItems = new ArrayList<>();
            System.out.println("💾 Tạo OrderItem cho " + serialsToSell.size() + " serial codes...");
            
            try {
                for (ProductStore ps : serialsToSell) {
                    // Mark ProductStore as BLOCKED (đã bán) - không xóa, chỉ đánh dấu
                    ps.setStatus(ProductStatus.BLOCKED);
                    productStoreRepository.save(ps);
                    System.out.println("   ✅ Marked ProductStore ID=" + ps.getId() + " (Serial: " + ps.getSerialCode() + ") as BLOCKED");
                    
                    // Tạo OrderItem để lưu serial code vào order
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrderId(order.getId());
                    orderItem.setProductStoreId(ps.getId());
                    orderItem.setSerialCode(ps.getSerialCode()); // Lưu serial code cụ thể
                    orderItem.setSecretCode(ps.getSecretCode());
                    orderItem.setFaceValue(ps.getFaceValue());
                    orderItem.setInformation(ps.getInfomation());
                    orderItem.setCreateAt(LocalDate.now());
                    orderItem.setCreateBy(buyerWithLock.getUsername());
                    orderItems.add(orderItem);
                    
                    System.out.println("   ✅ Created OrderItem: Serial=" + ps.getSerialCode() + ", Secret=" + 
                        (ps.getSecretCode() != null ? ps.getSecretCode() : "N/A"));
                }
                orderItemRepository.saveAll(orderItems);
                System.out.println("✅ Đã lưu " + orderItems.size() + " OrderItems vào database");

                // 12. Giảm stock của product
                product.setAvailableStock(product.getAvailableStock() - form.getQuantity());
                productRepository.save(product);

                // 13. KHÔNG gọi processOrderAsync ở đây nữa
                // Sẽ được gọi sau khi hold 15s ở OrderController
            } catch (Exception e) {
                // ROLLBACK: Nếu có lỗi khi tạo OrderItem hoặc mark BLOCKED
                // Hoàn tiền cho buyer
                System.err.println("❌ Lỗi khi tạo OrderItem, đang hoàn tiền cho buyer...");
                buyerWithLock.setBalance(buyerWithLock.getBalance().add(totalAmount));
                userRepository.save(buyerWithLock);
                
                // Xóa order đã tạo (nếu có)
                try {
                    orderRepository.delete(order);
                } catch (Exception deleteEx) {
                    System.err.println("⚠️ Không thể xóa order: " + deleteEx.getMessage());
                }
                
                throw new IllegalStateException(
                    "Xin lỗi, có lỗi xảy ra khi xử lý đơn hàng. Tiền đã được hoàn lại vào tài khoản của bạn. " +
                    "Vui lòng thử lại sau hoặc liên hệ hỗ trợ. Lỗi: " + e.getMessage()
                );
            }

            System.out.println("✅ [Product " + productId + "] Order " + order.getOrderCode() + 
                             " đã được tạo thành công. Stock còn lại: " + product.getAvailableStock());
            
            return order;
            
        } finally {
            // ============================================================
            // RELEASE LOCK: Sau khi transaction commit, release lock
            // ============================================================
            // Request tiếp theo trong queue sẽ được xử lý
            // Nếu hết hàng, request tiếp theo sẽ thấy stock = 0 ngay lập tức
            // ============================================================
            productLock.unlock();
            System.out.println("🔓 [Product " + productId + "] Lock đã được release. Request tiếp theo có thể xử lý.");
            
            // Cleanup: Nếu không còn request nào đợi, remove lock khỏi map để giải phóng memory
            if (!productLock.hasQueuedThreads() && productLock.getHoldCount() == 0) {
                productLocks.remove(productId);
            }
        }
    }

    @Override
    @Async("taskExecutor")
    public void processOrderAsync(Long orderId) {
        try {
            System.out.println("🔄 Processing order " + orderId + " - Đang hold tiền trong 20 giây...");
            
            // Đếm ngược 20 giây để hold tiền trước khi chuyển cho seller
            for (int i = 20; i > 0; i--) {
                Thread.sleep(1000); // Sleep 1 giây mỗi lần
                System.out.println("⏱️ [Order " + orderId + "] Đang hold tiền... " + i + " giây còn lại (Tiền sẽ được chuyển cho seller sau khi hết thời gian)");
            }
            
            System.out.println("✅ [Order " + orderId + "] Hết thời gian hold. Đang chuyển tiền cho seller...");
            // Sau 20 giây, chuyển tiền cho seller
            transferToSeller(orderId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ Error processing order async: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error processing order async: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public void transferToSeller(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));

        if (!"PENDING".equals(order.getStatus())) {
            System.out.println("⚠️ Order " + orderId + " không ở trạng thái PENDING, bỏ qua transfer");
            return;
        }

        try {
            // 1. Lấy seller user
            User seller = userRepository.findById(order.getSellerUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Người bán không tồn tại"));

            // 2. Cộng tiền cho seller
            seller.setBalance(seller.getBalance().add(order.getTotalAmount()));
            userRepository.save(seller);

            // 3. Cập nhật order status thành COMPLETED
            order.setStatus("COMPLETED");
            order.setUpdateAt(LocalDate.now());
            orderRepository.save(order);

            // 4. Product và serials đã được mark BLOCKED trong createOrder, không cần xóa
            // Chỉ cần đảm bảo stock được cập nhật đúng
            Product product = order.getProduct();
            if (product != null) {
                // Rebuild stock để đảm bảo chính xác
                long activeCount = productStoreRepository.countByProductIdAndStatus(
                    product.getId(), 
                    ProductStatus.ACTIVE
                );
                product.setAvailableStock((int) activeCount);
                productRepository.save(product);
            }

            System.out.println("✅ Order " + orderId + " đã được xử lý thành công. Tiền đã chuyển cho seller.");
        } catch (Exception e) {
            System.err.println("❌ Error transferring to seller for order " + orderId + ": " + e.getMessage());
            e.printStackTrace();
            
            // Nếu có lỗi, rollback: hoàn tiền cho buyer và đặt status thành FAILED
            try {
                User buyer = order.getUser();
                buyer.setBalance(buyer.getBalance().add(order.getTotalAmount()));
                userRepository.save(buyer);
                
                order.setStatus("FAILED");
                orderRepository.save(order);
                
                // Restore ProductStore status từ BLOCKED về ACTIVE
                List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
                for (OrderItem item : orderItems) {
                    ProductStore ps = productStoreRepository.findById(item.getProductStoreId()).orElse(null);
                    if (ps != null) {
                        ps.setStatus(ProductStatus.ACTIVE);
                        productStoreRepository.save(ps);
                    }
                }
                
                // Restore stock
                Product product = order.getProduct();
                if (product != null) {
                    long activeCount = productStoreRepository.countByProductIdAndStatus(
                        product.getId(), 
                        ProductStatus.ACTIVE
                    );
                    product.setAvailableStock((int) activeCount);
                    productRepository.save(product);
                }
            } catch (Exception rollbackException) {
                System.err.println("❌ Critical: Cannot rollback order " + orderId);
                rollbackException.printStackTrace();
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findOrdersByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderByCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));
    }
}

