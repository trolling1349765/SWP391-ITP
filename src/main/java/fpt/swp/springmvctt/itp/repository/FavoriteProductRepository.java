package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.FavoriteProduct;
import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteProductRepository extends JpaRepository<FavoriteProduct, Long> {

    // ✅ Fetch toàn bộ product + shop + category trong 1 query duy nhất
    @Query("""
        SELECT f FROM FavoriteProduct f
        JOIN FETCH f.product p
        LEFT JOIN FETCH p.shop
        LEFT JOIN FETCH p.category
        WHERE f.user = :user
    """)
    List<FavoriteProduct> findByUser(@Param("user") User user);

    Optional<FavoriteProduct> findByUserAndProduct(User user, Product product);

    void deleteByUserAndProduct(User user, Product product);
}
