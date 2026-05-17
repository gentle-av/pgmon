package avr.service;

import avr.config.ConfigRoot;
import avr.model.LockInfo;
import avr.repository.LockRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LockMonitorService {
    private final LockRepository lockRepository;
    private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();
    public LockMonitorService(LockRepository lockRepository) {
        this.lockRepository = lockRepository;
    }
    @Cacheable(value = "lock_info", key = "#dbConfig.name")
    public List<LockInfo> getBlockingLocks(ConfigRoot.DatabaseConfig dbConfig) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
        return lockRepository.getBlockingLocks(jdbcTemplate);
    }
    public boolean hasBlockingLocks(ConfigRoot.DatabaseConfig dbConfig) {
        List<LockInfo> locks = getBlockingLocks(dbConfig);
        return !locks.isEmpty();
    }
    @CacheEvict(value = "lock_info", key = "#dbConfig.name")
    public void evictLocks(ConfigRoot.DatabaseConfig dbConfig) {
    }
    private JdbcTemplate getJdbcTemplate(ConfigRoot.DatabaseConfig dbConfig) {
        return jdbcTemplates.computeIfAbsent(dbConfig.getName(), key -> {
            var builder = org.springframework.boot.jdbc.DataSourceBuilder.create();
            builder.url(String.format("jdbc:postgresql://%s:%d/%s", dbConfig.getHost(), dbConfig.getPort(), dbConfig.getDatabase()));
            builder.username(dbConfig.getUsername());
            builder.password(dbConfig.getPassword());
            builder.driverClassName("org.postgresql.Driver");
            return new JdbcTemplate(builder.build());
        });
    }
}
