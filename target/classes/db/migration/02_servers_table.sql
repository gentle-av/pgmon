CREATE TABLE IF NOT EXISTS monitored_servers (
    id VARCHAR(36) PRIMARY KEY,
    credential_id VARCHAR(36) NOT NULL,
    server_name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    environment VARCHAR(50) DEFAULT 'production',
    description TEXT,
    connection_timeout_ms INTEGER DEFAULT 5000,
    socket_timeout_ms INTEGER DEFAULT 30000,
    tags TEXT[] DEFAULT '{}',
    groups TEXT[] DEFAULT '{}',
    enabled BOOLEAN DEFAULT TRUE,
    status VARCHAR(50) DEFAULT 'unknown',
    last_checked TIMESTAMP,
    last_error TEXT,
    postgres_version VARCHAR(50),
    data_directory VARCHAR(500),
    max_connections INTEGER,
    shared_buffers_mb INTEGER,
    effective_cache_size_mb INTEGER,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_server_credential
        FOREIGN KEY (credential_id)
        REFERENCES stored_database_credentials(id)
        ON DELETE RESTRICT,
    CONSTRAINT uk_server_name UNIQUE (server_name)
);

CREATE INDEX IF NOT EXISTS idx_servers_credential ON monitored_servers(credential_id);
CREATE INDEX IF NOT EXISTS idx_servers_enabled_status ON monitored_servers(enabled, status);
CREATE INDEX IF NOT EXISTS idx_servers_environment ON monitored_servers(environment);
CREATE INDEX IF NOT EXISTS idx_servers_tags ON monitored_servers USING GIN(tags);
CREATE INDEX IF NOT EXISTS idx_servers_groups ON monitored_servers USING GIN(groups);
CREATE INDEX IF NOT EXISTS idx_servers_last_checked ON monitored_servers(last_checked);
