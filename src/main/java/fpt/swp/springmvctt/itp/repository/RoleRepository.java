package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}
