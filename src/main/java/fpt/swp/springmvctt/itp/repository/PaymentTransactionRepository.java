package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
}
