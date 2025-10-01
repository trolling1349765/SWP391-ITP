package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
