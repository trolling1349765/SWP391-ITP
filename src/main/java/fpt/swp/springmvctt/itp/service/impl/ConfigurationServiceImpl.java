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
    public void save(String configKey, String configValue) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        Configuration configuration = new Configuration();
        configuration.setConfigKey(configKey.toUpperCase());
        configuration.setConfigValue(configValue);
        configuration.setCreateAt(LocalDate.now());
        configuration.setCreateBy(user.getUsername());
        configurationRepository.save(configuration);
    }

    @Override
    public void delete(Long id) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        configurationRepository.findById(id).ifPresent((configuration) ->{
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
        configurationRepository.findById(id).ifPresent((configuration) ->{
           configuration.setConfigKey(configKey);
           configuration.setConfigValue(configValue);
           configuration.setUpdateAt(LocalDate.now());
           configurationRepository.save(configuration);
        });
    }

    @Override
    public void reborn(Long id) {
        configurationRepository.findById(id).ifPresent((configuration) ->{
            if (!configuration.getIsDeleted()) return;
            configuration.setIsDeleted(false);
            configuration.setUpdateAt(LocalDate.now());
            configuration.setDeleteBy(null);
            configurationRepository.save(configuration);
        });
    }

}
