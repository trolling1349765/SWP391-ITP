package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {
}
