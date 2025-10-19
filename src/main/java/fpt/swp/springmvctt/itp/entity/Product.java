package fpt.swp.springmvctt.itp.entity;

import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="shop_id", nullable=false)
    private Long shopId;

    @Column(name="product_name", nullable=false, length=150)
    private String productName;

    @Column(columnDefinition="TEXT")
    private String description;

    @Column(precision=15, scale=2, nullable=false)
    private BigDecimal price;

    @Column(name="category_id")
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private ProductStatus status = ProductStatus.HIDDEN;

    @Column(name="available_stock", nullable=false)
    private Integer availableStock = 0;              // tổng tồn

    @Column(name="image", length=255)
    private String image;                             // /assets/img/xxx
}
