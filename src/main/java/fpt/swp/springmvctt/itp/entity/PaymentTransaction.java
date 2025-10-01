package fpt.swp.springmvctt.itp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String type;

    @Column(precision = 15, scale = 2)
    private Double amount;

    @Column(length = 50)
    private String status;

    @OneToOne
    private Order order;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}

