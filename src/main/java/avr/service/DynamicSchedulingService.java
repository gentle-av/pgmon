package avr.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

import avr.model.MonitoredServer;
import avr.model.PollingConfiguration;
import avr.repository.MonitoredServerRepository;
import avr.repository.PollingConfigurationRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class DynamicSchedulingService {
    private static final Logger log = LoggerFactory.getLogger(DynamicSchedulingService.class);
    private final TaskScheduler taskScheduler;
    private final AshCollectorService ashCollectorService;
    private final PollingConfigurationRepository pollingConfigRepository;
    private final MonitoredServerRepository serverRepository;
    private final Map<String, ScheduledFuture<?>> ashTasks = new ConcurrentHashMap<>();

    public DynamicSchedulingService(TaskScheduler taskScheduler,
                                    AshCollectorService ashCollectorService,
                                    PollingConfigurationRepository pollingConfigRepository,
                                    MonitoredServerRepository serverRepository) {
        this.taskScheduler = taskScheduler;
        this.ashCollectorService = ashCollectorService;
        this.pollingConfigRepository = pollingConfigRepository;
        this.serverRepository = serverRepository;
    }

    @PostConstruct
    public void init() {
        log.info("Dynamic scheduling service started");
        scheduleAllServers();
    }

    @PreDestroy
    public void shutdown() {
        cancelAllTasks();
    }

    public void scheduleAllServers() {
        var configs = pollingConfigRepository.findByIsActiveTrueOrderByPriorityAsc();
        for (var config : configs) {
            var server = serverRepository.findById(config.getServerId()).orElse(null);
            if (server != null && server.isEnabled()) {
                scheduleServer(server, config);
            }
        }
    }

    public void scheduleServer(MonitoredServer server, PollingConfiguration config) {
        String serverId = server.getId();
        cancelTask(ashTasks, serverId + "_ash");
        if (config.isActive() && config.isCollectAshData()) {
            Runnable ashTask = () -> {
                if (shouldExecuteNow(config)) {
                    boolean storeEmpty = config.getStoreEmptySnapshots() != null ? config.getStoreEmptySnapshots() : true;
                    ashCollectorService.collectAshSnapshotsForServer(server, storeEmpty);
                }
            };
            int ashIntervalMs = config.getAshCollectionIntervalMs() > 0 ? config.getAshCollectionIntervalMs() : 2000;
            ScheduledFuture<?> ashFuture = scheduleWithPrecision(ashTask, ashIntervalMs);
            ashTasks.put(serverId + "_ash", ashFuture);
            log.info("Scheduled ASH collector for {} with interval {} ms", server.getServerName(), ashIntervalMs);
        }
    }

    private ScheduledFuture<?> scheduleWithPrecision(Runnable task, int intervalMs) {
        int actualInterval = Math.max(100, intervalMs);
        PeriodicTrigger trigger = new PeriodicTrigger(Duration.ofMillis(actualInterval));
        trigger.setInitialDelay(Duration.ofMillis(0));
        trigger.setFixedRate(true);
        return taskScheduler.schedule(task, trigger);
    }

    private boolean shouldExecuteNow(PollingConfiguration config) {
        if (!config.isActive()) return false;
        if (config.getStartTime() != null && config.getEndTime() != null) {
            var now = LocalDateTime.now();
            var currentTime = now.toLocalTime();
            if (currentTime.isBefore(config.getStartTime()) || currentTime.isAfter(config.getEndTime())) {
                return false;
            }
        }
        if (config.getActiveDays() != null) {
            int currentDay = LocalDateTime.now().getDayOfWeek().getValue();
            if (!config.getActiveDays().contains(String.valueOf(currentDay))) {
                return false;
            }
        }
        return true;
    }

    public void rescheduleServer(String serverId) {
        var config = pollingConfigRepository.findByServerId(serverId).orElse(null);
        var server = serverRepository.findById(serverId).orElse(null);
        if (config != null && server != null) {
            cancelServerTasks(serverId);
            if (config.isActive() && server.isEnabled()) {
                scheduleServer(server, config);
            }
        }
    }

    private void cancelTask(Map<String, ScheduledFuture<?>> taskMap, String key) {
        ScheduledFuture<?> task = taskMap.remove(key);
        if (task != null && !task.isDone()) {
            task.cancel(false);
        }
    }

    private void cancelServerTasks(String serverId) {
        cancelTask(ashTasks, serverId + "_ash");
    }

    private void cancelAllTasks() {
        ashTasks.keySet().forEach(key -> cancelTask(ashTasks, key));
    }

    public void updateStoreEmptySnapshots(String serverId, Boolean storeEmpty) {
        var config = pollingConfigRepository.findByServerId(serverId).orElse(null);
        var server = serverRepository.findById(serverId).orElse(null);
        if (config != null && server != null) {
            config.setStoreEmptySnapshots(storeEmpty);
            pollingConfigRepository.save(config);
            rescheduleServer(serverId);
            log.info("Updated ASH collector for {} with storeEmpty={}", server.getServerName(), storeEmpty);
        }
    }
}
