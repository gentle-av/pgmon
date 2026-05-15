package avr.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import avr.model.LockInfo;

@Repository
public class LockRepository {

  public List<LockInfo> getBlockingLocks(JdbcTemplate jdbcTemplate) {
    String sql = """
        SELECT blocked_locks.pid AS blocked_pid,
               blocked_activity.usename AS blocked_username,
               blocked_activity.query AS blocked_query,
               blocking_locks.pid AS blocking_pid,
               blocking_activity.usename AS blocking_username,
               blocking_activity.query AS blocking_query,
               blocked_locks.locktype,
               blocked_locks.relation::regclass::text AS relation,
               EXTRACT(EPOCH FROM (now() - blocked_activity.query_start)) AS wait_seconds
        FROM pg_locks blocked_locks
        JOIN pg_stat_activity blocked_activity ON blocked_activity.pid = blocked_locks.pid
        JOIN pg_locks blocking_locks ON blocking_locks.locktype = blocked_locks.locktype
            AND blocking_locks.database IS NOT DISTINCT FROM blocked_locks.database
            AND blocking_locks.relation IS NOT DISTINCT FROM blocked_locks.relation
            AND blocking_locks.page IS NOT DISTINCT FROM blocked_locks.page
            AND blocking_locks.tuple IS NOT DISTINCT FROM blocked_locks.tuple
            AND blocking_locks.virtualxid IS NOT DISTINCT FROM blocked_locks.virtualxid
            AND blocking_locks.transactionid IS NOT DISTINCT FROM blocked_locks.transactionid
            AND blocking_locks.classid IS NOT DISTINCT FROM blocked_locks.classid
            AND blocking_locks.objid IS NOT DISTINCT FROM blocked_locks.objid
            AND blocking_locks.objsubid IS NOT DISTINCT FROM blocked_locks.objsubid
            AND blocking_locks.pid != blocked_locks.pid
        JOIN pg_stat_activity blocking_activity ON blocking_activity.pid = blocking_locks.pid
        WHERE NOT blocked_locks.granted
        """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> new LockInfo(
        rs.getInt("blocked_pid"),
        rs.getString("blocked_username"),
        rs.getString("blocked_query"),
        rs.getInt("blocking_pid"),
        rs.getString("blocking_username"),
        rs.getString("blocking_query"),
        rs.getString("locktype"),
        rs.getString("relation"),
        rs.getInt("wait_seconds")));
  }
}
