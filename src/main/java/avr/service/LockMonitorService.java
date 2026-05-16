package avr.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import avr.cache.CacheNames;
import avr.config.DatabaseConfig;
import avr.model.LockInfo;
import avr.repository.LockRepository;

@Service
public class LockMonitorService {

  private final LockRepository lockRepository;
  private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();

  public LockMonitorService(LockRepository lockRepository) {
    this.lockRepository = lockRepository;
  }

  @Cacheable(value = CacheNames.LOCK_INFO, key = "#dbConfig.name()")
  public List<LockInfo> getBlockingLocks(DatabaseConfig dbConfig) {
    JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
    return lockRepository.getBlockingLocks(jdbcTemplate);
  }

  public boolean hasBlockingLocks(DatabaseConfig dbConfig) {
    List<LockInfo> locks = getBlockingLocks(dbConfig);
    return !locks.isEmpty();
  }

  @CacheEvict(value = CacheNames.LOCK_INFO, key = "#dbConfig.name()")
  public void evictLocks(DatabaseConfig dbConfig) {
    // кэш будет очищен при вызове
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
