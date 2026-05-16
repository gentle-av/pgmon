package avr.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import avr.config.DatabaseConfig;
import avr.config.MonitoringConfig;
import avr.service.HealthCheckService;

@RestController
@RequestMapping("/api/health")
public class HealthController {

  private final MonitoringConfig monitoringConfig;
  private final HealthCheckService healthCheckService;

  public HealthController(MonitoringConfig monitoringConfig,
      HealthCheckService healthCheckService) {
    this.monitoringConfig = monitoringConfig;
    this.healthCheckService = healthCheckService;
  }

  @GetMapping
  public Map<String, Object> health() {
    Map<String, Object> result = new HashMap<>();
    result.put("status", "UP");
    result.put("timestamp", System.currentTimeMillis());

    Map<String, Object> databases = new HashMap<>();
    for (DatabaseConfig dbConfig : monitoringConfig.getDatabases()) {
      if (dbConfig.enabled()) {
        databases.put(dbConfig.name(), healthCheckService.check(dbConfig));
      } else {
        databases.put(dbConfig.name(), Map.of("status", "DISABLED"));
      }
    }
    result.put("databases", databases);

    return result;
  }

  @GetMapping("/{dbName}")
  public Map<String, Object> databaseHealth(@PathVariable String dbName) {
    DatabaseConfig dbConfig = findDatabase(dbName);
    if (dbConfig == null) {
      return Map.of("error", "Database not found: " + dbName);
    }
    if (!dbConfig.enabled()) {
      return Map.of("status", "DISABLED", "name", dbName);
    }
    return healthCheckService.check(dbConfig);
  }

  private DatabaseConfig findDatabase(String name) {
    return monitoringConfig.getDatabases().stream()
        .filter(db -> db.name().equals(name))
        .findFirst()
        .orElse(null);
  }
}
