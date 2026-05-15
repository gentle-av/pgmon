package avr.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import avr.config.DatabaseConfig;
import avr.config.MonitoringConfig;
import avr.model.DatabaseStats;
import avr.repository.DatabaseStatsRepository;

@Service
public class DatabaseMonitorService {

  private static final Logger log = LoggerFactory.getLogger(DatabaseMonitorService.class);

  private final MonitoringConfig monitoringConfig;
  private final DatabaseStatsRepository databaseStatsRepository;
  private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();

  public DatabaseMonitorService(MonitoringConfig monitoringConfig,
      DatabaseStatsRepository databaseStatsRepository) {
    this.monitoringConfig = monitoringConfig;
    this.databaseStatsRepository = databaseStatsRepository;
  }

  public DatabaseStats collectStats(DatabaseConfig dbConfig) {
    JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
    return databaseStatsRepository.getDatabaseStats(jdbcTemplate, dbConfig.name());
  }

  private JdbcTemplate getJdbcTemplate(DatabaseConfig dbConfig) {
    return jdbcTemplates.computeIfAbsent(dbConfig.name(), key -> {
      var builder = org.springframework.boot.jdbc.DataSourceBuilder.create();
      builder.url(dbConfig.getJdbcUrl());
      builder.username(dbConfig.username());
      builder.password(dbConfig.password());
      builder.driverClassName("org.postgresql.Driver");
      return new JdbcTemplate(builder.build());
    });
  }
}
