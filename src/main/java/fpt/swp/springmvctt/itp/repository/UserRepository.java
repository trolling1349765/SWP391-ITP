package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import java.time.LocalDate;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByShopId(Long shopId);
    boolean existsByShopId(Long shopId);
    User findByEmail(String email);
    User findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);


}
