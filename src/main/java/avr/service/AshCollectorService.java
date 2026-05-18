package avr.service;

import avr.model.MonitoredServer;
import avr.model.PollingConfiguration;
import avr.repository.AshHistoryRepository;
import avr.repository.MonitoredServerRepository;
import avr.repository.PollingConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Service
public class AshCollectorService {
    private static final Logger log = LoggerFactory.getLogger(AshCollectorService.class);
    private final AshHistoryRepository ashRepository;
    private final DatabaseInitializationService dbInitService;
    private final ConnectionPoolManager connectionPoolManager;
    private final MonitoredServerRepository serverRepository;
    private final PollingConfigurationRepository pollingConfigRepository;

    public AshCollectorService(AshHistoryRepository ashRepository,
                                DatabaseInitializationService dbInitService,
                                ConnectionPoolManager connectionPoolManager,
                                MonitoredServerRepository serverRepository,
                                PollingConfigurationRepository pollingConfigRepository) {
        this.ashRepository = ashRepository;
        this.dbInitService = dbInitService;
        this.connectionPoolManager = connectionPoolManager;
        this.serverRepository = serverRepository;
        this.pollingConfigRepository = pollingConfigRepository;
    }

    @PostConstruct
    public void init() {
        log.info("AshCollectorService инициализирован");
    }

    @Scheduled(fixedDelay = 2000)
    public void collectAshSnapshots() {
        List<MonitoredServer> servers = serverRepository.findByEnabledTrue();
        for (MonitoredServer server : servers) {
            if (!dbInitService.isInitialized(server.getServerName())) {
                log.debug("БД {} не инициализирована, пропускаем сбор", server.getServerName());
                continue;
            }
            PollingConfiguration pollingConfig = pollingConfigRepository.findByServerId(server.getId()).orElse(null);
            if (pollingConfig == null || !pollingConfig.isCollectAshData()) {
                continue;
            }
            try {
                DataSource dataSource = connectionPoolManager.getDataSource(server.getId());
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                List<Map<String, Object>> sessions = collectActiveSessions(jdbcTemplate, server);
                if (!sessions.isEmpty()) {
                    ashRepository.insertSnapshot(jdbcTemplate, server.getServerName(), sessions);
                    log.debug("Собрано {} сессий для БД {}", sessions.size(), server.getServerName());
                }
            } catch (Exception e) {
                log.error("ASH collection failed for {}: {}", server.getServerName(), e.getMessage());
            }
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
              AND query NOT LIKE '%%pash_ash_history%%'
        """;
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("Ошибка сбора сессий для {}: {}", server.getServerName(), e.getMessage());
            return List.of();
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanOldData() {
        List<MonitoredServer> servers = serverRepository.findByEnabledTrue();
        for (MonitoredServer server : servers) {
            if (!dbInitService.isInitialized(server.getServerName())) continue;
            try {
                DataSource dataSource = connectionPoolManager.getDataSource(server.getId());
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                Integer deleted = jdbcTemplate.queryForObject(
                    "SELECT clean_old_ash_data(7)",
                    Integer.class
                );
                if (deleted != null && deleted > 0) {
                    log.info("Очищено {} записей из ASH для БД {}", deleted, server.getServerName());
                }
            } catch (Exception e) {
                log.error("Ошибка очистки ASH для {}: {}", server.getServerName(), e.getMessage());
            }
        }
    }
}
