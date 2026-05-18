package avr.service;

import avr.dto.RegisterServerRequest;
import avr.dto.UpdatePollingRequest;
import avr.dto.UpdateServerRequest;
import avr.model.MonitoredServer;
import avr.model.PollingConfiguration;
import avr.model.StoredCredential;
import avr.repository.MonitoredServerRepository;
import avr.repository.PollingConfigurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ServerManagementService {
    private final MonitoredServerRepository serverRepository;
    private final PollingConfigurationRepository pollingConfigRepository;
    private final CredentialService credentialService;
    private final ConnectionPoolManager connectionPoolManager;

    public ServerManagementService(MonitoredServerRepository serverRepository,
                                   PollingConfigurationRepository pollingConfigRepository,
                                   CredentialService credentialService,
                                   ConnectionPoolManager connectionPoolManager) {
        this.serverRepository = serverRepository;
        this.pollingConfigRepository = pollingConfigRepository;
        this.credentialService = credentialService;
        this.connectionPoolManager = connectionPoolManager;
    }

    @Transactional
    public MonitoredServer registerServer(RegisterServerRequest request) {
        StoredCredential credential = credentialService.getCredentialByName(request.getCredentialName())
            .orElseGet(() -> createCredential(request));
        MonitoredServer server = new MonitoredServer();
        server.setId(UUID.randomUUID().toString());
        server.setCredentialId(credential.getId());
        server.setServerName(request.getServerName());
        server.setDisplayName(request.getDisplayName());
        server.setEnvironment(request.getEnvironment());
        server.setDescription(request.getDescription());
        server.setConnectionTimeoutMs(request.getConnectionTimeoutMs());
        server.setEnabled(request.isEnabled());
        server.setStatus("unknown");
        server.setCreatedAt(LocalDateTime.now());
        server.setUpdatedAt(LocalDateTime.now());
        MonitoredServer saved = serverRepository.save(server);
        PollingConfiguration config = createPollingConfiguration(saved.getId(), request);
        pollingConfigRepository.save(config);
        return saved;
    }

    private StoredCredential createCredential(RegisterServerRequest request) {
        StoredCredential credential = new StoredCredential();
        credential.setId(UUID.randomUUID().toString());
        credential.setName(request.getCredentialName());
        credential.setHost(request.getHost());
        credential.setPort(request.getPort());
        credential.setDatabaseName(request.getDatabaseName());
        credential.setUsername(request.getUsername());
        credential.setEncryptedPassword(request.getPassword());
        credential.setSslMode(request.getSslMode());
        credential.setEnabled(true);
        credential.setCreatedAt(LocalDateTime.now());
        credential.setUpdatedAt(LocalDateTime.now());
        return credentialService.saveCredentialWithoutEncryption(credential);
    }

    private PollingConfiguration createPollingConfiguration(String serverId, RegisterServerRequest request) {
        PollingConfiguration config = new PollingConfiguration();
        config.setId(UUID.randomUUID().toString());
        config.setServerId(serverId);
        config.setActive(true);
        config.setPriority(request.getPriority());
        config.setScheduleType("interval");
        config.setPollingIntervalSeconds(request.getPollingIntervalSeconds());
        config.setAshCollectionIntervalSeconds(2);
        config.setCollectQueries(request.isCollectQueries());
        config.setCollectConnections(request.isCollectConnections());
        config.setCollectLocks(request.isCollectLocks());
        config.setCollectCacheHitRatio(request.isCollectCacheHitRatio());
        config.setCollectUnusedIndexes(request.isCollectUnusedIndexes());
        config.setCollectVacuumStats(request.isCollectVacuumStats());
        config.setCollectAshData(request.isCollectAshData());
        config.setSlowQueryThresholdMs(request.getSlowQueryThresholdMs());
        config.setMaxSlowQueries(request.getMaxSlowQueries());
        config.setDeadTupleRatioThresholdPercent(request.getDeadTupleRatioThresholdPercent());
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return config;
    }

    public List<MonitoredServer> getAllServers() {
        return serverRepository.findAll();
    }

    public List<MonitoredServer> getEnabledServers() {
        return serverRepository.findByEnabledTrue();
    }

    public Optional<MonitoredServer> getServer(String id) {
        return serverRepository.findById(id);
    }

    public Optional<MonitoredServer> getServerByName(String name) {
        return serverRepository.findAll().stream()
            .filter(s -> s.getServerName().equals(name))
            .findFirst();
    }

    @Transactional
    public MonitoredServer updateServer(String id, UpdateServerRequest request) {
        MonitoredServer server = serverRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Server not found: " + id));
        if (request.getDisplayName() != null) server.setDisplayName(request.getDisplayName());
        if (request.getEnvironment() != null) server.setEnvironment(request.getEnvironment());
        if (request.getDescription() != null) server.setDescription(request.getDescription());
        if (request.getConnectionTimeoutMs() > 0) server.setConnectionTimeoutMs(request.getConnectionTimeoutMs());
        server.setEnabled(request.isEnabled());
        if (request.getStatus() != null) server.setStatus(request.getStatus());
        server.setUpdatedAt(LocalDateTime.now());
        return serverRepository.save(server);
    }

    @Transactional
    public void deleteServer(String id) {
        pollingConfigRepository.findByServerId(id).ifPresent(pollingConfigRepository::delete);
        connectionPoolManager.refreshPool(id);
        serverRepository.deleteById(id);
    }

    @Transactional
    public MonitoredServer enableServer(String id) {
        MonitoredServer server = serverRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Server not found: " + id));
        server.setEnabled(true);
        server.setUpdatedAt(LocalDateTime.now());
        return serverRepository.save(server);
    }

    @Transactional
    public MonitoredServer disableServer(String id) {
        MonitoredServer server = serverRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Server not found: " + id));
        server.setEnabled(false);
        server.setUpdatedAt(LocalDateTime.now());
        return serverRepository.save(server);
    }

    public Optional<PollingConfiguration> getPollingConfig(String serverId) {
        return pollingConfigRepository.findByServerId(serverId);
    }

    @Transactional
    public PollingConfiguration updatePollingConfig(String serverId, UpdatePollingRequest request) {
        PollingConfiguration config = pollingConfigRepository.findByServerId(serverId)
            .orElseThrow(() -> new RuntimeException("Polling config not found for server: " + serverId));
        if (request.getPollingIntervalSeconds() > 0) config.setPollingIntervalSeconds(request.getPollingIntervalSeconds());
        if (request.getSlowQueryThresholdMs() != null) config.setSlowQueryThresholdMs(request.getSlowQueryThresholdMs());
        if (request.getMaxSlowQueries() != null) config.setMaxSlowQueries(request.getMaxSlowQueries());
        if (request.getPriority() > 0) config.setPriority(request.getPriority());
        config.setCollectQueries(request.isCollectQueries());
        config.setCollectConnections(request.isCollectConnections());
        config.setCollectLocks(request.isCollectLocks());
        config.setCollectCacheHitRatio(request.isCollectCacheHitRatio());
        config.setCollectUnusedIndexes(request.isCollectUnusedIndexes());
        config.setCollectVacuumStats(request.isCollectVacuumStats());
        config.setCollectAshData(request.isCollectAshData());
        config.setUpdatedAt(LocalDateTime.now());
        return pollingConfigRepository.save(config);
    }

    public void refreshConnectionPool(String serverId) {
        connectionPoolManager.refreshPool(serverId);
    }
}
