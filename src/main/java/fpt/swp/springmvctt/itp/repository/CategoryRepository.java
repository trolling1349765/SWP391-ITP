package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {}
