CREATE TABLE IF NOT EXISTS ash_history (
    id BIGSERIAL PRIMARY KEY,
    server_id VARCHAR(36) NOT NULL,
    server_name VARCHAR(255) NOT NULL,
    snapshot_time TIMESTAMP NOT NULL,
    pid INTEGER NOT NULL,
    state VARCHAR(50),
    wait_event_type VARCHAR(50),
    wait_event VARCHAR(100),
    query_hash VARCHAR(32),
    query TEXT,
    duration_seconds DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ash_snapshot_time ON ash_history(snapshot_time);
CREATE INDEX IF NOT EXISTS idx_ash_server_time ON ash_history(server_id, snapshot_time);
CREATE INDEX IF NOT EXISTS idx_ash_server_name_time ON ash_history(server_name, snapshot_time);
CREATE INDEX IF NOT EXISTS idx_ash_query_hash ON ash_history(query_hash);

CREATE OR REPLACE FUNCTION clean_old_ash_data(retention_days INTEGER DEFAULT 7)
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM ash_history
    WHERE snapshot_time < NOW() - (retention_days || ' days')::INTERVAL;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;
