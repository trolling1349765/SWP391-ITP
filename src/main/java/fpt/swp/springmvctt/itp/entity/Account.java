package fpt.swp.springmvctt.itp.entity;

import fpt.swp.springmvctt.itp.entity.enums.Provider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Provider provider;

    @Column(length = 100)
    private String email;

    @Column(name = "access_token", length = 255)
    private String accessToken;

    @Column(name = "refresh_token", length = 255)
    private String refreshToken;

    @Column(length = 20)
    private String phone;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}

