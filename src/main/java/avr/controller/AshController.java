package avr.controller;

import avr.model.MonitoredServer;
import avr.repository.AshHistoryRepository;
import avr.repository.MonitoredServerRepository;
import avr.service.ConnectionPoolManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/ash")
public class AshController {
    private final MonitoredServerRepository serverRepository;
    private final AshHistoryRepository ashRepository;
    private final ConnectionPoolManager connectionPoolManager;
    private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();

    public AshController(MonitoredServerRepository serverRepository,
                         AshHistoryRepository ashRepository,
                         ConnectionPoolManager connectionPoolManager) {
        this.serverRepository = serverRepository;
        this.ashRepository = ashRepository;
        this.connectionPoolManager = connectionPoolManager;
    }

    @GetMapping("/data/{serverId}")
    public Map<String, Object> getAshData(@PathVariable String serverId, @RequestParam(defaultValue = "30") int minutes) {
        MonitoredServer server = serverRepository.findById(serverId).orElse(null);
        if (server == null) {
            return Map.of("error", "Server not found");
        }
        JdbcTemplate jdbcTemplate = getJdbcTemplate(server);
        List<Map<String, Object>> rawData = ashRepository.getAshData(jdbcTemplate, server.getServerName(), minutes);
        Map<String, Object> chartData = new HashMap<>();
        List<String> times = new ArrayList<>();
        Map<String, List<Number>> seriesMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rawData) {
            String time = row.get("time_bucket").toString();
            String category = (String) row.get("category");
            Number count = (Number) row.get("session_count");
            if (!times.contains(time)) times.add(time);
            seriesMap.computeIfAbsent(category, k -> new ArrayList<>()).add(count);
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

    private JdbcTemplate getJdbcTemplate(MonitoredServer server) {
        return jdbcTemplates.computeIfAbsent(server.getId(), key -> {
            DataSource dataSource = connectionPoolManager.getDataSource(server.getId());
            return new JdbcTemplate(dataSource);
        });
    }
}
