package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.entity.Configuration;
import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.repository.ConfigurationRepository;
import fpt.swp.springmvctt.itp.service.ConfigurationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    @Autowired
    private ConfigurationRepository configurationRepository;
    @Autowired
    private HttpServletRequest request;

    @Override
    public Page<Configuration> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return configurationRepository.findAll(pageable);
    }

    @Override
    public boolean save(String configKey, String configValue) {
        Configuration configuration = configurationRepository.findByConfigKey(configKey);
        if (configuration == null) {
            configuration = new Configuration();
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");
            configuration.setConfigKey(configKey.toUpperCase());
            configuration.setConfigValue(configValue);
            configuration.setCreateAt(LocalDate.now());
            configuration.setCreateBy(user.getUsername());
            configurationRepository.save(configuration);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void delete(Long id) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        configurationRepository.findById(id).ifPresent((configuration) -> {
            if (configuration.getIsDeleted()) return;
            configuration.setDeleteBy(user.getUsername());
            configuration.setUpdateAt(LocalDate.now());
            configuration.setIsDeleted(true);
            configurationRepository.save(configuration);
        });
    }

    @Override
    public Configuration findById(Long id) {
        return configurationRepository.findById(id).orElse(null);
    }

    @Override
    public void update(Long id, String configKey, String configValue) {
        configurationRepository.findById(id).ifPresent((configuration) -> {
            configuration.setConfigKey(configKey);
            configuration.setConfigValue(configValue);
            configuration.setUpdateAt(LocalDate.now());
            configurationRepository.save(configuration);
        });
    }

    @Override
    public void reborn(Long id) {
        configurationRepository.findById(id).ifPresent((configuration) -> {
            if (!configuration.getIsDeleted()) return;
            configuration.setIsDeleted(false);
            configuration.setUpdateAt(LocalDate.now());
            configuration.setDeleteBy(null);
            configurationRepository.save(configuration);
        });
    }

    @Override
    public Page<Configuration> findByFilter(String configKey, LocalDate toDate, LocalDate fromDate, Boolean delete, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        if (configKey.isEmpty() || configKey.equals("")) {
            configKey = null;
        }
        if (!configurationRepository.findByFilter(configKey, toDate, fromDate, delete, pageable).isEmpty())
            return configurationRepository.findByFilter(configKey, toDate, fromDate, delete, pageable);
        else return Page.empty();
    }

}
