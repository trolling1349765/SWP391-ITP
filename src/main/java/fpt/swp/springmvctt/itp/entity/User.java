package fpt.swp.springmvctt.itp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String  username;

    @Column(unique = true, nullable = false, length = 100)
    private String  email;

    @Column(nullable = false, length = 255)
    private String  password;

    @Column(length = 20)
    private String  Phone;

    @Column()
    private double  balance;

    @Column()
    private String  status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")   // khóa ngoại role_id trong bảng users
    private Role role;

    @OneToOne
    private Shop shop;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ChatMessage> chatMessages;

    @OneToOne
    private UserRestriction userRestriction;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Account> accounts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private  List<PaymentTransaction> paymentTransactions;
}
