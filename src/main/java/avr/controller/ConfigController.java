package avr.controller;

import avr.config.ConfigRoot;
import avr.config.MonitoringConfig;
import avr.dto.DatabaseConfigDTO;
import avr.dto.UpdatePollingRequest;
import avr.model.MonitoredServer;
import avr.model.PollingConfiguration;
import avr.repository.MonitoredServerRepository;
import avr.repository.PollingConfigurationRepository;
import avr.service.DynamicSchedulingService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/config")
public class ConfigController {
    private static final Logger log = LoggerFactory.getLogger(ConfigController.class);
    private final MonitoringConfig monitoringConfig;
    private final MonitoredServerRepository serverRepository;
    private final PollingConfigurationRepository pollingConfigRepository;
    private final DynamicSchedulingService dynamicSchedulingService;

    public ConfigController(MonitoringConfig monitoringConfig,
                            MonitoredServerRepository serverRepository,
                            PollingConfigurationRepository pollingConfigRepository,
                            DynamicSchedulingService dynamicSchedulingService) {
        this.monitoringConfig = monitoringConfig;
        this.serverRepository = serverRepository;
        this.pollingConfigRepository = pollingConfigRepository;
        this.dynamicSchedulingService = dynamicSchedulingService;
    }

    @PostConstruct
    public void ensureAshEnabled() {
        List<PollingConfiguration> configs = pollingConfigRepository.findAll();
        for (PollingConfiguration config : configs) {
            if (!config.isCollectAshData()) {
                log.warn("Fixing collect_ash_data for server: {} from false to true", config.getServerId());
                config.setCollectAshData(true);
                pollingConfigRepository.save(config);
                dynamicSchedulingService.rescheduleServer(config.getServerId());
            }
        }
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
    public PollingConfiguration updatePollingConfig(@PathVariable String id, @RequestBody UpdatePollingRequest request) {
        PollingConfiguration config = pollingConfigRepository.findByServerId(id)
                .orElseThrow(() -> new RuntimeException("Polling config not found for server: " + id));
        if (request.getStoreEmptySnapshots() != null) {
            config.setStoreEmptySnapshots(request.getStoreEmptySnapshots());
        }
        config.setCollectAshData(request.isCollectAshData());
        config.setCollectQueries(request.isCollectQueries());
        config.setCollectConnections(request.isCollectConnections());
        config.setCollectLocks(request.isCollectLocks());
        config.setCollectCacheHitRatio(request.isCollectCacheHitRatio());
        config.setCollectUnusedIndexes(request.isCollectUnusedIndexes());
        config.setCollectVacuumStats(request.isCollectVacuumStats());
        if (request.getPollingIntervalMs() != null && request.getPollingIntervalMs() > 0) {
            config.setPollingIntervalMs(request.getPollingIntervalMs());
            config.setPollingIntervalSeconds(request.getPollingIntervalMs() / 1000);
        }
        if (request.getAshCollectionIntervalMs() != null && request.getAshCollectionIntervalMs() > 0) {
            config.setAshCollectionIntervalMs(request.getAshCollectionIntervalMs());
            config.setAshCollectionIntervalSeconds(request.getAshCollectionIntervalMs() / 1000);
        }
        if (request.getSlowQueryThresholdMs() != null) {
            config.setSlowQueryThresholdMs(request.getSlowQueryThresholdMs());
        }
        if (request.getMaxSlowQueries() != null) {
            config.setMaxSlowQueries(request.getMaxSlowQueries());
        }
        if (request.getPriority() != null && request.getPriority() > 0) {
            config.setPriority(request.getPriority());
        }
        config.setUpdatedAt(LocalDateTime.now());
        PollingConfiguration saved = pollingConfigRepository.save(config);
        dynamicSchedulingService.rescheduleServer(id);
        return saved;
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
