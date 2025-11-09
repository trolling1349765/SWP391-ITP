package fpt.swp.springmvctt.itp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items",
       indexes = {
           @Index(name = "idx_order_items_order_id", columnList = "order_id"),
           @Index(name = "idx_order_items_product_store_id", columnList = "product_store_id"),
           @Index(name = "idx_order_items_serial_code", columnList = "serial_code")
       })
@Getter @Setter @NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_store_id", nullable = false)
    private Long productStoreId; // Reference to ProductStore

    @Column(name = "serial_code", nullable = false, length = 200)
    private String serialCode;

    @Column(name = "secret_code", length = 255)
    private String secretCode;

    @Column(name = "face_value", precision = 15, scale = 2)
    private java.math.BigDecimal faceValue;

    @Column(name = "information", length = 255)
    private String information;

    // Many order items -> one order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    // Many order items -> one product store
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_store_id", insertable = false, updatable = false)
    private ProductStore productStore;
}

