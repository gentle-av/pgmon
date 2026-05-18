package avr.service;

import avr.model.IndexInfo;
import avr.model.MonitoredServer;
import avr.repository.IndexRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.util.List;

@Service
public class IndexAnalysisService {
    private final IndexRepository indexRepository;
    private final ConnectionPoolManager connectionPoolManager;

    public IndexAnalysisService(IndexRepository indexRepository,
                                 ConnectionPoolManager connectionPoolManager) {
        this.indexRepository = indexRepository;
        this.connectionPoolManager = connectionPoolManager;
    }

    public List<IndexInfo> getUnusedIndexes(MonitoredServer server) {
        DataSource dataSource = connectionPoolManager.getDataSource(server.getId());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return indexRepository.getUnusedIndexes(jdbcTemplate);
    }
}
