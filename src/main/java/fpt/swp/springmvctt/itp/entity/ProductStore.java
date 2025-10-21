package fpt.swp.springmvctt.itp.entity;

import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name="product_stores",
        indexes = {
                @Index(name="idx_ps_product", columnList="product_id"),
                @Index(name="idx_ps_serial",  columnList="serial_code")
        })
@Getter @Setter @NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductStore extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="product_id", nullable=false)
    private Long productId;                 // FK -> products.id

    @Column(name="shop_id")
    private Long shopId;

    @Column(name="serial_code", nullable=false, length=200)
    private String serialCode;              // seri-code

    @Column(name="secret_code", length=255)
    private String secretCode;              // mã như id (tùy chọn)

    // Removed quantity field - each serial code represents 1 item

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private ProductStatus status = ProductStatus.HIDDEN;

    @Column(name="face_value", precision=15, scale=2)
    private BigDecimal faceValue;

    @Column(name="infomation", length=255)
    private String infomation;
}
