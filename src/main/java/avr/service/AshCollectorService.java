package avr.service;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import avr.model.MonitoredServer;
import avr.model.PollingConfiguration;
import avr.repository.AshHistoryRepository;
import avr.repository.MonitoredServerRepository;
import avr.repository.PollingConfigurationRepository;
import jakarta.annotation.PostConstruct;

@Service
public class AshCollectorService {
  private static final Logger log = LoggerFactory.getLogger(AshCollectorService.class);
  private final AshHistoryRepository ashRepository;
  private final DatabaseInitializationService dbInitService;
  private final ConnectionPoolManager connectionPoolManager;
  private final MonitoredServerRepository serverRepository;
  private final PollingConfigurationRepository pollingConfigRepository;
  private final DataSource pgmonDataSource;

  public AshCollectorService(AshHistoryRepository ashRepository, DatabaseInitializationService dbInitService,
      ConnectionPoolManager connectionPoolManager, MonitoredServerRepository serverRepository,
      PollingConfigurationRepository pollingConfigRepository, DataSource pgmonDataSource) {
    this.ashRepository = ashRepository;
    this.dbInitService = dbInitService;
    this.connectionPoolManager = connectionPoolManager;
    this.serverRepository = serverRepository;
    this.pollingConfigRepository = pollingConfigRepository;
    this.pgmonDataSource = pgmonDataSource;
  }

  @PostConstruct
  public void init() {
    log.info("AshCollectorService инициализирован");
  }

  public void collectAshSnapshotsForServer(MonitoredServer server) {
    log.info("=== DEBUG: collectAshSnapshotsForServer called for {}", server.getServerName());
    log.info("=== DEBUG: pgmonDataSource = {}", pgmonDataSource);
    PollingConfiguration pollingConfig = pollingConfigRepository.findByServerId(server.getId()).orElse(null);
    if (pollingConfig == null || !pollingConfig.isCollectAshData()) {
      log.info("=== DEBUG: ASH data collection disabled for {}", server.getServerName());
      return;
    }
    try {
      DataSource targetDataSource = connectionPoolManager.getDataSource(server.getId());
      JdbcTemplate targetJdbcTemplate = new JdbcTemplate(targetDataSource);
      List<Map<String, Object>> sessions = collectActiveSessions(targetJdbcTemplate, server);
      log.info("=== DEBUG: Collected {} sessions from target", sessions.size());
      if (!sessions.isEmpty()) {
        JdbcTemplate pgmonJdbcTemplate = new JdbcTemplate(pgmonDataSource);
        Integer test = pgmonJdbcTemplate.queryForObject("SELECT 1", Integer.class);
        log.info("=== DEBUG: pgmon connection OK, test result = {}", test);
        ashRepository.insertSnapshot(pgmonJdbcTemplate, server, sessions);
        log.debug("Собрано {} сессий для БД {}", sessions.size(), server.getServerName());
      }
    } catch (Exception e) {
      log.error("ASH collection failed for {}: {}", server.getServerName(), e.getMessage(), e);
    }
  }

  @Scheduled(fixedDelay = 1000)
  public void collectAllServersAshSnapshots() {
    List<MonitoredServer> servers = serverRepository.findByEnabledTrue();
    for (MonitoredServer server : servers) {
      collectAshSnapshotsForServer(server);
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

  @Scheduled(cron = "0 0 * * * *")
  public void cleanOldData() {
    JdbcTemplate pgmonJdbcTemplate = new JdbcTemplate(pgmonDataSource);
    Integer deleted = pgmonJdbcTemplate.queryForObject("SELECT clean_old_ash_data(7)", Integer.class);
    if (deleted != null && deleted > 0) {
      log.info("Очищено {} записей из ASH", deleted);
    }
  }
}
