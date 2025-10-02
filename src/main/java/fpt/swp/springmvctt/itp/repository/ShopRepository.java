package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
}
