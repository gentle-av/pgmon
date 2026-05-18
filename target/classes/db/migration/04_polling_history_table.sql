CREATE TABLE IF NOT EXISTS polling_history (
    id BIGSERIAL PRIMARY KEY,
    server_id VARCHAR(36) NOT NULL,
    polling_config_id VARCHAR(36) NOT NULL,
    polling_start TIMESTAMP NOT NULL,
    polling_end TIMESTAMP,
    duration_ms INTEGER,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    metrics_collected JSONB,
    metrics_count INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_polling_history_server
        FOREIGN KEY (server_id) REFERENCES monitored_servers(id) ON DELETE CASCADE,
    CONSTRAINT fk_polling_history_config
        FOREIGN KEY (polling_config_id) REFERENCES polling_configurations(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_polling_history_server_time
    ON polling_history(server_id, polling_start DESC);
CREATE INDEX IF NOT EXISTS idx_polling_history_status
    ON polling_history(status, polling_start);
CREATE INDEX IF NOT EXISTS idx_polling_history_config
    ON polling_history(polling_config_id, polling_start);
