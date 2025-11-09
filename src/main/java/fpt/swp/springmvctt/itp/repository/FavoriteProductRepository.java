package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.FavoriteProduct;
import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteProductRepository extends JpaRepository<FavoriteProduct, Long> {
    List<FavoriteProduct> findByUser(User user);
    Optional<FavoriteProduct> findByUserAndProduct(User user, Product product);
    void deleteByUserAndProduct(User user, Product product);
}
