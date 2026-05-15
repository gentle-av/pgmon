package avr.strategy;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import avr.model.DatabaseStats;
import avr.model.IndexInfo;
import avr.model.QueryStats;

public interface PostgresVersionStrategy {

  String getVersion();

  DatabaseStats getDatabaseStats(JdbcTemplate jdbcTemplate, String dbName);

  List<QueryStats> getSlowestQueries(JdbcTemplate jdbcTemplate, int limit, long thresholdMs);

  List<QueryStats> getMostFrequentQueries(JdbcTemplate jdbcTemplate, int limit);

  List<IndexInfo> getUnusedIndexes(JdbcTemplate jdbcTemplate);

  String getCacheHitRatioSql();

  boolean supportsPgStatStatements();
}
