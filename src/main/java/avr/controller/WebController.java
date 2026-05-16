package avr.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import avr.config.DatabaseConfig;
import avr.config.MonitoringConfig;
import avr.config.WebConfig;

@RestController
public class WebController {

    private final MonitoringConfig monitoringConfig;
    private final ObjectMapper objectMapper;

    public WebController(MonitoringConfig monitoringConfig) {
        this.monitoringConfig = monitoringConfig;
        this.objectMapper = new ObjectMapper();

        // Отладка - выводим количество БД при создании контроллера
        System.out.println("=== WebController created ===");
        System.out.println("MonitoringConfig is " + (monitoringConfig != null ? "NOT NULL" : "NULL"));
        if (monitoringConfig != null) {
            List<DatabaseConfig> dbs = monitoringConfig.getDatabases();
            System.out.println("Databases in controller: " + (dbs != null ? dbs.size() : "NULL"));
            if (dbs != null) {
                dbs.forEach(db -> System.out.println("  DB in controller: " + db.name() + " enabled=" + db.enabled()));
            }
        }
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> index() throws IOException {
        System.out.println("=== Handling / request ===");

        List<DatabaseConfig> databases = monitoringConfig.getDatabases();
        System.out.println("Databases in request handler: " + (databases != null ? databases.size() : "NULL"));

        if (databases == null || databases.isEmpty()) {
            System.out.println("ERROR: No databases in request handler!");
            return ResponseEntity.ok("No databases configured");
        }

        Resource resource = new ClassPathResource("static/index.html");
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        WebConfig webConfig = monitoringConfig.getWeb();

        List<DatabaseConfig> enabledDatabases = databases.stream()
                .filter(DatabaseConfig::enabled)
                .collect(Collectors.toList());

        System.out.println("Sending " + enabledDatabases.size() + " enabled databases to frontend");

        String title = webConfig != null ? webConfig.title() : "PostgreSQL Monitor";
        int refreshInterval = webConfig != null ? webConfig.refreshIntervalSeconds() : 10;
        String theme = webConfig != null ? webConfig.theme() : "dark";

        content = content.replace("__TITLE__", title);
        content = content.replace("__REFRESH_INTERVAL__", String.valueOf(refreshInterval));
        content = content.replace("__THEME__", theme);

        String databasesJson = objectMapper.writeValueAsString(enabledDatabases);
        System.out.println("JSON: " + databasesJson);

        String initScript = """
            <script>
                window.__INITIAL_DATABASES__ = %s;
                console.log('Frontend received databases:', window.__INITIAL_DATABASES__);
                console.log('Databases count:', window.__INITIAL_DATABASES__.length);
            </script>
            """.formatted(databasesJson);

        content = content.replace("</body>", initScript + "\n</body>");

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(content);
    }

    @GetMapping(value = "/dashboard", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> dashboard() throws IOException {
        return index();
    }

    @GetMapping("/api/debug/config")
    public ResponseEntity<?> debugConfig() {
        List<DatabaseConfig> databases = monitoringConfig.getDatabases();
        return ResponseEntity.ok(java.util.Map.of(
            "databasesCount", databases != null ? databases.size() : 0,
            "enabledCount", databases != null ? databases.stream().filter(DatabaseConfig::enabled).count() : 0,
            "databases", databases,
            "web", monitoringConfig.getWeb()
        ));
    }
}
