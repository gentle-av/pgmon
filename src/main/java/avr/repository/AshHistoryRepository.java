package avr.repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import avr.model.MonitoredServer;

@Repository
public class AshHistoryRepository {

  public void insertSnapshot(JdbcTemplate pgmonJdbcTemplate, MonitoredServer server,
      List<Map<String, Object>> sessions) {
    String sql = """
            INSERT INTO ash_history (
                server_id, server_name, snapshot_time, pid, state,
                wait_event_type, wait_event, query_hash, query, duration_seconds
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    LocalDateTime snapshotTime = LocalDateTime.now();
    pgmonJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps, int i) throws java.sql.SQLException {
        Map<String, Object> session = sessions.get(i);
        ps.setString(1, server.getId());
        ps.setString(2, server.getServerName());
        ps.setTimestamp(3, Timestamp.valueOf(snapshotTime));
        ps.setInt(4, ((Number) session.get("pid")).intValue());
        ps.setString(5, (String) session.get("state"));
        ps.setString(6, (String) session.get("wait_event_type"));
        ps.setString(7, (String) session.get("wait_event"));
        ps.setString(8, (String) session.get("query_hash"));
        ps.setString(9, (String) session.get("query"));
        ps.setDouble(10, ((Number) session.get("duration_seconds")).doubleValue());
      }

      @Override
      public int getBatchSize() {
        return sessions.size();
      }
    });
  }

  public List<Map<String, Object>> getAshData(JdbcTemplate pgmonJdbcTemplate, String serverName, int minutes) {
    String sql = """
            SELECT
                DATE_TRUNC('minute', snapshot_time) as time_bucket,
                COALESCE(wait_event_type, 'CPU') as category,
                COUNT(*) as session_count
            FROM ash_history
            WHERE server_name = ?
              AND snapshot_time > NOW() - (? || ' minutes')::INTERVAL
            GROUP BY DATE_TRUNC('minute', snapshot_time), wait_event_type
            ORDER BY time_bucket ASC
        """;
    return pgmonJdbcTemplate.queryForList(sql, serverName, minutes);
  }
}
