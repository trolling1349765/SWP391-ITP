package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Tìm theo tên (ALL status)
    Page<Category> findByCategoryNameContainingIgnoreCase(String name, Pageable pageable);
}
