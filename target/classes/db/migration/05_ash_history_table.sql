CREATE TABLE IF NOT EXISTS pash_ash_history (
    id BIGSERIAL PRIMARY KEY,
    database_name VARCHAR(100) NOT NULL,
    server_id VARCHAR(36),
    snapshot_time TIMESTAMP NOT NULL,
    pid INTEGER NOT NULL,
    state VARCHAR(50),
    wait_event_type VARCHAR(50),
    wait_event VARCHAR(100),
    query_hash VARCHAR(32),
    query TEXT,
    duration_seconds DOUBLE PRECISION,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_ash_server
        FOREIGN KEY (server_id)
        REFERENCES monitored_servers(id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_ash_snapshot_time ON pash_ash_history(snapshot_time);
CREATE INDEX IF NOT EXISTS idx_ash_database_time ON pash_ash_history(database_name, snapshot_time);
CREATE INDEX IF NOT EXISTS idx_ash_query_hash ON pash_ash_history(query_hash);
CREATE INDEX IF NOT EXISTS idx_ash_pid_time ON pash_ash_history(pid, snapshot_time);
CREATE INDEX IF NOT EXISTS idx_ash_server_id ON pash_ash_history(server_id);
