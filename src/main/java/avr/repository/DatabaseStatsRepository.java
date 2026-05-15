package avr.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import avr.model.DatabaseStats;
import avr.strategy.PostgresStrategyFactory;
import avr.strategy.PostgresVersionStrategy;

@Repository
public class DatabaseStatsRepository {

  private static final Logger log = LoggerFactory.getLogger(DatabaseStatsRepository.class);
  private final PostgresStrategyFactory strategyFactory;

  public DatabaseStatsRepository(PostgresStrategyFactory strategyFactory) {
    this.strategyFactory = strategyFactory;
  }

  public DatabaseStats getDatabaseStats(JdbcTemplate jdbcTemplate, String dbName) {
    try {
      PostgresVersionStrategy strategy = strategyFactory.getStrategy(jdbcTemplate);
      log.info("Используется стратегия для PostgreSQL {} для статистики БД", strategy.getVersion());
      return strategy.getDatabaseStats(jdbcTemplate, dbName);
    } catch (Exception e) {
      log.error("Ошибка получения статистики БД: {}", e.getMessage());
      return new DatabaseStats(dbName, 0, "0 B", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, null, null, null, 0, 0, 0, 0, 0, 0);
    }
  }
}
