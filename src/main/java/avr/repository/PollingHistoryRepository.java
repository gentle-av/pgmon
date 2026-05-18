package avr.repository;

import avr.model.PollingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollingHistoryRepository extends JpaRepository<PollingHistory, Long> {
}
