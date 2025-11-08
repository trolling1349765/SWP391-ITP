package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.time.LocalDate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByShopId(Long shopId);
    boolean existsByShopId(Long shopId);
    User findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    @Query("""
        select r.name, count(u.id)
        from User u join u.role r
        group by r.name
        """)
    List<Object[]> countUsersByRole();

    List<User> findTop10ByOrderByIdDesc();
}
