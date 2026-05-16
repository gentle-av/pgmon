package avr.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import avr.cache.CacheNames;
import avr.config.DatabaseConfig;
import avr.config.MonitoringConfig;
import avr.model.QueryStats;
import avr.repository.QueryStatsRepository;

@Service
public class QueryAnalysisService {

  private static final Logger log = LoggerFactory.getLogger(QueryAnalysisService.class);

  private final MonitoringConfig monitoringConfig;
  private final QueryStatsRepository queryStatsRepository;
  private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();

  public QueryAnalysisService(MonitoringConfig monitoringConfig,
      QueryStatsRepository queryStatsRepository) {
    this.monitoringConfig = monitoringConfig;
    this.queryStatsRepository = queryStatsRepository;
  }

  @Cacheable(value = CacheNames.QUERY_STATS, key = "#dbConfig.name() + ':slow'")
  public List<QueryStats> getSlowestQueries(DatabaseConfig dbConfig) {
    JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
    int threshold = monitoringConfig.getMonitoring().slowQueryThresholdMs();
    int limit = monitoringConfig.getMonitoring().maxSlowQueries();
    return queryStatsRepository.getSlowestQueries(jdbcTemplate, limit, threshold);
  }

  @Cacheable(value = CacheNames.QUERY_STATS, key = "#dbConfig.name() + ':frequent'")
  public List<QueryStats> getMostFrequentQueries(DatabaseConfig dbConfig) {
    JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
    return queryStatsRepository.getMostFrequentQueries(jdbcTemplate, 20);
  }

  @CacheEvict(value = CacheNames.QUERY_STATS, key = "#dbConfig.name() + ':slow'")
  public void evictSlowQueries(DatabaseConfig dbConfig) {
    log.debug("Evicted slow queries cache for: {}", dbConfig.name());
  }

  @CacheEvict(value = CacheNames.QUERY_STATS, key = "#dbConfig.name() + ':frequent'")
  public void evictFrequentQueries(DatabaseConfig dbConfig) {
    log.debug("Evicted frequent queries cache for: {}", dbConfig.name());
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
