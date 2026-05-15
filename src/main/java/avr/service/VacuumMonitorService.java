package avr.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import avr.config.DatabaseConfig;
import avr.model.VacuumInfo;

@Service
public class VacuumMonitorService {

  private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();

  public List<VacuumInfo> getVacuumStats(DatabaseConfig dbConfig) {
    JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
    String sql = """
        SELECT schemaname, relname as table_name,
               n_dead_tup as dead_tuples,
               n_live_tup as live_tuples,
               CASE WHEN n_live_tup > 0
                    THEN round(100.0 * n_dead_tup / (n_live_tup + n_dead_tup), 2)
                    ELSE 0 END as dead_tuple_ratio,
               to_char(last_vacuum, 'YYYY-MM-DD HH24:MI:SS') as last_vacuum,
               to_char(last_autovacuum, 'YYYY-MM-DD HH24:MI:SS') as last_autovacuum,
               to_char(last_analyze, 'YYYY-MM-DD HH24:MI:SS') as last_analyze,
               vacuum_count, autovacuum_count
        FROM pg_stat_user_tables
        WHERE n_dead_tup > 1000
        ORDER BY n_dead_tup DESC
        """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> new VacuumInfo(
        rs.getString("schemaname"),
        rs.getString("table_name"),
        rs.getLong("dead_tuples"),
        rs.getLong("live_tuples"),
        rs.getDouble("dead_tuple_ratio"),
        rs.getString("last_vacuum"),
        rs.getString("last_autovacuum"),
        rs.getString("last_analyze"),
        rs.getLong("vacuum_count"),
        rs.getLong("autovacuum_count")));
  }

  private JdbcTemplate getJdbcTemplate(DatabaseConfig dbConfig) {
    return jdbcTemplates.computeIfAbsent(dbConfig.name(), key -> {
      var builder = org.springframework.boot.jdbc.DataSourceBuilder.create();
      builder.url(dbConfig.getJdbcUrl());
      builder.username(dbConfig.username());
      builder.password(dbConfig.password());
      builder.driverClassName("org.postgresql.Driver");
      return new JdbcTemplate(builder.build());
    });
  }
}
