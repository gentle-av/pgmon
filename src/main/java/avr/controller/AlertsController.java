package avr.controller;

import avr.model.MonitoredServer;
import avr.model.PollingConfiguration;
import avr.model.QueryStats;
import avr.repository.MonitoredServerRepository;
import avr.repository.PollingConfigurationRepository;
import avr.service.ConnectionsMonitorService;
import avr.service.LockMonitorService;
import avr.service.QueryAnalysisService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/alerts")
public class AlertsController {
    private final MonitoredServerRepository serverRepository;
    private final PollingConfigurationRepository pollingConfigRepository;
    private final LockMonitorService lockMonitorService;
    private final QueryAnalysisService queryAnalysisService;
    private final ConnectionsMonitorService connectionsMonitorService;

    public AlertsController(MonitoredServerRepository serverRepository,
                            PollingConfigurationRepository pollingConfigRepository,
                            LockMonitorService lockMonitorService,
                            QueryAnalysisService queryAnalysisService,
                            ConnectionsMonitorService connectionsMonitorService) {
        this.serverRepository = serverRepository;
        this.pollingConfigRepository = pollingConfigRepository;
        this.lockMonitorService = lockMonitorService;
        this.queryAnalysisService = queryAnalysisService;
        this.connectionsMonitorService = connectionsMonitorService;
    }

    @GetMapping
    public List<Map<String, Object>> getActiveAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        List<MonitoredServer> servers = serverRepository.findByEnabledTrue();
        for (MonitoredServer server : servers) {
            PollingConfiguration pollingConfig = pollingConfigRepository.findByServerId(server.getId()).orElse(null);
            if (lockMonitorService.hasBlockingLocks(server)) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "BLOCKING_LOCKS");
                alert.put("database", server.getServerName());
                alert.put("severity", "HIGH");
                alert.put("message", "Обнаружены блокирующие locks");
                alert.put("details", lockMonitorService.getBlockingLocks(server));
                alerts.add(alert);
            }
            if (pollingConfig != null && pollingConfig.isCollectQueries()) {
                List<QueryStats> slowQueries = queryAnalysisService.getSlowestQueries(server, pollingConfig);
                if (!slowQueries.isEmpty()) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "SLOW_QUERIES");
                    alert.put("database", server.getServerName());
                    alert.put("severity", "MEDIUM");
                    alert.put("message", "Обнаружены медленные запросы");
                    alert.put("details", slowQueries);
                    alerts.add(alert);
                }
            }
            int waitingQueries = connectionsMonitorService.getWaitingQueriesCount(server);
            if (waitingQueries > 10) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "WAITING_QUERIES");
                alert.put("database", server.getServerName());
                alert.put("severity", "HIGH");
                alert.put("message", "Много ожидающих запросов: " + waitingQueries);
                alerts.add(alert);
            }
        }
        return alerts;
    }
}
