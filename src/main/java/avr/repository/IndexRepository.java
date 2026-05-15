package avr.repository;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import avr.model.IndexInfo;
import avr.strategy.PostgresStrategyFactory;
import avr.strategy.PostgresVersionStrategy;

@Repository
public class IndexRepository {

  private static final Logger log = LoggerFactory.getLogger(IndexRepository.class);
  private final PostgresStrategyFactory strategyFactory;

  public IndexRepository(PostgresStrategyFactory strategyFactory) {
    this.strategyFactory = strategyFactory;
  }

  public List<IndexInfo> getUnusedIndexes(JdbcTemplate jdbcTemplate) {
    try {
      PostgresVersionStrategy strategy = strategyFactory.getStrategy(jdbcTemplate);
      log.info("Используется стратегия для PostgreSQL {} для индексов", strategy.getVersion());
      return strategy.getUnusedIndexes(jdbcTemplate);
    } catch (Exception e) {
      log.error("Ошибка получения неиспользуемых индексов: {}", e.getMessage());
      return List.of();
    }
  }
}
