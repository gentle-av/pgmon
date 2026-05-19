// ActiveSessionsCollectorService.java
package avr.service;

import java.time.LocalDateTime;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import avr.model.MonitoredServer;
import avr.repository.MonitoredServerRepository;

@Service
public class ActiveSessionsCollectorService {
  private final MonitoredServerRepository serverRepository;
  private final ConnectionPoolManager connectionPoolManager;
  private static final Logger log = LoggerFactory.getLogger(ActiveSessionsCollectorService.class);

  public ActiveSessionsCollectorService(MonitoredServerRepository serverRepository,
      ConnectionPoolManager connectionPoolManager) {
    this.serverRepository = serverRepository;
    this.connectionPoolManager = connectionPoolManager;
  }

  @Scheduled(fixedDelay = 1000)
  public void collectActiveSessions() {
    List<MonitoredServer> servers = serverRepository.findByEnabledTrue();
    for (MonitoredServer server : servers) {
      try {
        DataSource dataSource = connectionPoolManager.getDataSource(server.getId());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = """
                INSERT INTO active_sessions_history (server_id, snapshot_time, pid, username, state, wait_event_type, wait_event, query, duration_seconds)
                SELECT ?, ?, pid, usename, state, wait_event_type, wait_event, query, EXTRACT(EPOCH FROM (NOW() - query_start))
                FROM pg_stat_activity
                WHERE state = 'active' AND pid != pg_backend_pid()
            """;
        jdbcTemplate.update(sql, server.getId(), LocalDateTime.now());
      } catch (Exception e) {
        log.error("Failed to collect active sessions for {}: {}", server.getServerName(), e.getMessage());
      }
    }
  }
}
