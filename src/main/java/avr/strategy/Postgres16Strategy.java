package avr.strategy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import avr.model.DatabaseStats;
import avr.model.IndexInfo;
import avr.model.QueryStats;

public class Postgres16Strategy implements PostgresVersionStrategy {
  private static final Logger log = LoggerFactory.getLogger(Postgres16Strategy.class);

  public String getVersion() {
    return "16";
  }

  public DatabaseStats getDatabaseStats(JdbcTemplate jdbcTemplate, String dbName) {
    String sql = """
            SELECT
                pg_database_size(current_database()) as size_bytes,
                pg_size_pretty(pg_database_size(current_database())) as size_human,
                (SELECT count(*) FROM pg_stat_activity WHERE state = 'active') as active_conn,
                (SELECT count(*) FROM pg_stat_activity WHERE state = 'idle') as idle_conn,
                (SELECT count(*) FROM pg_stat_activity) as total_conn,
                round(blks_hit * 100.0 / NULLIF(blks_hit + blks_read, 0), 2) as cache_hit,
                round(xact_commit * 100.0 / NULLIF(xact_commit + xact_rollback, 0), 2) as commit_ratio,
                tup_inserted, tup_updated, tup_deleted, tup_fetched,
                temp_files, temp_bytes
            FROM pg_stat_database
            WHERE datname = current_database()
        """;
    try {
      return jdbcTemplate.queryForObject(sql,
          (rs, rowNum) -> new DatabaseStats(dbName, rs.getLong("size_bytes"), rs.getString("size_human"),
              rs.getInt("active_conn"), rs.getInt("idle_conn"), rs.getInt("total_conn"), rs.getDouble("cache_hit"),
              rs.getDouble("commit_ratio"), rs.getLong("tup_inserted"), rs.getLong("tup_updated"),
              rs.getLong("tup_deleted"), rs.getLong("tup_fetched"), 0, null, null, null, 0.0, rs.getInt("active_conn"),
              0, 0.0, rs.getLong("temp_files"), rs.getLong("temp_bytes")));
    } catch (Exception e) {
      log.error("Ошибка получения статистики БД (16): {}", e.getMessage());
      return createEmptyStats(dbName);
    }
  }

  public List<QueryStats> getSlowestQueries(JdbcTemplate jdbcTemplate, int limit, long thresholdMs) {
    String sql = """
            SELECT queryid, query, datname, usename, calls,
                   total_time as total_time_ms,
                   mean_time as mean_time_ms,
                   max_time as max_time_ms,
                   min_time as min_time_ms,
                   rows,
                   shared_blks_hit, shared_blks_read,
                   temp_blks_read, temp_blks_written
            FROM pg_stat_statements
            WHERE mean_time > ?
            ORDER BY mean_time DESC
            LIMIT ?
        """;
    try {
      return jdbcTemplate.query(sql,
          (rs, rowNum) -> new QueryStats(rs.getLong("queryid"), rs.getString("query"), null, rs.getString("datname"),
              rs.getString("usename"), rs.getLong("calls"), rs.getDouble("total_time_ms"), rs.getDouble("mean_time_ms"),
              rs.getDouble("max_time_ms"), rs.getDouble("min_time_ms"), rs.getLong("rows"),
              rs.getLong("shared_blks_hit"), rs.getLong("shared_blks_read"), rs.getLong("temp_blks_read"),
              rs.getLong("temp_blks_written")),
          thresholdMs, limit);
    } catch (Exception e) {
      log.warn("pg_stat_statements (16) не доступен: {}", e.getMessage());
      return List.of();
    }
  }

  public List<QueryStats> getMostFrequentQueries(JdbcTemplate jdbcTemplate, int limit) {
    String sql = """
            SELECT queryid, query, datname, usename, calls,
                   total_time as total_time_ms,
                   mean_time as mean_time_ms,
                   max_time as max_time_ms,
                   min_time as min_time_ms,
                   rows,
                   shared_blks_hit, shared_blks_read,
                   temp_blks_read, temp_blks_written
            FROM pg_stat_statements
            ORDER BY calls DESC
            LIMIT ?
        """;
    try {
      return jdbcTemplate.query(sql,
          (rs, rowNum) -> new QueryStats(rs.getLong("queryid"), rs.getString("query"), null, rs.getString("datname"),
              rs.getString("usename"), rs.getLong("calls"), rs.getDouble("total_time_ms"), rs.getDouble("mean_time_ms"),
              rs.getDouble("max_time_ms"), rs.getDouble("min_time_ms"), rs.getLong("rows"),
              rs.getLong("shared_blks_hit"), rs.getLong("shared_blks_read"), rs.getLong("temp_blks_read"),
              rs.getLong("temp_blks_written")),
          limit);
    } catch (Exception e) {
      log.warn("pg_stat_statements (16) не доступен: {}", e.getMessage());
      return List.of();
    }
  }

  public List<IndexInfo> getUnusedIndexes(JdbcTemplate jdbcTemplate) {
    return List.of();
  }

  public String getCacheHitRatioSql() {
    return "SELECT round(blks_hit * 100.0 / NULLIF(blks_hit + blks_read, 0), 2) FROM pg_stat_database WHERE datname = current_database()";
  }

  public boolean supportsPgStatStatements() {
    return true;
  }

  private DatabaseStats createEmptyStats(String dbName) {
    return new DatabaseStats(dbName, 0, "0 B", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, null, null, null, 0.0, 0, 0, 0.0, 0, 0);
  }
}
