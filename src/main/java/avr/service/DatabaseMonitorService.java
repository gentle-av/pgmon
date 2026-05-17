package avr.service;

import avr.config.ConfigRoot;
import avr.model.DatabaseStats;
import avr.repository.DatabaseStatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DatabaseMonitorService {
    private static final Logger log = LoggerFactory.getLogger(DatabaseMonitorService.class);
    private final DatabaseStatsRepository databaseStatsRepository;
    private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();
    public DatabaseMonitorService(DatabaseStatsRepository databaseStatsRepository) {
        this.databaseStatsRepository = databaseStatsRepository;
    }
    @Cacheable(value = "database_stats", key = "#dbConfig.name")
    public DatabaseStats collectStats(ConfigRoot.DatabaseConfig dbConfig) {
        log.debug("Cache MISS - collecting fresh stats for: {}", dbConfig.getName());
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
        return databaseStatsRepository.getDatabaseStats(jdbcTemplate, dbConfig.getName());
    }
    @CacheEvict(value = "database_stats", key = "#dbConfig.name")
    public void evictStats(ConfigRoot.DatabaseConfig dbConfig) {
        log.info("Evicted database stats for: {}", dbConfig.getName());
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
