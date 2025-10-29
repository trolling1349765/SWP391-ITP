package fpt.swp.springmvctt.itp.entity;

import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;
import fpt.swp.springmvctt.itp.entity.enums.ProductType;
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
    private String description;                // Mô tả ngắn
    @Column(name="detailed_description", columnDefinition="TEXT")
    private String detailedDescription;        // Mô tả chi tiết
    @Column(precision=15, scale=2, nullable=false)
    private BigDecimal price;

    @Column(name="category_id")
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name="product_type", nullable=false, length=20)
    private ProductType productType = ProductType.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private ProductStatus status = ProductStatus.HIDDEN;

    @Column(name="available_stock", nullable=false)
    private Integer availableStock = 0;

    @Column(name="image", length=255)
    private String image;

    // === Relations ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", insertable = false, updatable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;
}
