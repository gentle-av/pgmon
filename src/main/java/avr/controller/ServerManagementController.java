package avr.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import avr.dto.RegisterServerRequest;
import avr.dto.UpdatePollingRequest;
import avr.dto.UpdateServerRequest;
import avr.model.MonitoredServer;
import avr.model.PollingConfiguration;
import avr.service.ServerManagementService;

@RestController @RequestMapping("/api/servers")
public class ServerManagementController {
  private final ServerManagementService serverManagementService;

  public ServerManagementController(ServerManagementService serverManagementService) {
    this.serverManagementService = serverManagementService;
  }

  @PostMapping("/register")
  public ResponseEntity<Map<String, Object>> registerServer(@RequestBody RegisterServerRequest request) {
    try {
      MonitoredServer server = serverManagementService.registerServer(request);
      PollingConfiguration config = serverManagementService.getPollingConfig(server.getId()).orElse(null);
      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "Server registered successfully");
      response.put("serverId", server.getId());
      response.put("server", server);
      response.put("pollingConfig", config);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @GetMapping
  public List<MonitoredServer> getAllServers() {
    return serverManagementService.getAllServers();
  }

  @GetMapping("/enabled")
  public List<MonitoredServer> getEnabledServers() {
    return serverManagementService.getEnabledServers();
  }

  @GetMapping("/{id}")
  public ResponseEntity<MonitoredServer> getServer(@PathVariable String id) {
    return serverManagementService.getServer(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/by-name/{name}")
  public ResponseEntity<MonitoredServer> getServerByName(@PathVariable String name) {
    return serverManagementService.getServerByName(name).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<MonitoredServer> updateServer(@PathVariable String id,
      @RequestBody UpdateServerRequest request) {
    try {
      return ResponseEntity.ok(serverManagementService.updateServer(id, request));
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteServer(@PathVariable String id) {
    try {
      serverManagementService.deleteServer(id);
      return ResponseEntity.noContent().build();
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/{id}/enable")
  public ResponseEntity<MonitoredServer> enableServer(@PathVariable String id) {
    try {
      return ResponseEntity.ok(serverManagementService.enableServer(id));
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/{id}/disable")
  public ResponseEntity<MonitoredServer> disableServer(@PathVariable String id) {
    try {
      return ResponseEntity.ok(serverManagementService.disableServer(id));
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PutMapping("/{id}/polling/ms")
  public ResponseEntity<PollingConfiguration> updatePollingConfigMs(@PathVariable String id,
      @RequestBody UpdatePollingRequest request) {
    try {
      return ResponseEntity.ok(serverManagementService.updatePollingConfig(id, request));
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/{id}/refresh-pool")
  public ResponseEntity<Map<String, String>> refreshConnectionPool(@PathVariable String id) {
    serverManagementService.refreshConnectionPool(id);
    Map<String, String> response = new HashMap<>();
    response.put("status", "refreshed");
    response.put("serverId", id);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}/polling")
  public ResponseEntity<PollingConfiguration> getPollingConfig(@PathVariable String id) {
    return serverManagementService.getPollingConfig(id).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}/polling")
  public ResponseEntity<PollingConfiguration> updatePollingConfig(@PathVariable String id,
      @RequestBody UpdatePollingRequest request) {
    try {
      return ResponseEntity.ok(serverManagementService.updatePollingConfig(id, request));
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
