package avr.service;

import avr.config.DatabaseConfig;
import avr.config.MonitoringConfig;
import avr.repository.AshHistoryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AshCollectorService {

    private final MonitoringConfig monitoringConfig;
    private final AshHistoryRepository ashRepository;
    private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();

    public AshCollectorService(MonitoringConfig monitoringConfig,
                                AshHistoryRepository ashRepository) {
        this.monitoringConfig = monitoringConfig;
        this.ashRepository = ashRepository;
    }

    @Scheduled(fixedDelay = 2000) // Сбор каждые 2 секунды
    public void collectAshSnapshots() {
        for (DatabaseConfig dbConfig : monitoringConfig.getDatabases()) {
            if (!dbConfig.enabled()) continue;

            try {
                JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
                List<Map<String, Object>> sessions = collectActiveSessions(jdbcTemplate);
                ashRepository.insertSnapshot(jdbcTemplate, dbConfig.name(), sessions);
            } catch (Exception e) {
                System.err.println("ASH collection failed for " + dbConfig.name() + ": " + e.getMessage());
            }
        }
    }

    private List<Map<String, Object>> collectActiveSessions(JdbcTemplate jdbcTemplate) {
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
        """;

        return jdbcTemplate.queryForList(sql);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanOldData() {
        for (DatabaseConfig dbConfig : monitoringConfig.getDatabases()) {
            if (!dbConfig.enabled()) continue;
            JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
            jdbcTemplate.execute("SELECT clean_old_ash_data()");
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
