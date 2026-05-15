package avr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import avr.config.DatabaseConfig;
import avr.config.MonitoringConfig;
import avr.model.DatabaseStats;

@Service
public class MultiDatabaseMonitorService {

  private static final Logger log = LoggerFactory.getLogger(MultiDatabaseMonitorService.class);

  private final MonitoringConfig monitoringConfig;
  private final DatabaseMonitorService databaseMonitorService;
  private final QueryAnalysisService queryAnalysisService;
  private final LockMonitorService lockMonitorService;
  private final IndexAnalysisService indexAnalysisService;
  private final ConnectionsMonitorService connectionsMonitorService;
  private final VacuumMonitorService vacuumMonitorService;

  public MultiDatabaseMonitorService(MonitoringConfig monitoringConfig,
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

  @Scheduled(fixedDelay = 30000)
  public void monitorAllDatabases() {
    log.info("=== Запуск полного мониторинга всех БД ===");

    for (DatabaseConfig dbConfig : monitoringConfig.getDatabases()) {
      if (!dbConfig.enabled()) {
        log.debug("База {} отключена", dbConfig.name());
        continue;
      }

      log.info("--- Мониторинг БД: {} ---", dbConfig.name());

      try {
        // 1. Общая статистика БД
        DatabaseStats stats = databaseMonitorService.collectStats(dbConfig);
        log.info("📊 [{}] Размер: {}, Активных коннектов: {}, Cache hit: {}%",
            dbConfig.name(), stats.sizeHuman(), stats.activeConnections(), stats.cacheHitRatio());

        // 2. Активные соединения
        var connections = connectionsMonitorService.getActiveConnections(dbConfig);
        log.info("🔌 [{}] Активных сессий: {}, Ожидающих: {}",
            dbConfig.name(), connections.size(),
            connectionsMonitorService.getWaitingQueriesCount(dbConfig));

        // 3. Блокировки
        var locks = lockMonitorService.getBlockingLocks(dbConfig);
        if (!locks.isEmpty()) {
          log.warn("🔒 [{}] Обнаружено {} блокировок!", dbConfig.name(), locks.size());
        }

        // 4. Медленные запросы
        var slowQueries = queryAnalysisService.getSlowestQueries(dbConfig);
        if (!slowQueries.isEmpty()) {
          log.warn("🐌 [{}] Медленных запросов: {}", dbConfig.name(), slowQueries.size());
          slowQueries.forEach(q -> log.debug("  - {} ms: {}", q.meanTimeMs(), q.query()));
        }

        // 5. Неиспользуемые индексы
        var unusedIndexes = indexAnalysisService.getUnusedIndexes(dbConfig);
        if (!unusedIndexes.isEmpty()) {
          log.warn("📇 [{}] Неиспользуемых индексов: {}", dbConfig.name(), unusedIndexes.size());
        }

        // 6. Статистика VACUUM
        var vacuumStats = vacuumMonitorService.getVacuumStats(dbConfig);
        long highDeadTuples = vacuumStats.stream()
            .filter(v -> v.deadTupleRatio() > 10)
            .count();
        if (highDeadTuples > 0) {
          log.warn("🗑️ [{}] Таблиц с >10% dead tuples: {}", dbConfig.name(), highDeadTuples);
        }

        log.info("✅ Мониторинг БД {} завершён", dbConfig.name());

      } catch (Exception e) {
        log.error("❌ Ошибка мониторинга базы {}: {}", dbConfig.name(), e.getMessage());
      }
    }

    log.info("=== Мониторинг завершён ===");
  }
}
