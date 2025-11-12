package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.entity.Configuration;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface ConfigurationService {
    Page<Configuration> findAll(int page, int size);

    boolean save(String configKey, String configValue);

    void delete(Long id);

    Configuration findById(Long id);

    void update(Long id, String configKey, String configValue);

    void reborn(Long id);

    Page<Configuration> findByFilter(String configKey, LocalDate toDate, LocalDate fromDate, Boolean delete, int page, int size);
}
