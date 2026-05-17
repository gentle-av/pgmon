package avr.service;

import avr.config.ConfigRoot;
import avr.config.MonitoringConfig;
import avr.model.QueryStats;
import avr.repository.QueryStatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QueryAnalysisService {
    private static final Logger log = LoggerFactory.getLogger(QueryAnalysisService.class);
    private final MonitoringConfig monitoringConfig;
    private final QueryStatsRepository queryStatsRepository;
    private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();
    public QueryAnalysisService(MonitoringConfig monitoringConfig, QueryStatsRepository queryStatsRepository) {
        this.monitoringConfig = monitoringConfig;
        this.queryStatsRepository = queryStatsRepository;
    }
    @Cacheable(value = "query_stats", key = "#dbConfig.name + ':slow'")
    public List<QueryStats> getSlowestQueries(ConfigRoot.DatabaseConfig dbConfig) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
        int threshold = monitoringConfig.getMonitoringSettings().getSlowQueryThresholdMs();
        int limit = monitoringConfig.getMonitoringSettings().getMaxSlowQueries();
        return queryStatsRepository.getSlowestQueries(jdbcTemplate, limit, threshold);
    }
    @Cacheable(value = "query_stats", key = "#dbConfig.name + ':frequent'")
    public List<QueryStats> getMostFrequentQueries(ConfigRoot.DatabaseConfig dbConfig) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
        return queryStatsRepository.getMostFrequentQueries(jdbcTemplate, 20);
    }
    @CacheEvict(value = "query_stats", key = "#dbConfig.name + ':slow'")
    public void evictSlowQueries(ConfigRoot.DatabaseConfig dbConfig) {
        log.debug("Evicted slow queries cache for: {}", dbConfig.getName());
    }
    @CacheEvict(value = "query_stats", key = "#dbConfig.name + ':frequent'")
    public void evictFrequentQueries(ConfigRoot.DatabaseConfig dbConfig) {
        log.debug("Evicted frequent queries cache for: {}", dbConfig.getName());
    }
    private JdbcTemplate getJdbcTemplate(ConfigRoot.DatabaseConfig dbConfig) {
        return jdbcTemplates.computeIfAbsent(dbConfig.getName(), key -> {
            var builder = org.springframework.boot.jdbc.DataSourceBuilder.create();
            builder.url(String.format("jdbc:postgresql://%s:%d/%s", dbConfig.getHost(), dbConfig.getPort(), dbConfig.getDatabase()));
            builder.username(dbConfig.getUsername());
            builder.password(dbConfig.getPassword());
            builder.driverClassName("org.postgresql.Driver");
            return new JdbcTemplate(builder.build());
        });
    }
}
