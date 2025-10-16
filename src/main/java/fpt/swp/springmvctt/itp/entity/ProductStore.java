package fpt.swp.springmvctt.itp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_stores")
@Data
@NoArgsConstructor
public class ProductStore extends BaseEntity {

    @Id
    private String id;

    @Column
    private String infomation;

    @Column
    private String hidenInformation;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public ProductStore(String createBy, LocalDateTime createAt, LocalDateTime updateAt,
                        Boolean isDeleted, String deleteBy,
                         String hidenInformation, String infomation,
                        Product product, Shop shop) {
        // BaseEntity phải có constructor tương ứng (LocalDateTime)
        super(createBy, createAt, updateAt, isDeleted, deleteBy);
        this.id = product.getId() + "_" + hidenInformation;
        this.infomation = infomation;
        this.product = product;
    }
}
