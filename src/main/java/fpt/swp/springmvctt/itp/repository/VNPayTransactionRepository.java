package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.VNPayTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VNPayTransactionRepository extends JpaRepository<VNPayTransaction, Long> {
}
