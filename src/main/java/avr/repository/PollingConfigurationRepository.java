package avr.repository;

import avr.model.PollingConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PollingConfigurationRepository extends JpaRepository<PollingConfiguration, String> {
    List<PollingConfiguration> findByIsActiveTrueOrderByPriorityAsc();

    Optional<PollingConfiguration> findByServerId(String serverId);
}
