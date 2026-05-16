package avr.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StatsCacheWrapper {

    private static final Logger log = LoggerFactory.getLogger(StatsCacheWrapper.class);

    @Cacheable(value = CacheNames.DATABASE_STATS, key = "#databaseName")
    public Map<String, Object> getDatabaseStats(String databaseName) {
        log.debug("Cache MISS - fetching database stats for: {}", databaseName);
        return new ConcurrentHashMap<>();
    }

    @Cacheable(value = CacheNames.QUERY_STATS, key = "#databaseName")
    public List<Map<String, Object>> getQueryStats(String databaseName) {
        log.debug("Cache MISS - fetching query stats for: {}", databaseName);
        return List.of();
    }

    @Cacheable(value = CacheNames.CONNECTION_INFO, key = "#databaseName")
    public Map<String, Object> getConnectionInfo(String databaseName) {
        log.debug("Cache MISS - fetching connection info for: {}", databaseName);
        return new ConcurrentHashMap<>();
    }

    @Cacheable(value = CacheNames.TABLE_METRICS, key = "#databaseName")
    public List<Map<String, Object>> getTableMetrics(String databaseName) {
        log.debug("Cache MISS - fetching table metrics for: {}", databaseName);
        return List.of();
    }

    @Cacheable(value = CacheNames.LOCK_INFO, key = "#databaseName")
    public List<Map<String, Object>> getLockInfo(String databaseName) {
        log.debug("Cache MISS - fetching lock info for: {}", databaseName);
        return List.of();
    }

    @CacheEvict(value = CacheNames.DATABASE_STATS, key = "#databaseName")
    public void evictDatabaseStats(String databaseName) {
        log.info("Evicted database stats cache for: {}", databaseName);
    }

    @CacheEvict(value = CacheNames.QUERY_STATS, key = "#databaseName")
    public void evictQueryStats(String databaseName) {
        log.info("Evicted query stats cache for: {}", databaseName);
    }

    @CacheEvict(value = CacheNames.CONNECTION_INFO, key = "#databaseName")
    public void evictConnectionInfo(String databaseName) {
        log.info("Evicted connection info cache for: {}", databaseName);
    }

    @CacheEvict(value = CacheNames.TABLE_METRICS, allEntries = true)
    public void evictAllTableMetrics() {
        log.info("Evicted all table metrics cache");
    }
}
