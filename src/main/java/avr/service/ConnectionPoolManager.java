package avr.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import avr.model.MonitoredServer;
import avr.model.StoredCredential;
import avr.repository.MonitoredServerRepository;

@Service
public class ConnectionPoolManager {
  private final Map<String, HikariDataSource> pools = new ConcurrentHashMap<>();
  private final MonitoredServerRepository serverRepository;
  private final CredentialService credentialService;

  public ConnectionPoolManager(MonitoredServerRepository serverRepository, CredentialService credentialService) {
    this.serverRepository = serverRepository;
    this.credentialService = credentialService;
  }

  public DataSource getDataSource(String serverId) {
    return pools.computeIfAbsent(serverId, this::createPool);
  }

  private HikariDataSource createPool(String serverId) {
    MonitoredServer server = serverRepository.findById(serverId)
        .orElseThrow(() -> new RuntimeException("Server not found: " + serverId));
    try {
      StoredCredential credential = credentialService.getCredential(server.getCredentialId())
          .orElseThrow(() -> new RuntimeException("Credential not found: " + server.getCredentialId()));
      String password = credentialService.getDecryptedPassword(credential.getId());
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s", credential.getHost(), credential.getPort(),
          credential.getDatabaseName()));
      config.setUsername(credential.getUsername());
      config.setPassword(password);
      config.setConnectionTimeout(server.getConnectionTimeoutMs());
      config.setValidationTimeout(5000);
      config.setMaximumPoolSize(10);
      config.setMinimumIdle(2);
      config.setIdleTimeout(300000);
      config.setMaxLifetime(600000);
      config.setConnectionTestQuery("SELECT 1");
      if (credential.getSslMode() != null && !"disable".equals(credential.getSslMode())) {
        config.addDataSourceProperty("ssl", "true");
        config.addDataSourceProperty("sslmode", credential.getSslMode());
      }
      return new HikariDataSource(config);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create connection pool for server: " + serverId, e);
    }
  }

  public void refreshPool(String serverId) {
    HikariDataSource oldPool = pools.remove(serverId);
    if (oldPool != null && !oldPool.isClosed()) {
      oldPool.close();
    }
    createPool(serverId);
  }

  public void closeAllPools() {
    for (HikariDataSource pool : pools.values()) {
      if (!pool.isClosed()) {
        pool.close();
      }
    }
    pools.clear();
  }
}
