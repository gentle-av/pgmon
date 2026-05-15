package avr.repository;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import avr.model.QueryStats;
import avr.strategy.PostgresStrategyFactory;
import avr.strategy.PostgresVersionStrategy;

@Repository
public class QueryStatsRepository {

  private static final Logger log = LoggerFactory.getLogger(QueryStatsRepository.class);
  private final PostgresStrategyFactory strategyFactory;

  public QueryStatsRepository(PostgresStrategyFactory strategyFactory) {
    this.strategyFactory = strategyFactory;
  }

  public List<QueryStats> getSlowestQueries(JdbcTemplate jdbcTemplate, int limit, long thresholdMs) {
    try {
      PostgresVersionStrategy strategy = strategyFactory.getStrategy(jdbcTemplate);
      log.info("Используется стратегия для PostgreSQL {} для slow queries", strategy.getVersion());
      return strategy.getSlowestQueries(jdbcTemplate, limit, thresholdMs);
    } catch (Exception e) {
      log.error("Ошибка получения медленных запросов: {}", e.getMessage());
      return List.of();
    }
  }

  public List<QueryStats> getMostFrequentQueries(JdbcTemplate jdbcTemplate, int limit) {
    try {
      PostgresVersionStrategy strategy = strategyFactory.getStrategy(jdbcTemplate);
      log.info("Используется стратегия для PostgreSQL {} для frequent queries", strategy.getVersion());
      return strategy.getMostFrequentQueries(jdbcTemplate, limit);
    } catch (Exception e) {
      log.error("Ошибка получения частых запросов: {}", e.getMessage());
      return List.of();
    }
  }
}
