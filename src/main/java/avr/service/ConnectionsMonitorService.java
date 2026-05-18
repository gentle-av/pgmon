package avr.service;

import avr.model.ConnectionInfo;
import avr.model.MonitoredServer;
import avr.repository.ActivityRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.util.List;

@Service
public class ConnectionsMonitorService {
    private final ActivityRepository activityRepository;
    private final ConnectionPoolManager connectionPoolManager;

    public ConnectionsMonitorService(ActivityRepository activityRepository,
                                      ConnectionPoolManager connectionPoolManager) {
        this.activityRepository = activityRepository;
        this.connectionPoolManager = connectionPoolManager;
    }

    @Cacheable(value = "connection_info", key = "#server.id")
    public List<ConnectionInfo> getActiveConnections(MonitoredServer server) {
        DataSource dataSource = connectionPoolManager.getDataSource(server.getId());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return activityRepository.getActiveConnections(jdbcTemplate);
    }

    @Cacheable(value = "connection_info", key = "#server.id + ':running'")
    public int getRunningQueriesCount(MonitoredServer server) {
        DataSource dataSource = connectionPoolManager.getDataSource(server.getId());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return activityRepository.getRunningQueriesCount(jdbcTemplate);
    }

    @Cacheable(value = "connection_info", key = "#server.id + ':waiting'")
    public int getWaitingQueriesCount(MonitoredServer server) {
        DataSource dataSource = connectionPoolManager.getDataSource(server.getId());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return activityRepository.getWaitingQueriesCount(jdbcTemplate);
    }
}
