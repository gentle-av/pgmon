package avr.service;

import avr.config.DatabaseConfig;
import avr.config.MonitoringConfig;
import avr.repository.AshHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AshCollectorService {

    private static final Logger log = LoggerFactory.getLogger(AshCollectorService.class);

    private final MonitoringConfig monitoringConfig;
    private final AshHistoryRepository ashRepository;
    private final DatabaseInitializationService dbInitService;
    private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();

    public AshCollectorService(MonitoringConfig monitoringConfig,
                                AshHistoryRepository ashRepository,
                                DatabaseInitializationService dbInitService) {
        this.monitoringConfig = monitoringConfig;
        this.ashRepository = ashRepository;
        this.dbInitService = dbInitService;
    }

    @PostConstruct
    public void init() {
        log.info("AshCollectorService инициализирован");
        // Инициализация уже происходит в DatabaseInitializationService
    }

    @Scheduled(fixedDelay = 2000)
    public void collectAshSnapshots() {
        for (DatabaseConfig dbConfig : monitoringConfig.getDatabases()) {
            if (!dbConfig.enabled()) continue;

            // Проверяем, инициализирована ли БД
            if (!dbInitService.isInitialized(dbConfig.name())) {
                log.debug("БД {} не инициализирована, пропускаем сбор", dbConfig.name());
                continue;
            }

            try {
                JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
                List<Map<String, Object>> sessions = collectActiveSessions(jdbcTemplate, dbConfig);

                if (!sessions.isEmpty()) {
                    ashRepository.insertSnapshot(jdbcTemplate, dbConfig.name(), sessions);
                    log.debug("Собрано {} сессий для БД {}", sessions.size(), dbConfig.name());
                }
            } catch (Exception e) {
                log.error("ASH collection failed for {}: {}", dbConfig.name(), e.getMessage());
            }
        }
    }

    private List<Map<String, Object>> collectActiveSessions(JdbcTemplate jdbcTemplate, DatabaseConfig dbConfig) {
        String sql = """
            SELECT
                pid,
                state,
                wait_event_type,
                wait_event,
                MD5(query) as query_hash,
                LEFT(query, 500) as query,
                EXTRACT(EPOCH FROM (NOW() - query_start)) as duration_seconds
            FROM pg_stat_activity
            WHERE state = 'active'
              AND pid != pg_backend_pid()
              AND query NOT LIKE '%pg_stat_activity%'
              AND query NOT LIKE '%pash_ash_history%'
        """;

        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("Ошибка сбора сессий для {}: {}", dbConfig.name(), e.getMessage());
            return List.of();
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanOldData() {
        for (DatabaseConfig dbConfig : monitoringConfig.getDatabases()) {
            if (!dbConfig.enabled() || !dbInitService.isInitialized(dbConfig.name())) continue;

            try {
                JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
                Integer deleted = jdbcTemplate.queryForObject(
                    "SELECT clean_old_ash_data(7)",
                    Integer.class
                );
                if (deleted != null && deleted > 0) {
                    log.info("Очищено {} записей из ASH для БД {}", deleted, dbConfig.name());
                }
            } catch (Exception e) {
                log.error("Ошибка очистки ASH для {}: {}", dbConfig.name(), e.getMessage());
            }
        }
    }

    private JdbcTemplate getJdbcTemplate(DatabaseConfig dbConfig) {
        return jdbcTemplates.computeIfAbsent(dbConfig.name(), key -> {
            var builder = org.springframework.boot.jdbc.DataSourceBuilder.create();
            builder.url(dbConfig.getJdbcUrl());
            builder.username(dbConfig.username());
            builder.password(dbConfig.password());
            builder.driverClassName("org.postgresql.Driver");
            return new JdbcTemplate(builder.build());
        });
    }
}
