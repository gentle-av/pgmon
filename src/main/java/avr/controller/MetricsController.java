package avr.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import avr.dto.ActiveSessionsRequest;
import avr.model.ConnectionInfo;
import avr.model.IndexInfo;
import avr.model.LockInfo;
import avr.model.MonitoredServer;
import avr.model.PollingConfiguration;
import avr.model.QueryStats;
import avr.model.VacuumInfo;
import avr.repository.MonitoredServerRepository;
import avr.repository.PollingConfigurationRepository;
import avr.service.ConnectionsMonitorService;
import avr.service.DatabaseMonitorService;
import avr.service.IndexAnalysisService;
import avr.service.LockMonitorService;
import avr.service.QueryAnalysisService;
import avr.service.VacuumMonitorService;

@RestController @RequestMapping("/api/metrics")
public class MetricsController {
  private final MonitoredServerRepository serverRepository;
  private final PollingConfigurationRepository pollingConfigRepository;
  private final DatabaseMonitorService databaseMonitorService;
  private final QueryAnalysisService queryAnalysisService;
  private final LockMonitorService lockMonitorService;
  private final IndexAnalysisService indexAnalysisService;
  private final ConnectionsMonitorService connectionsMonitorService;
  private final VacuumMonitorService vacuumMonitorService;

  public MetricsController(MonitoredServerRepository serverRepository,
      PollingConfigurationRepository pollingConfigRepository, DatabaseMonitorService databaseMonitorService,
      QueryAnalysisService queryAnalysisService, LockMonitorService lockMonitorService,
      IndexAnalysisService indexAnalysisService, ConnectionsMonitorService connectionsMonitorService,
      VacuumMonitorService vacuumMonitorService) {
    this.serverRepository = serverRepository;
    this.pollingConfigRepository = pollingConfigRepository;
    this.databaseMonitorService = databaseMonitorService;
    this.queryAnalysisService = queryAnalysisService;
    this.lockMonitorService = lockMonitorService;
    this.indexAnalysisService = indexAnalysisService;
    this.connectionsMonitorService = connectionsMonitorService;
    this.vacuumMonitorService = vacuumMonitorService;
  }

  @GetMapping("/all/{serverId}")
  public Map<String, Object> getAllMetrics(@PathVariable String serverId) {
    MonitoredServer server = serverRepository.findById(serverId).orElse(null);
    if (server == null) {
      return Map.of("error", "Server not found: " + serverId);
    }
    PollingConfiguration pollingConfig = pollingConfigRepository.findByServerId(serverId).orElse(null);
    Map<String, Object> metrics = new HashMap<>();
    try {
      metrics.put("database_stats", databaseMonitorService.collectStats(server));
    } catch (Exception e) {
      metrics.put("database_stats_error", e.getMessage());
    }
    try {
      if (pollingConfig != null && pollingConfig.isCollectQueries()) {
        metrics.put("slow_queries", queryAnalysisService.getSlowestQueries(server, pollingConfig));
        metrics.put("frequent_queries", queryAnalysisService.getMostFrequentQueries(server));
      } else {
        metrics.put("slow_queries", List.of());
        metrics.put("frequent_queries", List.of());
      }
    } catch (Exception e) {
      metrics.put("slow_queries_error", e.getMessage());
    }
    try {
      if (pollingConfig != null && pollingConfig.isCollectLocks()) {
        metrics.put("blocking_locks", lockMonitorService.getBlockingLocks(server));
      } else {
        metrics.put("blocking_locks", List.of());
      }
    } catch (Exception e) {
      metrics.put("blocking_locks_error", e.getMessage());
    }
    try {
      if (pollingConfig != null && pollingConfig.isCollectUnusedIndexes()) {
        metrics.put("unused_indexes", indexAnalysisService.getUnusedIndexes(server));
      } else {
        metrics.put("unused_indexes", List.of());
      }
    } catch (Exception e) {
      metrics.put("unused_indexes_error", e.getMessage());
    }
    try {
      if (pollingConfig != null && pollingConfig.isCollectConnections()) {
        metrics.put("active_connections", connectionsMonitorService.getActiveConnections(server));
        metrics.put("running_queries_count", connectionsMonitorService.getRunningQueriesCount(server));
        metrics.put("waiting_queries_count", connectionsMonitorService.getWaitingQueriesCount(server));
      } else {
        metrics.put("active_connections", List.of());
        metrics.put("running_queries_count", 0);
        metrics.put("waiting_queries_count", 0);
      }
    } catch (Exception e) {
      metrics.put("active_connections_error", e.getMessage());
    }
    try {
      if (pollingConfig != null && pollingConfig.isCollectVacuumStats()) {
        metrics.put("vacuum_stats", vacuumMonitorService.getVacuumStats(server));
      } else {
        metrics.put("vacuum_stats", List.of());
      }
    } catch (Exception e) {
      metrics.put("vacuum_stats_error", e.getMessage());
    }
    return metrics;
  }

