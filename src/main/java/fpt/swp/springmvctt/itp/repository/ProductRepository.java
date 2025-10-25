package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // CŨ (giữ lại): Phân trang theo status, order id desc (khi muốn mặc định newest)
    Page<Product> findByStatusOrderByIdDesc(String status, Pageable pageable);

    // CŨ (giữ lại): Phân trang theo status + categoryId, order id desc
    Page<Product> findByStatusAndCategory_IdOrderByIdDesc(String status, Long categoryId, Pageable pageable);

    // CŨ (giữ lại): Không lọc status, chỉ lọc category
    Page<Product> findByCategory_Id(Long categoryId, Pageable pageable);

    // MỚI (thêm): Cho phép sort linh hoạt qua Pageable (price asc/desc, id, ...)
    Page<Product> findByStatus(String status, Pageable pageable);

    // MỚI (thêm): Lọc theo status + category, sort linh hoạt qua Pageable
    Page<Product> findByStatusAndCategory_Id(String status, Long categoryId, Pageable pageable);
}
