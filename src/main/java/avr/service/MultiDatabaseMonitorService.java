package avr.service;

import avr.config.ConfigRoot;
import avr.config.MonitoringConfig;
import avr.model.DatabaseStats;
import avr.service.ConnectionsMonitorService;
import avr.service.DatabaseMonitorService;
import avr.service.IndexAnalysisService;
import avr.service.LockMonitorService;
import avr.service.QueryAnalysisService;
import avr.service.VacuumMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
        for (ConfigRoot.DatabaseConfig dbConfig : monitoringConfig.getDatabaseConfigs()) {
            if (!dbConfig.isEnabled()) {
                log.debug("База {} отключена", dbConfig.getName());
                continue;
            }
            log.info("--- Мониторинг БД: {} ---", dbConfig.getName());
            try {
                DatabaseStats stats = databaseMonitorService.collectStats(dbConfig);
                log.info("📊 [{}] Размер: {}, Активных коннектов: {}, Cache hit: {}%",
                    dbConfig.getName(), stats.sizeHuman(), stats.activeConnections(), stats.cacheHitRatio());
                var connections = connectionsMonitorService.getActiveConnections(dbConfig);
                log.info("🔌 [{}] Активных сессий: {}, Ожидающих: {}",
                    dbConfig.getName(), connections.size(),
                    connectionsMonitorService.getWaitingQueriesCount(dbConfig));
                var locks = lockMonitorService.getBlockingLocks(dbConfig);
                if (!locks.isEmpty()) {
                    log.warn("🔒 [{}] Обнаружено {} блокировок!", dbConfig.getName(), locks.size());
                }
                var slowQueries = queryAnalysisService.getSlowestQueries(dbConfig);
                if (!slowQueries.isEmpty()) {
                    log.warn("🐌 [{}] Медленных запросов: {}", dbConfig.getName(), slowQueries.size());
                }
                var unusedIndexes = indexAnalysisService.getUnusedIndexes(dbConfig);
                if (!unusedIndexes.isEmpty()) {
                    log.warn("📇 [{}] Неиспользуемых индексов: {}", dbConfig.getName(), unusedIndexes.size());
                }
                var vacuumStats = vacuumMonitorService.getVacuumStats(dbConfig);
                long highDeadTuples = vacuumStats.stream()
                    .filter(v -> v.deadTupleRatio() > 10)
                    .count();
                if (highDeadTuples > 0) {
                    log.warn("🗑️ [{}] Таблиц с >10% dead tuples: {}", dbConfig.getName(), highDeadTuples);
                }
                log.info("✅ Мониторинг БД {} завершён", dbConfig.getName());
            } catch (Exception e) {
                log.error("❌ Ошибка мониторинга базы {}: {}", dbConfig.getName(), e.getMessage());
            }
        }
        log.info("=== Мониторинг завершён ===");
    }
    @CacheEvict(value = {
        "database_stats",
        "query_stats",
        "connection_info",
        "table_metrics",
        "index_metrics",
        "lock_info",
        "vacuum_info"
    }, allEntries = true)
    public void evictAllCaches() {
        log.info("All monitoring caches evicted");
    }
}
