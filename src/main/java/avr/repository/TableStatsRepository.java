package avr.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import avr.model.TableSizeInfo;

@Repository
public class TableStatsRepository {

  public List<TableSizeInfo> getLargestTables(JdbcTemplate jdbcTemplate, int limit) {
    String sql = """
        SELECT schemaname, tablename,
               pg_table_size(schemaname||'.'||tablename) as table_bytes,
               pg_indexes_size(schemaname||'.'||tablename) as indexes_bytes,
               pg_total_relation_size(schemaname||'.'||tablename) as total_bytes
        FROM pg_tables
        WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
        ORDER BY total_bytes DESC
        LIMIT ?
        """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> new TableSizeInfo(
        rs.getString("schemaname"),
        rs.getString("tablename"),
        rs.getLong("table_bytes"),
        formatBytes(rs.getLong("table_bytes")),
        rs.getLong("indexes_bytes"),
        formatBytes(rs.getLong("indexes_bytes")),
        rs.getLong("total_bytes"),
        formatBytes(rs.getLong("total_bytes"))), limit);
  }

  private String formatBytes(long bytes) {
    if (bytes < 1024)
      return bytes + " B";
    if (bytes < 1024 * 1024)
      return String.format("%.2f KB", bytes / 1024.0);
    if (bytes < 1024 * 1024 * 1024)
      return String.format("%.2f MB", bytes / (1024.0 * 1024));
    return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
  }
}