  @GetMapping("/connections/{serverId}")
  public List<ConnectionInfo> getConnections(@PathVariable String serverId) {
    MonitoredServer server = serverRepository.findById(serverId).orElse(null);
    if (server == null) {
      return List.of();
    }
    return connectionsMonitorService.getActiveConnections(server);
  }

  @GetMapping("/slow-queries/{serverId}")
  public List<QueryStats> getSlowQueries(@PathVariable String serverId) {
    MonitoredServer server = serverRepository.findById(serverId).orElse(null);
    if (server == null) {
      return List.of();
    }
    PollingConfiguration pollingConfig = pollingConfigRepository.findByServerId(serverId).orElse(null);
    return queryAnalysisService.getSlowestQueries(server, pollingConfig);
  }

  @GetMapping("/locks/{serverId}")
  public List<LockInfo> getLocks(@PathVariable String serverId) {
    MonitoredServer server = serverRepository.findById(serverId).orElse(null);
    if (server == null) {
      return List.of();
    }
    return lockMonitorService.getBlockingLocks(server);
  }

  @GetMapping("/unused-indexes/{serverId}")
  public List<IndexInfo> getUnusedIndexes(@PathVariable String serverId) {
    MonitoredServer server = serverRepository.findById(serverId).orElse(null);
    if (server == null) {
      return List.of();
    }
    return indexAnalysisService.getUnusedIndexes(server);
  }

  @GetMapping("/vacuum/{serverId}")
  public List<VacuumInfo> getVacuumStats(@PathVariable String serverId) {
    MonitoredServer server = serverRepository.findById(serverId).orElse(null);
    if (server == null) {
      return List.of();
    }
    return vacuumMonitorService.getVacuumStats(server);
  }

  @PostMapping("/active-sessions")
  public Map<String, Object> getActiveSessions(@RequestBody ActiveSessionsRequest request) {
    MonitoredServer server = serverRepository.findById(request.getServerId()).orElse(null);
    if (server == null) {
      return Map.of("error", "Server not found: " + request.getServerId());
    }
    LocalDateTime fromTime = request.getFrom() != null ? request.getFrom() : LocalDateTime.now().minusMinutes(5);
    LocalDateTime toTime = request.getTo() != null ? request.getTo() : LocalDateTime.now();
    Map<String, Object> response = new HashMap<>();
    response.put("from", fromTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    response.put("to", toTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    response.put("server_id", server.getId());
    response.put("server_name", server.getServerName());
    response.put("active_sessions", connectionsMonitorService.getActiveSessionsInTimeRange(server, fromTime, toTime));
    return response;
  }

  @GetMapping("/active-sessions/all")
  public List<Map<String, Object>> getAllActiveSessions() {
    List<MonitoredServer> servers = serverRepository.findByEnabledTrue();
    List<Map<String, Object>> result = new ArrayList<>();
    for (MonitoredServer server : servers) {
      Map<String, Object> serverStats = new HashMap<>();
      serverStats.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
      serverStats.put("server_id", server.getId());
      serverStats.put("server_name", server.getServerName());
      serverStats.put("active_sessions_count", connectionsMonitorService.getRunningQueriesCount(server));
      serverStats.put("waiting_sessions_count", connectionsMonitorService.getWaitingQueriesCount(server));
      result.add(serverStats);
    }
    return result;
  }
}
