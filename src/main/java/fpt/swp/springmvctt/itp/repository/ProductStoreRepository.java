package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.ProductStore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductStoreRepository extends JpaRepository<ProductStore, Long> {
}
