package avr.controller;

import avr.config.ConfigRoot;
import avr.config.MonitoringConfig;
import avr.model.QueryStats;
import avr.service.ConnectionsMonitorService;
import avr.service.LockMonitorService;
import avr.service.QueryAnalysisService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/alerts")
public class AlertsController {
    private final MonitoringConfig monitoringConfig;
    private final LockMonitorService lockMonitorService;
    private final QueryAnalysisService queryAnalysisService;
    private final ConnectionsMonitorService connectionsMonitorService;
    public AlertsController(MonitoringConfig monitoringConfig,
                            LockMonitorService lockMonitorService,
                            QueryAnalysisService queryAnalysisService,
                            ConnectionsMonitorService connectionsMonitorService) {
        this.monitoringConfig = monitoringConfig;
        this.lockMonitorService = lockMonitorService;
        this.queryAnalysisService = queryAnalysisService;
        this.connectionsMonitorService = connectionsMonitorService;
    }
    @GetMapping
    public List<Map<String, Object>> getActiveAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        for (ConfigRoot.DatabaseConfig dbConfig : monitoringConfig.getDatabaseConfigs()) {
            if (!dbConfig.isEnabled()) continue;
            if (lockMonitorService.hasBlockingLocks(dbConfig)) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "BLOCKING_LOCKS");
                alert.put("database", dbConfig.getName());
                alert.put("severity", "HIGH");
                alert.put("message", "Обнаружены блокирующие locks");
                alert.put("details", lockMonitorService.getBlockingLocks(dbConfig));
                alerts.add(alert);
            }
            List<QueryStats> slowQueries = queryAnalysisService.getSlowestQueries(dbConfig);
            if (!slowQueries.isEmpty()) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "SLOW_QUERIES");
                alert.put("database", dbConfig.getName());
                alert.put("severity", "MEDIUM");
                alert.put("message", "Обнаружены медленные запросы");
                alert.put("details", slowQueries);
                alerts.add(alert);
            }
            int waitingQueries = connectionsMonitorService.getWaitingQueriesCount(dbConfig);
            if (waitingQueries > 10) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "WAITING_QUERIES");
                alert.put("database", dbConfig.getName());
                alert.put("severity", "HIGH");
                alert.put("message", "Много ожидающих запросов: " + waitingQueries);
                alerts.add(alert);
            }
        }
        return alerts;
    }
}
