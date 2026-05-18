package avr.service;

import avr.model.DatabaseStats;
import avr.model.MonitoredServer;
import avr.repository.DatabaseStatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;

@Service
public class DatabaseMonitorService {
    private static final Logger log = LoggerFactory.getLogger(DatabaseMonitorService.class);
    private final DatabaseStatsRepository databaseStatsRepository;
    private final ConnectionPoolManager connectionPoolManager;

    public DatabaseMonitorService(DatabaseStatsRepository databaseStatsRepository,
                                   ConnectionPoolManager connectionPoolManager) {
        this.databaseStatsRepository = databaseStatsRepository;
        this.connectionPoolManager = connectionPoolManager;
    }

    @Cacheable(value = "database_stats", key = "#server.id")
    public DatabaseStats collectStats(MonitoredServer server) {
        log.debug("Cache MISS - collecting fresh stats for: {}", server.getServerName());
        DataSource dataSource = connectionPoolManager.getDataSource(server.getId());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return databaseStatsRepository.getDatabaseStats(jdbcTemplate, server.getServerName());
    }

    @CacheEvict(value = "database_stats", key = "#server.id")
    public void evictStats(MonitoredServer server) {
        log.info("Evicted database stats for: {}", server.getServerName());
    }
}
