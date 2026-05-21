package avr.controller;

import avr.model.MonitoredServer;
import avr.repository.AshHistoryRepository;
import avr.repository.MonitoredServerRepository;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/ash")
public class AshController {
  private final MonitoredServerRepository serverRepository;
  private final AshHistoryRepository ashHistoryRepository;

  public AshController(MonitoredServerRepository serverRepository,
      AshHistoryRepository ashHistoryRepository) {
    this.serverRepository = serverRepository;
    this.ashHistoryRepository = ashHistoryRepository;
  }

  @GetMapping("/data/{serverId}")
  public Map<String, Object> getAshData(@PathVariable String serverId,
      @RequestParam(defaultValue = "30") int minutes,
      @RequestParam(defaultValue = "true") boolean includeEmpty) {
    MonitoredServer server = serverRepository.findById(serverId).orElse(null);
    if (server == null) {
      return Map.of("error", "Server not found");
    }
    List<Map<String, Object>> rawData = ashHistoryRepository.getAshData(server.getServerName(), minutes, includeEmpty);
    Map<String, Object> chartData = new HashMap<>();
    List<String> times = new ArrayList<>();
    Map<String, List<Number>> seriesMap = new LinkedHashMap<>();
    for (Map<String, Object> row : rawData) {
      String time = row.get("time_bucket").toString();
      String category = (String) row.get("category");
      Number count = (Number) row.get("session_count");
      if (!times.contains(time))
        times.add(time);
      seriesMap.computeIfAbsent(category, k -> new ArrayList<>()).add(count);
    }
    List<Map<String, Object>> series = new ArrayList<>();
    for (Map.Entry<String, List<Number>> entry : seriesMap.entrySet()) {
      List<Number> values = entry.getValue();
      while (values.size() < times.size())
        values.add(0);
      series.add(Map.of(
          "name", entry.getKey(),
          "type", "line",
          "stack", "total",
          "data", values,
          "areaStyle", Map.of("opacity", 0.5)));
    }
    chartData.put("times", times);
    chartData.put("series", series);
    return chartData;
  }

  @PostMapping("/active-sessions")
  public Map<String, Object> getActiveSessions(@RequestBody Map<String, Object> request) {
    String serverId = (String) request.get("serverId");
    String time = (String) request.get("time");
    MonitoredServer server = serverRepository.findById(serverId).orElse(null);
    if (server == null) {
      return Map.of("error", "Server not found");
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime snapshotTime = LocalDateTime.parse(time, formatter);
    int activeSessions = ashHistoryRepository.getActiveSessionsCount(server.getServerName(), snapshotTime);
    Map<String, Object> response = new HashMap<>();
    response.put("serverId", serverId);
    response.put("time", time);
    response.put("activeSessions", activeSessions);
    return response;
  }
}
