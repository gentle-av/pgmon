package avr.controller;

import avr.config.ConfigRoot;
import avr.config.MonitoringConfig;
import avr.model.ConnectionInfo;
import avr.model.IndexInfo;
import avr.model.LockInfo;
import avr.model.QueryStats;
import avr.model.VacuumInfo;
import avr.service.*;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {
    private final MonitoringConfig monitoringConfig;
    private final DatabaseMonitorService databaseMonitorService;
    private final QueryAnalysisService queryAnalysisService;
    private final LockMonitorService lockMonitorService;
    private final IndexAnalysisService indexAnalysisService;
    private final ConnectionsMonitorService connectionsMonitorService;
    private final VacuumMonitorService vacuumMonitorService;
    public MetricsController(MonitoringConfig monitoringConfig,
                             DatabaseMonitorService databaseMonitorService,
                             QueryAnalysisService queryAnalysisService,
                             LockMonitorService lockMonitorService,
                             IndexAnalysisService indexAnalysisService,
                             ConnectionsMonitorService connectionsMonitorService,
                             VacuumMonitorService vacuumMonitorService) {
        this.monitoringConfig = monitoringConfig;
        this.databaseMonitorService = databaseMonitorService;
        this.queryAnalysisService = queryAnalysisService;
        this.lockMonitorService = lockMonitorService;
        this.indexAnalysisService = indexAnalysisService;
        this.connectionsMonitorService = connectionsMonitorService;
        this.vacuumMonitorService = vacuumMonitorService;
    }
    @GetMapping("/all/{dbName}")
    public Map<String, Object> getAllMetrics(@PathVariable String dbName) {
        ConfigRoot.DatabaseConfig dbConfig = findDatabase(dbName);
        if (dbConfig == null) {
            return Map.of("error", "Database not found: " + dbName);
        }
        Map<String, Object> metrics = new HashMap<>();
        try {
            metrics.put("database_stats", databaseMonitorService.collectStats(dbConfig));
        } catch (Exception e) {
            metrics.put("database_stats_error", e.getMessage());
        }
        try {
            metrics.put("slow_queries", queryAnalysisService.getSlowestQueries(dbConfig));
        } catch (Exception e) {
            metrics.put("slow_queries_error", e.getMessage());
        }
        try {
            metrics.put("frequent_queries", queryAnalysisService.getMostFrequentQueries(dbConfig));
        } catch (Exception e) {
            metrics.put("frequent_queries_error", e.getMessage());
        }
        try {
            metrics.put("blocking_locks", lockMonitorService.getBlockingLocks(dbConfig));
        } catch (Exception e) {
            metrics.put("blocking_locks_error", e.getMessage());
        }
        try {
            metrics.put("unused_indexes", indexAnalysisService.getUnusedIndexes(dbConfig));
        } catch (Exception e) {
            metrics.put("unused_indexes_error", e.getMessage());
        }
        try {
            metrics.put("active_connections", connectionsMonitorService.getActiveConnections(dbConfig));
        } catch (Exception e) {
            metrics.put("active_connections_error", e.getMessage());
        }
        try {
            metrics.put("vacuum_stats", vacuumMonitorService.getVacuumStats(dbConfig));
        } catch (Exception e) {
            metrics.put("vacuum_stats_error", e.getMessage());
        }
        try {
            metrics.put("running_queries_count", connectionsMonitorService.getRunningQueriesCount(dbConfig));
        } catch (Exception e) {
            metrics.put("running_queries_count_error", e.getMessage());
        }
        try {
            metrics.put("waiting_queries_count", connectionsMonitorService.getWaitingQueriesCount(dbConfig));
        } catch (Exception e) {
            metrics.put("waiting_queries_count_error", e.getMessage());
        }
        return metrics;
    }
    @GetMapping("/connections/{dbName}")
    public List<ConnectionInfo> getConnections(@PathVariable String dbName) {
        ConfigRoot.DatabaseConfig dbConfig = findDatabase(dbName);
        if (dbConfig == null) {
            return List.of();
        }
        return connectionsMonitorService.getActiveConnections(dbConfig);
    }
    @GetMapping("/slow-queries/{dbName}")
    public List<QueryStats> getSlowQueries(@PathVariable String dbName) {
        ConfigRoot.DatabaseConfig dbConfig = findDatabase(dbName);
        if (dbConfig == null) {
            return List.of();
        }
        return queryAnalysisService.getSlowestQueries(dbConfig);
    }
    @GetMapping("/locks/{dbName}")
    public List<LockInfo> getLocks(@PathVariable String dbName) {
        ConfigRoot.DatabaseConfig dbConfig = findDatabase(dbName);
        if (dbConfig == null) {
            return List.of();
        }
        return lockMonitorService.getBlockingLocks(dbConfig);
    }
    @GetMapping("/unused-indexes/{dbName}")
    public List<IndexInfo> getUnusedIndexes(@PathVariable String dbName) {
        ConfigRoot.DatabaseConfig dbConfig = findDatabase(dbName);
        if (dbConfig == null) {
            return List.of();
        }
        return indexAnalysisService.getUnusedIndexes(dbConfig);
    }
    @GetMapping("/vacuum/{dbName}")
    public List<VacuumInfo> getVacuumStats(@PathVariable String dbName) {
        ConfigRoot.DatabaseConfig dbConfig = findDatabase(dbName);
        if (dbConfig == null) {
            return List.of();
        }
        return vacuumMonitorService.getVacuumStats(dbConfig);
    }
    private ConfigRoot.DatabaseConfig findDatabase(String name) {
        return monitoringConfig.getDatabaseConfigs().stream()
            .filter(db -> db.getName().equals(name) && db.isEnabled())
            .findFirst()
            .orElse(null);
    }
}
