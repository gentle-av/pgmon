package avr.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import avr.model.ConnectionInfo;

@Repository
public class ActivityRepository {

  public List<ConnectionInfo> getActiveConnections(JdbcTemplate jdbcTemplate) {
    String sql = """
        SELECT pid, usename, application_name, client_addr,
               backend_start, state, query, wait_event_type, wait_event,
               EXTRACT(EPOCH FROM (now() - query_start)) as duration,
               state = 'idle in transaction' as is_idle_in_transaction
        FROM pg_stat_activity
        WHERE state != 'idle'
        ORDER BY duration DESC NULLS LAST
        """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> new ConnectionInfo(
        rs.getInt("pid"),
        rs.getString("usename"),
        rs.getString("application_name"),
        rs.getString("client_addr"),
        rs.getString("backend_start"),
        rs.getString("state"),
        rs.getString("query"),
        rs.getString("wait_event_type"),
        rs.getString("wait_event"),
        rs.getLong("duration"),
        rs.getBoolean("is_idle_in_transaction")));
  }

  public int getRunningQueriesCount(JdbcTemplate jdbcTemplate) {
    String sql = "SELECT count(*) FROM pg_stat_activity WHERE state = 'active'";
    return jdbcTemplate.queryForObject(sql, Integer.class);
  }

  public int getWaitingQueriesCount(JdbcTemplate jdbcTemplate) {
    String sql = "SELECT count(*) FROM pg_stat_activity WHERE wait_event IS NOT NULL";
    return jdbcTemplate.queryForObject(sql, Integer.class);
  }
}
