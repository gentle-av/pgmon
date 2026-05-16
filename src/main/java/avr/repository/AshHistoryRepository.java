package avr.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class AshHistoryRepository {

    public void insertSnapshot(JdbcTemplate jdbcTemplate, String databaseName,
                                List<Map<String, Object>> sessions) {
        String sql = """
            INSERT INTO pash_ash_history
            (database_name, snapshot_time, pid, state, wait_event_type, wait_event,
             query_hash, query, duration_seconds, is_active)
            VALUES (?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        for (Map<String, Object> session : sessions) {
            jdbcTemplate.update(sql,
                databaseName,
                session.get("pid"),
                session.get("state"),
                session.get("wait_event_type"),
                session.get("wait_event"),
                session.get("query_hash"),
                session.get("query"),
                session.get("duration_seconds"),
                true
            );
        }
    }

    public List<Map<String, Object>> getAshData(JdbcTemplate jdbcTemplate,
                                                  String databaseName,
                                                  long minutesBack) {
        String sql = """
            SELECT
                DATE_TRUNC('second', snapshot_time) as time_bucket,
                COALESCE(wait_event, 'CPU') as category,
                COUNT(*) as session_count
            FROM pash_ash_history
            WHERE database_name = ?
              AND snapshot_time > NOW() - (? || ' minutes')::INTERVAL
              AND is_active = true
            GROUP BY DATE_TRUNC('second', snapshot_time), category
            ORDER BY time_bucket ASC
        """;

        return jdbcTemplate.queryForList(sql, databaseName, minutesBack);
    }
}
