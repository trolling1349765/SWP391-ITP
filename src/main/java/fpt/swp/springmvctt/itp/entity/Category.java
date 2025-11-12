package fpt.swp.springmvctt.itp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="categories")
@Getter @Setter @NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Category extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="category_name", length=100, nullable=false)
    private String categoryName;

    @Column(columnDefinition="TEXT")
    private String description;

    @Column(name = "update_by")
    private String updateBy;
}
