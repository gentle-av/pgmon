package avr.service;

import avr.model.PollingHistory;
import avr.repository.PollingHistoryRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class PollingHistoryService {
    private final PollingHistoryRepository pollingHistoryRepository;

    public PollingHistoryService(PollingHistoryRepository pollingHistoryRepository) {
        this.pollingHistoryRepository = pollingHistoryRepository;
    }

    public Long recordPollingStart(String serverId, String configId) {
        PollingHistory history = new PollingHistory();
        history.setServerId(serverId);
        history.setPollingConfigId(configId);
        history.setPollingStart(LocalDateTime.now());
        history.setStatus("running");
        history.setCreatedAt(LocalDateTime.now());
        PollingHistory saved = pollingHistoryRepository.save(history);
        return saved.getId();
    }

    public void recordPollingSuccess(Long historyId, long durationMs) {
        PollingHistory history = pollingHistoryRepository.findById(historyId).orElse(null);
        if (history != null) {
            history.setPollingEnd(LocalDateTime.now());
            history.setDurationMs(durationMs);
            history.setStatus("success");
            pollingHistoryRepository.save(history);
        }
    }

    public void recordPollingFailure(Long historyId, String errorMessage) {
        PollingHistory history = pollingHistoryRepository.findById(historyId).orElse(null);
        if (history != null) {
            history.setPollingEnd(LocalDateTime.now());
            history.setStatus("failed");
            history.setErrorMessage(errorMessage);
            pollingHistoryRepository.save(history);
        }
    }
}
