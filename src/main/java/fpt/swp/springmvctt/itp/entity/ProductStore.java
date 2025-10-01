package fpt.swp.springmvctt.itp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Entity
@Table(name = "product_stores")
@Data
@NoArgsConstructor
public class ProductStore extends BaseEntity {

    @Id
    private String id;

    @Column
    private String infomation;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    public ProductStore(String createBy, Date createAt, Date updateAt, Boolean isDeleted, String deleteBy, String id, String infomationUnique, String infomation, Product product, Shop shop) {
        super(createBy, createAt, updateAt, isDeleted, deleteBy);
        this.id = product.getId() + "_" + infomationUnique;
        this.infomation = infomation;
        this.product = product;
        this.shop = shop;
    }
}
