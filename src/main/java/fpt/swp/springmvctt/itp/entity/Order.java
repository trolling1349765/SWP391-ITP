package fpt.swp.springmvctt.itp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "orders",
       indexes = {
           @Index(name = "idx_orders_order_code", columnList = "order_code"),
           @Index(name = "idx_orders_user_id", columnList = "user_id"),
           @Index(name = "idx_orders_product_id", columnList = "product_id"),
           @Index(name = "idx_orders_seller_user_id", columnList = "seller_user_id"),
           @Index(name = "idx_orders_status", columnList = "status")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Integer quantity;

    @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(length = 50, nullable = false)
    private String status;

    @Column(name = "order_code", unique = true, length = 20)
    private String orderCode; // Mã đơn hàng duy nhất

    @Column(name = "message_to_seller", columnDefinition = "TEXT")
    private String messageToSeller; // Lời nhắn cho người bán

    @Column(name = "seller_user_id")
    private Long sellerUserId; // ID của người bán (shop owner)

    // Many orders -> one product
    @ManyToOne
    @JoinColumn(name = "product_id" )
    private Product product;

    // Many orders -> one user
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // One order -> many payment_transactions
    @OneToOne
    private PaymentTransaction paymentTransaction;
}
