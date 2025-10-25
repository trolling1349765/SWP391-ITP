package fpt.swp.springmvctt.itp.entity;

import fpt.swp.springmvctt.itp.entity.enums.Provider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(length = 100, nullable = false)
    private String email;

    @Column(name = "access_token", length = 255 , nullable = false)
    private String accessToken;

    @Column(name = "refresh_token", length = 255, nullable = false)
    private String refreshToken;

    @Column(length = 20,  nullable = false)
    private String phone;

    @ManyToOne
    @JoinColumn(name = "user_id" , nullable = false)
    private User user;
}

