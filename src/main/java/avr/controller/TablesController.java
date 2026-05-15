package avr.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import avr.config.DatabaseConfig;
import avr.config.MonitoringConfig;
import avr.model.TableSizeInfo;
import avr.repository.TableStatsRepository;

@RestController
@RequestMapping("/api/tables")
public class TablesController {

  private static final Logger log = LoggerFactory.getLogger(TablesController.class);

  private final MonitoringConfig monitoringConfig;
  private final TableStatsRepository tableStatsRepository;
  private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();

  public TablesController(MonitoringConfig monitoringConfig,
      TableStatsRepository tableStatsRepository) {
    this.monitoringConfig = monitoringConfig;
    this.tableStatsRepository = tableStatsRepository;
  }

  @GetMapping("/largest/{dbName}")
  public List<TableSizeInfo> getLargestTables(@PathVariable String dbName,
      @RequestParam(defaultValue = "20") int limit) {
    DatabaseConfig dbConfig = findDatabase(dbName);
    if (dbConfig == null) {
      log.warn("База данных {} не найдена", dbName);
      return List.of();
    }

    if (!dbConfig.enabled()) {
      log.warn("База данных {} отключена", dbName);
      return List.of();
    }

    try {
      JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
      return tableStatsRepository.getLargestTables(jdbcTemplate, limit);
    } catch (Exception e) {
      log.error("Ошибка получения таблиц для {}: {}", dbName, e.getMessage());
      return List.of();
    }
  }

  private JdbcTemplate getJdbcTemplate(DatabaseConfig dbConfig) {
    return jdbcTemplates.computeIfAbsent(dbConfig.name(), key -> {
      try {
        var builder = org.springframework.boot.jdbc.DataSourceBuilder.create();
        builder.url(dbConfig.getJdbcUrl());
        builder.username(dbConfig.username());
        builder.password(dbConfig.password());
        builder.driverClassName("org.postgresql.Driver");
        return new JdbcTemplate(builder.build());
      } catch (Exception e) {
        log.error("Ошибка создания DataSource для {}: {}", dbConfig.name(), e.getMessage());
        throw new RuntimeException("Cannot create DataSource for " + dbConfig.name(), e);
      }
    });
  }

  private DatabaseConfig findDatabase(String name) {
    return monitoringConfig.getDatabases().stream()
        .filter(db -> db.name().equals(name))
        .findFirst()
        .orElse(null);
  }
}
