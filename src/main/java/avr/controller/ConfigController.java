package avr.controller;

import avr.config.ConfigRoot;
import avr.config.MonitoringConfig;
import avr.dto.DatabaseConfigDTO;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/config")
public class ConfigController {
    private final MonitoringConfig monitoringConfig;
    public ConfigController(MonitoringConfig monitoringConfig) {
        this.monitoringConfig = monitoringConfig;
    }
    @GetMapping("/databases")
    public List<DatabaseConfigDTO> getDatabases() {
        return monitoringConfig.getDatabaseConfigs().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    @GetMapping("/server")
    public Map<String, Integer> getServerPort() {
        return Map.of("port", monitoringConfig.getServerConfig().getPort());
    }
    @GetMapping("/monitoring")
    public ConfigRoot.MonitoringSettings getMonitoringSettings() {
        return monitoringConfig.getMonitoringSettings();
    }
    private DatabaseConfigDTO toDTO(ConfigRoot.DatabaseConfig db) {
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", db.getHost(), db.getPort(), db.getDatabase());
        return new DatabaseConfigDTO(db.getName(), db.isEnabled(), jdbcUrl, db.getUsername());
    }
}
