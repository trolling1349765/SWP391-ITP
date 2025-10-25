package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    User findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);


}
