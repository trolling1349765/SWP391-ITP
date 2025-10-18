package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIdAndShop_Id(Long id, Long shopId);
    List<Product> findByShopIdOrderByIdDesc(Long shopId);
    boolean existsByProductNameAndShop_Id(String productName, Long shopId);
    List<Product> findByShop_IdOrderByIdDesc(Long shopId);
    Optional<Product> findByIdAndShopId(Long id, Long shopId);

}
