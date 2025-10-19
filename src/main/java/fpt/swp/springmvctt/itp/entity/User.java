package fpt.swp.springmvctt.itp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name="users",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_users_shop",     columnNames={"shop_id"}),
                @UniqueConstraint(name="uk_users_username", columnNames={"username"})
        })
@Getter @Setter @NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length=50, nullable=false) private String username;
    @Column(length=100) private String email;
    @Column private String password;
    @Column(length=255) private String status;
    @Column(length=20)  private String phone;
    @Column(precision=19, scale=2) private BigDecimal balance;
    @Column(name="role_id") private Long roleId;
    @Column(name="user_restriction_id") private Long userRestrictionId;
    @Column(name="shop_id") private Long shopId; // 1–1 với shop
}
