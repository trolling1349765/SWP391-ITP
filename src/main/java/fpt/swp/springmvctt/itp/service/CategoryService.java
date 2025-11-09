package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.entity.Category;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> findAll();

    // thêm mới
    Page<Category> search(String q, int page, int size);
    Optional<Category> findById(Long id);
    Category save(Category c);
    void deleteById(Long id);
}
