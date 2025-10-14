package fpt.swp.springmvctt.itp.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "shops")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shop extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_name", length = 100, nullable = false)
    private String shopName;

    @Column(length = 255)
    private String email;

    @Column(length = 255 )
    private String phone;//

    @Column(length = 60)
    private String shopCode; // mã shop hiển thị ở UI

    @Column(precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(length = 50)
    private String status; // active or block

    @Column(length = 255, nullable = false)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    // === Relations ===
    // One shop -> many products

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<Product> products;

    @OneToOne(mappedBy = "shop")
    private User user;
}
