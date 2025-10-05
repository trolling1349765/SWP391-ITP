package fpt.swp.springmvctt.itp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import  java.time.LocalDateTime;


import java.sql.Date;

@Entity
@Table(name = "vnpay_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VNPayTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_no", length = 100)
    private String transactionNo;

    @Column(name = "bank_code", length = 50)
    private String bankCode;

    @Column(precision = 15, scale = 2 , nullable = false )
    private BigDecimal amount;

    @Column(name = "pay_date" )
    private LocalDateTime payDate;

    @Column(length = 50)
    private String status;

    @OneToOne
    private PaymentTransaction paymentTransaction;

    @ManyToOne
    @JoinColumn(name = "user_id",  nullable = false)
    private User user;
}

