package avr.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import avr.config.MonitoringConfig;

@RestController
@RequestMapping("/")
public class WebController {

  private final MonitoringConfig monitoringConfig;

  public WebController(MonitoringConfig monitoringConfig) {
    this.monitoringConfig = monitoringConfig;
  }

  @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> index() throws IOException {
    Resource resource = new ClassPathResource("static/index.html");
    String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    content = content.replace("__TITLE__", monitoringConfig.getWeb().title());
    content = content.replace("__REFRESH_INTERVAL__",
        String.valueOf(monitoringConfig.getWeb().refreshIntervalSeconds()));
    content = content.replace("__THEME__", monitoringConfig.getWeb().theme());
    return ResponseEntity.ok(content);
  }

  @GetMapping(value = "/dashboard", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> dashboard() throws IOException {
    return index();
  }
}
