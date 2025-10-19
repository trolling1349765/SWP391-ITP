package fpt.swp.springmvctt.itp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    // Many orders -> one product
    @ManyToOne
    @JoinColumn(name = "product_id" )
    private Product product;

//    // Many orders -> one user
//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private User user;
//
//    // One order -> many payment_transactions
//    @OneToOne
//    private PaymentTransaction paymentTransaction;
}
