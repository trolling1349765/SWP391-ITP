package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.ProductStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductStoreRepository extends JpaRepository<ProductStore, Long> {
    Optional<ProductStore> findByProductIdAndSerialCode(Long productId, String serialCode);
    List<ProductStore> findByProductIdOrderByIdDesc(Long productId);
}
