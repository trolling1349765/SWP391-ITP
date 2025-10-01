package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
