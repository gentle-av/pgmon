package avr.controller;

import avr.config.ConfigRoot;
import avr.config.MonitoringConfig;
import avr.model.TableSizeInfo;
import avr.repository.TableStatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/tables")
public class TablesController {
    private static final Logger log = LoggerFactory.getLogger(TablesController.class);
    private final MonitoringConfig monitoringConfig;
    private final TableStatsRepository tableStatsRepository;
    private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();
    public TablesController(MonitoringConfig monitoringConfig, TableStatsRepository tableStatsRepository) {
        this.monitoringConfig = monitoringConfig;
        this.tableStatsRepository = tableStatsRepository;
    }
    @GetMapping("/largest/{dbName}")
    public List<TableSizeInfo> getLargestTables(@PathVariable String dbName, @RequestParam(defaultValue = "20") int limit) {
        ConfigRoot.DatabaseConfig dbConfig = findDatabase(dbName);
        if (dbConfig == null) {
            log.warn("База данных {} не найдена", dbName);
            return List.of();
        }
        if (!dbConfig.isEnabled()) {
            log.warn("База данных {} отключена", dbName);
            return List.of();
        }
        try {
            JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
            return tableStatsRepository.getLargestTables(jdbcTemplate, limit);
        } catch (Exception e) {
            log.error("Ошибка получения таблиц для {}: {}", dbName, e.getMessage());
            return List.of();
        }
    }
    private JdbcTemplate getJdbcTemplate(ConfigRoot.DatabaseConfig dbConfig) {
        return jdbcTemplates.computeIfAbsent(dbConfig.getName(), key -> {
            try {
                var builder = org.springframework.boot.jdbc.DataSourceBuilder.create();
                builder.url(String.format("jdbc:postgresql://%s:%d/%s", dbConfig.getHost(), dbConfig.getPort(), dbConfig.getDatabase()));
                builder.username(dbConfig.getUsername());
                builder.password(dbConfig.getPassword());
                builder.driverClassName("org.postgresql.Driver");
                return new JdbcTemplate(builder.build());
            } catch (Exception e) {
                log.error("Ошибка создания DataSource для {}: {}", dbConfig.getName(), e.getMessage());
                throw new RuntimeException("Cannot create DataSource for " + dbConfig.getName(), e);
            }
        });
    }
    private ConfigRoot.DatabaseConfig findDatabase(String name) {
        return monitoringConfig.getDatabaseConfigs().stream()
            .filter(db -> db.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
}
