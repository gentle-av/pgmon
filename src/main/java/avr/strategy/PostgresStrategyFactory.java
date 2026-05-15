package avr.strategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostgresStrategyFactory {

  private final Map<String, PostgresVersionStrategy> strategies = new ConcurrentHashMap<>();

  public PostgresStrategyFactory() {
    strategies.put("16", new Postgres16Strategy());
    strategies.put("17", new Postgres16Strategy()); // 17 похожа на 16
    strategies.put("18", new Postgres18Strategy());
  }

  public PostgresVersionStrategy getStrategy(JdbcTemplate jdbcTemplate) {
    int majorVersion = PostgresVersionDetector.detectMajorVersion(jdbcTemplate);
    String versionKey = String.valueOf(majorVersion);
    PostgresVersionStrategy strategy = strategies.get(versionKey);
    if (strategy == null) {
      strategy = strategies.getOrDefault("18", strategies.get("16"));
    }
    return strategy;
  }

  public PostgresVersionStrategy getStrategyForVersion(int majorVersion) {
    return strategies.getOrDefault(String.valueOf(majorVersion), strategies.get("16"));
  }
}
