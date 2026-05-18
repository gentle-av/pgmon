package avr.repository;

import avr.model.MonitoredServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MonitoredServerRepository extends JpaRepository<MonitoredServer, String> {
    List<MonitoredServer> findByEnabledTrue();
}
