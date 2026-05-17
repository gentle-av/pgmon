package avr.service;

import avr.config.ConfigRoot;
import avr.model.IndexInfo;
import avr.repository.IndexRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IndexAnalysisService {
    private final IndexRepository indexRepository;
    private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();
    public IndexAnalysisService(IndexRepository indexRepository) {
        this.indexRepository = indexRepository;
    }
    public List<IndexInfo> getUnusedIndexes(ConfigRoot.DatabaseConfig dbConfig) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbConfig);
        return indexRepository.getUnusedIndexes(jdbcTemplate);
    }
    private JdbcTemplate getJdbcTemplate(ConfigRoot.DatabaseConfig dbConfig) {
        return jdbcTemplates.computeIfAbsent(dbConfig.getName(), key -> {
            var builder = org.springframework.boot.jdbc.DataSourceBuilder.create();
            builder.url(String.format("jdbc:postgresql://%s:%d/%s", dbConfig.getHost(), dbConfig.getPort(), dbConfig.getDatabase()));
            builder.username(dbConfig.getUsername());
            builder.password(dbConfig.getPassword());
            builder.driverClassName("org.postgresql.Driver");
            return new JdbcTemplate(builder.build());
        });
    }
}
