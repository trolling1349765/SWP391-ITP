package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
