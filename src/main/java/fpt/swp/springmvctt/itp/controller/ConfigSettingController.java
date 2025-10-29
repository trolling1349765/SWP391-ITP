package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.entity.Configuration;
import fpt.swp.springmvctt.itp.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class ConfigSettingController {

    @Autowired
    private ConfigurationService configurationService;

    @GetMapping("/configs")
    public String showConfigs(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            Model model
    ) {
        Page<Configuration> configs = configurationService.findAll(page, size);

        model.addAttribute("configs", configs);
        model.addAttribute("currentPage", configs.getNumber());
        model.addAttribute("totalItems", configs.getTotalElements());
        model.addAttribute("totalPages", configs.getTotalPages());
        return "admin/configs";
    }

    @GetMapping("/configs/new")
    public String newConfigForm(Model model) {
        model.addAttribute("config", new Configuration());
        return "admin/new-config";
    }

    @PostMapping("/configs/new")
    public String saveConfig(
            @RequestParam("configKey") String configKey,
            @RequestParam("configValue") String configValue
    ) {
        configurationService.save(configKey, configValue);
        return "redirect:/admin/configs";
    }

    @DeleteMapping("/configs/delete/{id}")
    public String deleteConfig(@PathVariable Long id) {
        configurationService.delete(id);
        return "redirect:/admin/configs";
    }

    @GetMapping("/configs/update/{id}")
    public String updateConfigForm(@PathVariable Long id, Model model) {
        model.addAttribute("config", configurationService.findById(id));
        return "admin/update-config";
    }

    @PutMapping("/configs/update")
    public String updateConfig(
            @RequestParam String configKey,
            @RequestParam String configValue,
            @RequestParam Long id
            ) {
        configurationService.update(id, configKey, configValue);
        return "redirect:/admin/configs";
    }

    @PutMapping("/configs/reborn/{id}")
    public String rebornConfig(
            @PathVariable Long id
    ) {
        configurationService.reborn(id);
        return "redirect:/admin/configs";
    }
}
