package avr.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import avr.model.DatabaseStats;
import avr.model.MonitoredServer;
import avr.model.PollingConfiguration;
import avr.repository.MonitoredServerRepository;
import avr.repository.PollingConfigurationRepository;

@Service
public class MultiDatabaseMonitorService {
  private static final Logger log = LoggerFactory.getLogger(MultiDatabaseMonitorService.class);
  private final MonitoredServerRepository serverRepository;
  private final PollingConfigurationRepository pollingConfigRepository;
  private final DatabaseMonitorService databaseMonitorService;
  private final QueryAnalysisService queryAnalysisService;
  private final LockMonitorService lockMonitorService;
  private final IndexAnalysisService indexAnalysisService;
  private final ConnectionsMonitorService connectionsMonitorService;
  private final VacuumMonitorService vacuumMonitorService;
  private final PollingHistoryService pollingHistoryService;

  public MultiDatabaseMonitorService(MonitoredServerRepository serverRepository,
      PollingConfigurationRepository pollingConfigRepository, DatabaseMonitorService databaseMonitorService,
      QueryAnalysisService queryAnalysisService, LockMonitorService lockMonitorService,
      IndexAnalysisService indexAnalysisService, ConnectionsMonitorService connectionsMonitorService,
      VacuumMonitorService vacuumMonitorService, PollingHistoryService pollingHistoryService) {
    this.serverRepository = serverRepository;
    this.pollingConfigRepository = pollingConfigRepository;
    this.databaseMonitorService = databaseMonitorService;
    this.queryAnalysisService = queryAnalysisService;
    this.lockMonitorService = lockMonitorService;
    this.indexAnalysisService = indexAnalysisService;
    this.connectionsMonitorService = connectionsMonitorService;
    this.vacuumMonitorService = vacuumMonitorService;
    this.pollingHistoryService = pollingHistoryService;
  }

  public void pollDatabase(MonitoredServer server, PollingConfiguration config) {
    Long historyId = pollingHistoryService.recordPollingStart(server.getId(), config.getId());
    long startTime = System.currentTimeMillis();
    try {
      log.info("--- Мониторинг БД: {} (приоритет: {}) ---", server.getServerName(), config.getPriority());
      config.setLastPollingStart(LocalDateTime.now());
      pollingConfigRepository.save(config);
      if (config.isCollectCacheHitRatio()) {
        DatabaseStats stats = databaseMonitorService.collectStats(server);
        log.info("📊 [{}] Размер: {}, Активных коннектов: {}, Cache hit: {}%", server.getServerName(),
            stats.sizeHuman(), stats.activeConnections(), stats.cacheHitRatio());
      }
      if (config.isCollectConnections()) {
        var connections = connectionsMonitorService.getActiveConnections(server);
        log.info("🔌 [{}] Активных сессий: {}, Ожидающих: {}", server.getServerName(), connections.size(),
            connectionsMonitorService.getWaitingQueriesCount(server));
      }
      if (config.isCollectLocks()) {
        var locks = lockMonitorService.getBlockingLocks(server);
        if (!locks.isEmpty()) {
          log.warn("🔒 [{}] Обнаружено {} блокировок!", server.getServerName(), locks.size());
        }
      }
      if (config.isCollectQueries()) {
        var slowQueries = queryAnalysisService.getSlowestQueries(server, config);
        if (!slowQueries.isEmpty()) {
          log.warn("🐌 [{}] Медленных запросов: {}", server.getServerName(), slowQueries.size());
        }
      }
      if (config.isCollectUnusedIndexes()) {
        var unusedIndexes = indexAnalysisService.getUnusedIndexes(server);
        if (!unusedIndexes.isEmpty()) {
          log.warn("📇 [{}] Неиспользуемых индексов: {}", server.getServerName(), unusedIndexes.size());
        }
      }
      if (config.isCollectVacuumStats()) {
        var vacuumStats = vacuumMonitorService.getVacuumStats(server);
        double threshold = config.getDeadTupleRatioThresholdPercent() != null
            ? config.getDeadTupleRatioThresholdPercent()
            : 10.0;
        long highDeadTuples = vacuumStats.stream().filter(v -> v.deadTupleRatio() > threshold).count();
        if (highDeadTuples > 0) {
          log.warn("🗑️ [{}] Таблиц с >{}% dead tuples: {}", server.getServerName(), threshold, highDeadTuples);
        }
      }
      long duration = System.currentTimeMillis() - startTime;
      pollingHistoryService.recordPollingSuccess(historyId, duration);
      config.setLastPollingEnd(LocalDateTime.now());
      pollingConfigRepository.save(config);
      log.info("✅ Мониторинг БД {} завершён за {} мс", server.getServerName(), duration);
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      pollingHistoryService.recordPollingFailure(historyId, e.getMessage());
      log.error("❌ Ошибка мониторинга базы {}: {}", server.getServerName(), e.getMessage());
    }
  }

  @CacheEvict(value = { "database_stats", "query_stats", "connection_info", "table_metrics", "index_metrics",
      "lock_info", "vacuum_info" }, allEntries = true)
  public void evictAllCaches() {
    log.info("All monitoring caches evicted");
  }
}
