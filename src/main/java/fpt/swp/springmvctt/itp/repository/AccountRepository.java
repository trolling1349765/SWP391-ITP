package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
