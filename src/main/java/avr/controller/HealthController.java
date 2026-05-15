package avr.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import avr.config.DatabaseConfig;
import avr.config.MonitoringConfig;

@RestController
@RequestMapping("/api/health")
public class HealthController {

  private final MonitoringConfig monitoringConfig;

  public HealthController(MonitoringConfig monitoringConfig) {
    this.monitoringConfig = monitoringConfig;
  }

  @GetMapping
  public Map<String, Object> health() {
    Map<String, Object> result = new HashMap<>();
    result.put("status", "UP");
    result.put("timestamp", System.currentTimeMillis());

    Map<String, Object> databases = new HashMap<>();
    for (DatabaseConfig dbConfig : monitoringConfig.getDatabases()) {
      databases.put(dbConfig.name(), checkDatabase(dbConfig));
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
    return checkDatabase(dbConfig);
  }

  private Map<String, Object> checkDatabase(DatabaseConfig dbConfig) {
    Map<String, Object> status = new HashMap<>();
    status.put("name", dbConfig.name());
    status.put("enabled", dbConfig.enabled());

    if (!dbConfig.enabled()) {
      status.put("status", "DISABLED");
      return status;
    }

    try {
      var builder = org.springframework.boot.jdbc.DataSourceBuilder.create();
      builder.url(dbConfig.getJdbcUrl());
      builder.username(dbConfig.username());
      builder.password(dbConfig.password());
      builder.driverClassName("org.postgresql.Driver");

      JdbcTemplate jdbcTemplate = new JdbcTemplate(builder.build());
      Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

      status.put("status", "UP");
      status.put("response", result);
    } catch (Exception e) {
      status.put("status", "DOWN");
      status.put("error", e.getMessage());
    }

    return status;
  }

  private DatabaseConfig findDatabase(String name) {
    return monitoringConfig.getDatabases().stream()
        .filter(db -> db.name().equals(name))
        .findFirst()
        .orElse(null);
  }
}
