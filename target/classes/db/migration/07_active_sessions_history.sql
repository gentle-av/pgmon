CREATE TABLE IF NOT EXISTS active_sessions_history (
    id BIGSERIAL PRIMARY KEY,
    server_id VARCHAR(36) NOT NULL,
    snapshot_time TIMESTAMP NOT NULL,
    pid INTEGER NOT NULL,
    username VARCHAR(100),
    state VARCHAR(50),
    wait_event_type VARCHAR(50),
    wait_event VARCHAR(100),
    query TEXT,
    duration_seconds DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ash_server_time ON active_sessions_history(server_id, snapshot_time);
