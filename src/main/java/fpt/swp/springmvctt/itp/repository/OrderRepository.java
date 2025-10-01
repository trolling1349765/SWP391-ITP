package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
