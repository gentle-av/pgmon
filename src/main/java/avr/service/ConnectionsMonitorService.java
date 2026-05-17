package avr.service;

import avr.config.ConfigRoot;
import avr.model.ConnectionInfo;
import avr.repository.ActivityRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConnectionsMonitorService {
    private final ActivityRepository activityRepository;
    private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();
    public ConnectionsMonitorService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }
    @Cacheable(value = "connection_info", key = "#dbConfig.name")
    public List<ConnectionInfo> getActiveConnections(ConfigRoot.DatabaseConfig dbConfig) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
        return activityRepository.getActiveConnections(jdbcTemplate);
    }
    @Cacheable(value = "connection_info", key = "#dbConfig.name + ':running'")
    public int getRunningQueriesCount(ConfigRoot.DatabaseConfig dbConfig) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
        return activityRepository.getRunningQueriesCount(jdbcTemplate);
    }
    @Cacheable(value = "connection_info", key = "#dbConfig.name + ':waiting'")
    public int getWaitingQueriesCount(ConfigRoot.DatabaseConfig dbConfig) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
        return activityRepository.getWaitingQueriesCount(jdbcTemplate);
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
