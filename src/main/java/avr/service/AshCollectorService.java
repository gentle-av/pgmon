package avr.service;

import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import avr.model.MonitoredServer;
import avr.model.PollingConfiguration;
import avr.repository.AshHistoryRepository;
import avr.repository.MonitoredServerRepository;
import avr.repository.PollingConfigurationRepository;

@Service
public class AshCollectorService {
  private static final Logger log = LoggerFactory.getLogger(AshCollectorService.class);
  private final AshHistoryRepository ashHistoryRepository;
  private final ConnectionPoolManager connectionPoolManager;
  private final MonitoredServerRepository serverRepository;
  private final PollingConfigurationRepository pollingConfigRepository;

  public AshCollectorService(AshHistoryRepository ashHistoryRepository,
      ConnectionPoolManager connectionPoolManager,
      MonitoredServerRepository serverRepository,
      PollingConfigurationRepository pollingConfigRepository) {
    this.ashHistoryRepository = ashHistoryRepository;
    this.connectionPoolManager = connectionPoolManager;
    this.serverRepository = serverRepository;
    this.pollingConfigRepository = pollingConfigRepository;
  }

  public void collectAshSnapshotsForServer(MonitoredServer server, boolean storeEmpty) {
    PollingConfiguration pollingConfig = pollingConfigRepository.findByServerId(server.getId()).orElse(null);
    if (pollingConfig == null || !pollingConfig.isCollectAshData()) {
      return;
    }
    try {
      DataSource targetDataSource = connectionPoolManager.getDataSource(server.getId());
      JdbcTemplate targetJdbcTemplate = new JdbcTemplate(targetDataSource);
      List<Map<String, Object>> sessions = ashHistoryRepository.getRawActiveSessions(targetJdbcTemplate);
      ashHistoryRepository.insertSnapshot(server, sessions, storeEmpty);
      log.debug("Собрано {} сессий для БД {}", sessions.size(), server.getServerName());
    } catch (Exception e) {
      log.error("ASH collection failed for {}: {}", server.getServerName(), e.getMessage(), e);
    }
  }
}
