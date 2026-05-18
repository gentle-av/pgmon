package avr.controller;

import avr.config.ConfigRoot;
import avr.config.MonitoringConfig;
import avr.dto.DatabaseConfigDTO;
import avr.model.MonitoredServer;
import avr.model.PollingConfiguration;
import avr.repository.MonitoredServerRepository;
import avr.repository.PollingConfigurationRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/config")
public class ConfigController {
    private final MonitoringConfig monitoringConfig;
    private final MonitoredServerRepository serverRepository;
    private final PollingConfigurationRepository pollingConfigRepository;

    public ConfigController(MonitoringConfig monitoringConfig,
                            MonitoredServerRepository serverRepository,
                            PollingConfigurationRepository pollingConfigRepository) {
        this.monitoringConfig = monitoringConfig;
        this.serverRepository = serverRepository;
        this.pollingConfigRepository = pollingConfigRepository;
    }

    @GetMapping("/databases")
    public List<DatabaseConfigDTO> getDatabases() {
        return monitoringConfig.getDatabaseConfigs().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @GetMapping("/servers")
    public List<MonitoredServer> getServers() {
        return serverRepository.findAll();
    }

    @GetMapping("/servers/{id}")
    public MonitoredServer getServer(@PathVariable String id) {
        return serverRepository.findById(id).orElse(null);
    }

    @PostMapping("/servers")
    public MonitoredServer createServer(@RequestBody MonitoredServer server) {
        server.setId(UUID.randomUUID().toString());
        server.setCreatedAt(LocalDateTime.now());
        server.setUpdatedAt(LocalDateTime.now());
        return serverRepository.save(server);
    }

    @PutMapping("/servers/{id}")
    public MonitoredServer updateServer(@PathVariable String id, @RequestBody MonitoredServer server) {
        server.setId(id);
        server.setUpdatedAt(LocalDateTime.now());
        return serverRepository.save(server);
    }

    @DeleteMapping("/servers/{id}")
    public void deleteServer(@PathVariable String id) {
        serverRepository.deleteById(id);
    }

    @GetMapping("/servers/{id}/polling")
    public PollingConfiguration getPollingConfig(@PathVariable String id) {
        return pollingConfigRepository.findByServerId(id).orElse(null);
    }

    @PostMapping("/servers/{id}/polling")
    public PollingConfiguration createPollingConfig(@PathVariable String id, @RequestBody PollingConfiguration config) {
        config.setId(UUID.randomUUID().toString());
        config.setServerId(id);
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return pollingConfigRepository.save(config);
    }

    @PutMapping("/servers/{id}/polling")
    public PollingConfiguration updatePollingConfig(@PathVariable String id, @RequestBody PollingConfiguration config) {
        config.setServerId(id);
        config.setUpdatedAt(LocalDateTime.now());
        return pollingConfigRepository.save(config);
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
