package avr.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import avr.model.ConnectionInfo;
import avr.model.MonitoredServer;
import avr.repository.ActivityRepository;

@Service
public class ConnectionsMonitorService {
  private final ActivityRepository activityRepository;
  private final ConnectionPoolManager connectionPoolManager;

  public ConnectionsMonitorService(ActivityRepository activityRepository, ConnectionPoolManager connectionPoolManager) {
    this.activityRepository = activityRepository;
    this.connectionPoolManager = connectionPoolManager;
  }

  @Cacheable(value = "connection_info", key = "#server.id")
  public List<ConnectionInfo> getActiveConnections(MonitoredServer server) {
    DataSource dataSource = connectionPoolManager.getDataSource(server.getId());
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    return activityRepository.getActiveConnections(jdbcTemplate);
  }

  @Cacheable(value = "connection_info", key = "#server.id + ':running'")
  public int getRunningQueriesCount(MonitoredServer server) {
    DataSource dataSource = connectionPoolManager.getDataSource(server.getId());
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    return activityRepository.getRunningQueriesCount(jdbcTemplate);
  }

  @Cacheable(value = "connection_info", key = "#server.id + ':waiting'")
  public int getWaitingQueriesCount(MonitoredServer server) {
    DataSource dataSource = connectionPoolManager.getDataSource(server.getId());
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    return activityRepository.getWaitingQueriesCount(jdbcTemplate);
  }

  public List<Map<String, Object>> getActiveSessionsInTimeRange(MonitoredServer server, LocalDateTime from,
      LocalDateTime to) {
    DataSource dataSource = connectionPoolManager.getDataSource(server.getId());
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    String sql = """
            SELECT
                to_char(query_start, 'YYYY-MM-DD HH24:MI:SS') as start_time,
                EXTRACT(EPOCH FROM (NOW() - query_start)) as duration_seconds,
                usename as username,
                state,
                wait_event_type,
                wait_event,
                LEFT(query, 200) as query_preview,
                pid
            FROM pg_stat_activity
            WHERE state = 'active'
              AND pid != pg_backend_pid()
              AND query_start BETWEEN ? AND ?
            ORDER BY query_start DESC
        """;
    return jdbcTemplate.queryForList(sql, from, to);
  }

  public List<Map<String, Object>> getActiveSessionsHistory(MonitoredServer server, LocalDateTime from,
      LocalDateTime to) {
    DataSource dataSource = connectionPoolManager.getDataSource(server.getId());
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    String sql = """
            SELECT
                DATE_TRUNC('minute', snapshot_time) as time_bucket,
                COUNT(*) as active_sessions_count,
                COUNT(DISTINCT pid) as unique_pids
            FROM pash_ash_history
            WHERE server_id = ?
              AND snapshot_time BETWEEN ? AND ?
              AND is_active = true
            GROUP BY DATE_TRUNC('minute', snapshot_time)
            ORDER BY time_bucket ASC
        """;
    return jdbcTemplate.queryForList(sql, server.getId(), from, to);
  }

  public List<Map<String, Object>> getActiveSessionsDetails(MonitoredServer server) {
    DataSource dataSource = connectionPoolManager.getDataSource(server.getId());
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    String sql = """
            SELECT
                pid,
                usename as username,
                application_name,
                client_addr,
                state,
                wait_event_type,
                wait_event,
                LEFT(query, 200) as query_preview,
                EXTRACT(EPOCH FROM (NOW() - query_start)) as duration_seconds,
                to_char(query_start, 'HH24:MI:SS') as start_time
            FROM pg_stat_activity
            WHERE state = 'active'
              AND pid != pg_backend_pid()
            ORDER BY duration_seconds DESC
            LIMIT 100
        """;
    return jdbcTemplate.queryForList(sql);
  }
}
