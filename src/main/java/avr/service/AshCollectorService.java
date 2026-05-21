package avr.service;

import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import avr.model.MonitoredServer;
import avr.model.PollingConfiguration;
import avr.repository.AshHistoryRepository;
import avr.repository.MonitoredServerRepository;
import avr.repository.PollingConfigurationRepository;

@Service
public class AshCollectorService {
    private static final Logger log = LoggerFactory.getLogger(AshCollectorService.class);
    private final AshHistoryRepository ashRepository;
    private final ConnectionPoolManager connectionPoolManager;
    private final MonitoredServerRepository serverRepository;
    private final PollingConfigurationRepository pollingConfigRepository;
    private final JdbcTemplate pgmonJdbcTemplate;

    public AshCollectorService(AshHistoryRepository ashRepository,
                               ConnectionPoolManager connectionPoolManager,
                               MonitoredServerRepository serverRepository,
                               PollingConfigurationRepository pollingConfigRepository,
                               DataSource pgmonDataSource) {
        this.ashRepository = ashRepository;
        this.connectionPoolManager = connectionPoolManager;
        this.serverRepository = serverRepository;
        this.pollingConfigRepository = pollingConfigRepository;
        this.pgmonJdbcTemplate = new JdbcTemplate(pgmonDataSource);
    }

    public void collectAshSnapshotsForServer(MonitoredServer server, boolean storeEmpty) {
        PollingConfiguration pollingConfig = pollingConfigRepository.findByServerId(server.getId()).orElse(null);
        if (pollingConfig == null || !pollingConfig.isCollectAshData()) {
            return;
        }
        try {
            DataSource targetDataSource = connectionPoolManager.getDataSource(server.getId());
            JdbcTemplate targetJdbcTemplate = new JdbcTemplate(targetDataSource);
            List<Map<String, Object>> sessions = collectActiveSessions(targetJdbcTemplate, server);
            ashRepository.insertSnapshot(pgmonJdbcTemplate, server, sessions, storeEmpty);
            log.debug("Собрано {} сессий для БД {} (storeEmpty={})", sessions.size(), server.getServerName(), storeEmpty);
        } catch (Exception e) {
            log.error("ASH collection failed for {}: {}", server.getServerName(), e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> collectActiveSessions(JdbcTemplate jdbcTemplate, MonitoredServer server) {
        String sql = """
            SELECT
                pid,
                state,
                wait_event_type,
                wait_event,
                MD5(query) as query_hash,
                LEFT(query, 500) as query,
                EXTRACT(EPOCH FROM (NOW() - query_start)) as duration_seconds
            FROM pg_stat_activity
            WHERE state = 'active'
                AND pid != pg_backend_pid()
                AND query NOT LIKE '%%pg_stat_activity%%'
                AND query NOT LIKE '%%ash_history%%'
        """;
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("Ошибка сбора сессий для {}: {}", server.getServerName(), e.getMessage());
            return List.of();
        }
    }
}
