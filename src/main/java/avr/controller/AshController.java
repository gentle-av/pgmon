package avr.controller;

import avr.config.DatabaseConfig;
import avr.config.MonitoringConfig;
import avr.repository.AshHistoryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/ash")
public class AshController {

    private final MonitoringConfig monitoringConfig;
    private final AshHistoryRepository ashRepository;
    private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();

    public AshController(MonitoringConfig monitoringConfig,
                         AshHistoryRepository ashRepository) {
        this.monitoringConfig = monitoringConfig;
        this.ashRepository = ashRepository;
    }

    @GetMapping("/data/{databaseName}")
    public Map<String, Object> getAshData(@PathVariable String databaseName,
                                           @RequestParam(defaultValue = "30") int minutes) {
        DatabaseConfig dbConfig = findDatabase(databaseName);
        if (dbConfig == null) {
            return Map.of("error", "Database not found");
        }
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
        List<Map<String, Object>> rawData = ashRepository.getAshData(jdbcTemplate, databaseName, minutes);
        Map<String, Object> chartData = new HashMap<>();
        List<String> times = new ArrayList<>();
        Map<String, List<Number>> seriesMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rawData) {
            String time = row.get("time_bucket").toString();
            String category = (String) row.get("category");
            Number count = (Number) row.get("session_count");
            if (!times.contains(time)) times.add(time);
            seriesMap.computeIfAbsent(category, k -> new ArrayList<>())
                     .add(count);
        }
        List<Map<String, Object>> series = new ArrayList<>();
        for (Map.Entry<String, List<Number>> entry : seriesMap.entrySet()) {
            List<Number> values = entry.getValue();
            while (values.size() < times.size()) values.add(0);
            series.add(Map.of(
                "name", entry.getKey(),
                "type", "line",
                "stack", "total",
                "data", values,
                "areaStyle", Map.of("opacity", 0.5)
            ));
        }
        chartData.put("times", times);
        chartData.put("series", series);
        return chartData;
    }

    private DatabaseConfig findDatabase(String name) {
        return monitoringConfig.getDatabases().stream()
            .filter(db -> db.name().equals(name) && db.enabled())
            .findFirst()
            .orElse(null);
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
