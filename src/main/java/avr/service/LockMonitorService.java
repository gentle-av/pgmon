package avr.service;

import avr.model.LockInfo;
import avr.model.MonitoredServer;
import avr.repository.LockRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.util.List;

@Service
public class LockMonitorService {
    private final LockRepository lockRepository;
    private final ConnectionPoolManager connectionPoolManager;

    public LockMonitorService(LockRepository lockRepository,
                               ConnectionPoolManager connectionPoolManager) {
        this.lockRepository = lockRepository;
        this.connectionPoolManager = connectionPoolManager;
    }

    @Cacheable(value = "lock_info", key = "#server.id")
    public List<LockInfo> getBlockingLocks(MonitoredServer server) {
        DataSource dataSource = connectionPoolManager.getDataSource(server.getId());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return lockRepository.getBlockingLocks(jdbcTemplate);
    }

    public boolean hasBlockingLocks(MonitoredServer server) {
        List<LockInfo> locks = getBlockingLocks(server);
        return !locks.isEmpty();
    }

    @CacheEvict(value = "lock_info", key = "#server.id")
    public void evictLocks(MonitoredServer server) {
    }
}
