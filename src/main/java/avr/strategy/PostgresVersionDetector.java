package avr.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class PostgresVersionDetector {

  private static final Logger log = LoggerFactory.getLogger(PostgresVersionDetector.class);

  public static int detectMajorVersion(JdbcTemplate jdbcTemplate) {
    try {
      String version = jdbcTemplate.queryForObject("SELECT version()", String.class);
      log.info("PostgreSQL version: {}", version);
      java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("PostgreSQL (\\d+)\\.(\\d+)");
      java.util.regex.Matcher matcher = pattern.matcher(version);
      if (matcher.find()) {
        return Integer.parseInt(matcher.group(1));
      }
    } catch (Exception e) {
      log.error("Ошибка определения версии PostgreSQL: {}", e.getMessage());
    }
    return 16;
  }
}
