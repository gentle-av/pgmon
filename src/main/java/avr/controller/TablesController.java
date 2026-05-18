package avr.controller;

import avr.model.MonitoredServer;
import avr.model.TableSizeInfo;
import avr.repository.MonitoredServerRepository;
import avr.repository.TableStatsRepository;
import avr.service.ConnectionPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/tables")
public class TablesController {
    private static final Logger log = LoggerFactory.getLogger(TablesController.class);
    private final MonitoredServerRepository serverRepository;
    private final TableStatsRepository tableStatsRepository;
    private final ConnectionPoolManager connectionPoolManager;
    private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();

    public TablesController(MonitoredServerRepository serverRepository,
                            TableStatsRepository tableStatsRepository,
                            ConnectionPoolManager connectionPoolManager) {
        this.serverRepository = serverRepository;
        this.tableStatsRepository = tableStatsRepository;
        this.connectionPoolManager = connectionPoolManager;
    }

    @GetMapping("/largest/{serverId}")
    public List<TableSizeInfo> getLargestTables(@PathVariable String serverId, @RequestParam(defaultValue = "20") int limit) {
        MonitoredServer server = serverRepository.findById(serverId).orElse(null);
        if (server == null) {
            log.warn("Сервер {} не найден", serverId);
            return List.of();
        }
        if (!server.isEnabled()) {
            log.warn("Сервер {} отключен", server.getServerName());
            return List.of();
        }
        try {
            JdbcTemplate jdbcTemplate = getJdbcTemplate(server);
            return tableStatsRepository.getLargestTables(jdbcTemplate, limit);
        } catch (Exception e) {
            log.error("Ошибка получения таблиц для {}: {}", server.getServerName(), e.getMessage());
            return List.of();
        }
    }

    private JdbcTemplate getJdbcTemplate(MonitoredServer server) {
        return jdbcTemplates.computeIfAbsent(server.getId(), key -> {
            try {
                DataSource dataSource = connectionPoolManager.getDataSource(server.getId());
                return new JdbcTemplate(dataSource);
            } catch (Exception e) {
                log.error("Ошибка создания DataSource для {}: {}", server.getServerName(), e.getMessage());
                throw new RuntimeException("Cannot create DataSource for " + server.getServerName(), e);
            }
        });
    }
}
