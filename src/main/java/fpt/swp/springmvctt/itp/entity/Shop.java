package fpt.swp.springmvctt.itp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name="shops",
        uniqueConstraints = @UniqueConstraint(name="uk_shop_user", columnNames={"user_id"}))
@Getter @Setter @NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Shop extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="shop_name", length=100, nullable=false)
    private String shopName;

    @Column(columnDefinition="TEXT")
    private String description;

    @Column(precision=2, scale=1)
    private BigDecimal rating;

    @Column(length=20)
    private String status;

    @Column(length=255)
    private String category;

    @Column(length=255) private String email;
    @Column(length=20)  private String phone;
    @Column(name="shop_code", length=60) private String shopCode;
    @Column(length=255) private String img;
    @Column(name="image_url", length=500) private String imageUrl;
    @Column(name="image", length=255)     private String image;

    // === Relations ===
    // One shop -> many products (relationship handled via shopId in Product)
    @OneToMany(mappedBy = "shopId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}

