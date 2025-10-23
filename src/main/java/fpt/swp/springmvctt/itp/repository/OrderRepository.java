package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

}
