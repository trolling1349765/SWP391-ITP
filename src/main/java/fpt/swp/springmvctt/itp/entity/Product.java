package fpt.swp.springmvctt.itp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", length = 150, nullable = false)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 15, scale = 2)
    private Double price;

    @Column(name = "available_stock")
    private Integer availableStock;

    @Column(length = 50)
    private String status;

    // Many products -> 1 category
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    // Many products -> 1 shop
    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    // One product -> many product_stores
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductStore> productStores;

    // One product -> many orders
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Order> orders;
}
