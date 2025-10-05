package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.UserRestriction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRestrictionRepository extends JpaRepository<UserRestriction, Long> {
}
