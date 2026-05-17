CREATE SCHEMA IF NOT EXISTS public;

CREATE TABLE IF NOT EXISTS stored_database_credentials (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL DEFAULT 5432,
    database_name VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    encrypted_password TEXT NOT NULL,
    ssl_mode VARCHAR(50) DEFAULT 'disable',
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_credentials_enabled ON stored_database_credentials(enabled);
CREATE INDEX IF NOT EXISTS idx_credentials_name ON stored_database_credentials(name);
