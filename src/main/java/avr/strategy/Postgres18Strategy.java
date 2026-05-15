package avr.strategy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import avr.model.DatabaseStats;
import avr.model.IndexInfo;
import avr.model.QueryStats;

public class Postgres18Strategy implements PostgresVersionStrategy {

  private static final Logger log = LoggerFactory.getLogger(Postgres18Strategy.class);

  @Override
  public String getVersion() {
    return "18";
  }

  @Override
  public DatabaseStats getDatabaseStats(JdbcTemplate jdbcTemplate, String dbName) {
    // Запрос с правильными колонками для PostgreSQL 18
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
      return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new DatabaseStats(
          dbName,
          rs.getLong("size_bytes"),
          rs.getString("size_human"),
          rs.getInt("active_conn"),
          rs.getInt("idle_conn"),
          rs.getInt("total_conn"),
          rs.getDouble("cache_hit"),
          rs.getDouble("commit_ratio"),
          rs.getLong("tup_inserted"),
          rs.getLong("tup_updated"),
          rs.getLong("tup_deleted"),
          rs.getLong("tup_fetched"),
          0, // dead_tuples — из другой таблицы
          null, // last_vacuum
          null, // last_autovacuum
          null, // last_analyze
          0.0,
          rs.getInt("active_conn"),
          0,
          0.0,
          rs.getLong("temp_files"),
          rs.getLong("temp_bytes")));
    } catch (Exception e) {
      log.error("Ошибка получения статистики БД (18): {}", e.getMessage());
      return createEmptyStats(dbName);
    }
  }

  @Override
  public List<QueryStats> getSlowestQueries(JdbcTemplate jdbcTemplate, int limit, long thresholdMs) {
    String sql = """
        SELECT queryid, query, datname, usename, calls,
               total_exec_time as total_time_ms,
               mean_exec_time as mean_time_ms,
               max_exec_time as max_time_ms,
               min_exec_time as min_time_ms,
               rows,
               shared_blks_hit, shared_blks_read,
               temp_blks_read, temp_blks_written
        FROM pg_stat_statements
        WHERE mean_exec_time > ?
        ORDER BY mean_exec_time DESC
        LIMIT ?
        """;

    try {
      return jdbcTemplate.query(sql, (rs, rowNum) -> new QueryStats(
          rs.getLong("queryid"),
          rs.getString("query"),
          null,
          rs.getString("datname"),
          rs.getString("usename"),
          rs.getLong("calls"),
          rs.getDouble("total_time_ms"),
          rs.getDouble("mean_time_ms"),
          rs.getDouble("max_time_ms"),
          rs.getDouble("min_time_ms"),
          rs.getLong("rows"),
          rs.getLong("shared_blks_hit"),
          rs.getLong("shared_blks_read"),
          rs.getLong("temp_blks_read"),
          rs.getLong("temp_blks_written")), thresholdMs, limit);
    } catch (Exception e) {
      log.warn("pg_stat_statements (18) не доступен: {}", e.getMessage());
      return List.of();
    }
  }

  @Override
  public List<QueryStats> getMostFrequentQueries(JdbcTemplate jdbcTemplate, int limit) {
    String sql = """
        SELECT queryid, query, datname, usename, calls,
               total_exec_time as total_time_ms,
               mean_exec_time as mean_time_ms,
               max_exec_time as max_time_ms,
               min_exec_time as min_time_ms,
               rows,
               shared_blks_hit, shared_blks_read,
               temp_blks_read, temp_blks_written
        FROM pg_stat_statements
        ORDER BY calls DESC
        LIMIT ?
        """;

    try {
      return jdbcTemplate.query(sql, (rs, rowNum) -> new QueryStats(
          rs.getLong("queryid"),
          rs.getString("query"),
          null,
          rs.getString("datname"),
          rs.getString("usename"),
          rs.getLong("calls"),
          rs.getDouble("total_time_ms"),
          rs.getDouble("mean_time_ms"),
          rs.getDouble("max_time_ms"),
          rs.getDouble("min_time_ms"),
          rs.getLong("rows"),
          rs.getLong("shared_blks_hit"),
          rs.getLong("shared_blks_read"),
          rs.getLong("temp_blks_read"),
          rs.getLong("temp_blks_written")), limit);
    } catch (Exception e) {
      log.warn("pg_stat_statements (18) не доступен: {}", e.getMessage());
      return List.of();
    }
  }

  @Override
  public List<IndexInfo> getUnusedIndexes(JdbcTemplate jdbcTemplate) {
    String sql = """
        SELECT schemaname, tablename, indexname,
               pg_relation_size(indexname::regclass) as size_bytes,
               pg_size_pretty(pg_relation_size(indexname::regclass)) as size_human,
               idx_scan, idx_tup_read, idx_tup_fetch,
               indisprimary as is_primary_key,
               indisunique as is_unique
        FROM pg_indexes i
        JOIN pg_class c ON c.relname = i.indexname
        JOIN pg_index idx ON idx.indexrelid = c.oid
        WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
          AND idx_scan < 100
        ORDER BY idx_scan ASC, size_bytes DESC
        LIMIT 50
        """;

    try {
      return jdbcTemplate.query(sql, (rs, rowNum) -> new IndexInfo(
          rs.getString("schemaname"),
          rs.getString("tablename"),
          rs.getString("indexname"),
          rs.getLong("size_bytes"),
          rs.getString("size_human"),
          rs.getLong("idx_scan"),
          rs.getLong("idx_tup_read"),
          rs.getLong("idx_tup_fetch"),
          0.0,
          rs.getBoolean("is_primary_key"),
          rs.getBoolean("is_unique"),
          ""));
    } catch (Exception e) {
      log.warn("Ошибка получения индексов (18): {}", e.getMessage());
      return List.of();
    }
  }

  @Override
  public String getCacheHitRatioSql() {
    return "SELECT round(blks_hit * 100.0 / NULLIF(blks_hit + blks_read, 0), 2) FROM pg_stat_database WHERE datname = current_database()";
  }

  @Override
  public boolean supportsPgStatStatements() {
    return true;
  }

  private DatabaseStats createEmptyStats(String dbName) {
    return new DatabaseStats(
        dbName, 0, "0 B", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        null, null, null, 0.0, 0, 0, 0.0, 0, 0);
  }
}
