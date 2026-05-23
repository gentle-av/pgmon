package avr.controller;

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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/config")
public class ConfigController {
  private static final Logger log = LoggerFactory.getLogger(ConfigController.class);
  private final MonitoredServerRepository serverRepository;
  private final PollingConfigurationRepository pollingConfigRepository;
  private final DynamicSchedulingService dynamicSchedulingService;

  public ConfigController(MonitoredServerRepository serverRepository,
      PollingConfigurationRepository pollingConfigRepository,
      DynamicSchedulingService dynamicSchedulingService) {
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
  public PollingConfiguration updatePollingConfig(@PathVariable String id, @RequestBody Map<String, Object> request) {
    PollingConfiguration config = pollingConfigRepository.findByServerId(id)
        .orElseThrow(() -> new RuntimeException("Polling config not found for server: " + id));
    if (request.containsKey("ashCollectionIntervalMs") && request.get("ashCollectionIntervalMs") != null) {
      config.setAshCollectionIntervalMs(((Number) request.get("ashCollectionIntervalMs")).intValue());
    }
    if (request.containsKey("pollingIntervalMs") && request.get("pollingIntervalMs") != null) {
      config.setPollingIntervalMs(((Number) request.get("pollingIntervalMs")).intValue());
    }
    if (request.containsKey("sessionsCollectionIntervalMs") && request.get("sessionsCollectionIntervalMs") != null) {
      config.setSessionsCollectionIntervalMs(((Number) request.get("sessionsCollectionIntervalMs")).intValue());
    }
    if (request.containsKey("collectAshData")) {
      config.setCollectAshData((Boolean) request.get("collectAshData"));
    }
    config.setUpdatedAt(LocalDateTime.now());
    PollingConfiguration saved = pollingConfigRepository.save(config);
    dynamicSchedulingService.rescheduleServer(id);
    return saved;
  }
}
