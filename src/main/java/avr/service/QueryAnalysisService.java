package avr.service;

import avr.config.MonitoringConfig;
import avr.model.MonitoredServer;
import avr.model.PollingConfiguration;
import avr.model.QueryStats;
import avr.repository.QueryStatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.util.List;

@Service
public class QueryAnalysisService {
    private static final Logger log = LoggerFactory.getLogger(QueryAnalysisService.class);
    private final MonitoringConfig monitoringConfig;
    private final QueryStatsRepository queryStatsRepository;
    private final ConnectionPoolManager connectionPoolManager;

    public QueryAnalysisService(MonitoringConfig monitoringConfig,
                                 QueryStatsRepository queryStatsRepository,
                                 ConnectionPoolManager connectionPoolManager) {
        this.monitoringConfig = monitoringConfig;
        this.queryStatsRepository = queryStatsRepository;
        this.connectionPoolManager = connectionPoolManager;
    }

    @Cacheable(value = "query_stats", key = "#server.id + ':slow'")
    public List<QueryStats> getSlowestQueries(MonitoredServer server, PollingConfiguration pollingConfig) {
        DataSource dataSource = connectionPoolManager.getDataSource(server.getId());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        int threshold = pollingConfig != null && pollingConfig.getSlowQueryThresholdMs() != null ?
                pollingConfig.getSlowQueryThresholdMs() :
                monitoringConfig.getMonitoringSettings().getSlowQueryThresholdMs();
        int limit = pollingConfig != null && pollingConfig.getMaxSlowQueries() != null ?
                pollingConfig.getMaxSlowQueries() :
                monitoringConfig.getMonitoringSettings().getMaxSlowQueries();
        return queryStatsRepository.getSlowestQueries(jdbcTemplate, limit, threshold);
    }

    @Cacheable(value = "query_stats", key = "#server.id + ':frequent'")
    public List<QueryStats> getMostFrequentQueries(MonitoredServer server) {
        DataSource dataSource = connectionPoolManager.getDataSource(server.getId());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return queryStatsRepository.getMostFrequentQueries(jdbcTemplate, 20);
    }

    @CacheEvict(value = "query_stats", key = "#server.id + ':slow'")
    public void evictSlowQueries(MonitoredServer server) {
        log.debug("Evicted slow queries cache for: {}", server.getServerName());
    }

    @CacheEvict(value = "query_stats", key = "#server.id + ':frequent'")
    public void evictFrequentQueries(MonitoredServer server) {
        log.debug("Evicted frequent queries cache for: {}", server.getServerName());
    }
}
