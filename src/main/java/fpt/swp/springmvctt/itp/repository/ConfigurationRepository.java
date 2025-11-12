package fpt.swp.springmvctt.itp.repository;

import fpt.swp.springmvctt.itp.entity.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {

    @Query("""
    SELECT c 
    FROM Configuration c
    WHERE (:configKey IS NULL OR c.configKey LIKE CONCAT('%', :configKey, '%') )
      AND (:startDate IS NULL OR c.createAt >= :startDate)
      AND (:endDate IS NULL OR c.createAt <= :endDate)
      AND (:isDelete IS NULL OR c.isDeleted = :isDelete)
""")
    Page<Configuration> findByFilter(
            @Param("configKey") String configKey,
            @Param("startDate") LocalDate toDate,
            @Param("endDate") LocalDate fromDate,
            @Param("isDelete") Boolean delete,
            Pageable pageable);

    /**
     * Tìm configuration theo key (không phân biệt hoa thường)
     * Chỉ lấy config chưa bị xóa (isDeleted = false)
     */
    @Query("""
    SELECT c 
    FROM Configuration c
    WHERE UPPER(c.configKey) = UPPER(:configKey)
      AND c.isDeleted = false
    ORDER BY c.id DESC
    """)
    java.util.Optional<Configuration> findByConfigKey(@Param("configKey") String configKey);
}
