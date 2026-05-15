package avr.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import avr.config.DatabaseConfig;
import avr.model.ConnectionInfo;
import avr.repository.ActivityRepository;

@Service
public class ConnectionsMonitorService {

  private final ActivityRepository activityRepository;
  private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();

  public ConnectionsMonitorService(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  public List<ConnectionInfo> getActiveConnections(DatabaseConfig dbConfig) {
    JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
    return activityRepository.getActiveConnections(jdbcTemplate);
  }

  public int getRunningQueriesCount(DatabaseConfig dbConfig) {
    JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
    return activityRepository.getRunningQueriesCount(jdbcTemplate);
  }

  public int getWaitingQueriesCount(DatabaseConfig dbConfig) {
    JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
    return activityRepository.getWaitingQueriesCount(jdbcTemplate);
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
