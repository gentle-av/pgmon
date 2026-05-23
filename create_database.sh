#!/bin/bash

DB_NAME="pgmon"
DB_USER="pgmon_user"
DB_PASSWORD="${PGMON_PASSWORD:-pgmon_pass}"
DB_HOST="localhost"
DB_PORT="5432"

echo "Creating database $DB_NAME..."

sudo -u postgres psql -c "DROP DATABASE IF EXISTS $DB_NAME;"
sudo -u postgres psql -c "DROP USER IF EXISTS $DB_USER;"
sudo -u postgres psql -c "CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';"
sudo -u postgres psql -c "CREATE DATABASE $DB_NAME OWNER $DB_USER;"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;"

PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME <<EOF
CREATE TABLE ash_history (
    id bigserial NOT NULL,
    server_id varchar(36) NOT NULL,
    server_name varchar(255) NOT NULL,
    snapshot_time timestamp NOT NULL,
    pid integer,
    state varchar(50),
    wait_event_type varchar(50),
    wait_event varchar(100),
    query_hash varchar(32),
    query text,
    duration_seconds double precision,
    session_count integer DEFAULT 1,
    CONSTRAINT ash_history_pkey PRIMARY KEY (id)
);

CREATE TABLE stored_database_credentials (
    id varchar(36) NOT NULL,
    name varchar(255) NOT NULL,
    host varchar(255) NOT NULL,
    port integer DEFAULT 5432 NOT NULL,
    database_name varchar(255) NOT NULL,
    username varchar(255) NOT NULL,
    encrypted_password text NOT NULL,
    ssl_mode varchar(50) DEFAULT 'disable',
    enabled boolean DEFAULT true,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT stored_database_credentials_pkey PRIMARY KEY (id),
    CONSTRAINT stored_database_credentials_name_key UNIQUE (name)
);

CREATE TABLE monitored_servers (
    id varchar(36) NOT NULL,
    credential_id varchar(36) NOT NULL,
    server_name varchar(255) NOT NULL,
    display_name varchar(255),
    environment varchar(50) DEFAULT 'production',
    description text,
    connection_timeout_ms integer DEFAULT 5000,
    enabled boolean DEFAULT true,
    status varchar(50) DEFAULT 'unknown',
    last_checked timestamp,
    last_error text,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT monitored_servers_pkey PRIMARY KEY (id),
    CONSTRAINT monitored_servers_server_name_key UNIQUE (server_name),
    CONSTRAINT fk_server_credential FOREIGN KEY (credential_id) REFERENCES stored_database_credentials(id) ON DELETE RESTRICT
);

CREATE TABLE polling_configurations (
    id varchar(36) NOT NULL,
    server_id varchar(36) NOT NULL,
    is_active boolean DEFAULT true,
    priority integer DEFAULT 5,
    polling_interval_ms integer DEFAULT 30000,
    ash_collection_interval_ms integer DEFAULT 2000,
    sessions_collection_interval_ms integer DEFAULT 1000,
    collect_ash_data boolean DEFAULT true,
    store_empty_snapshots boolean DEFAULT true,
    start_time time,
    end_time time,
    active_days varchar(7) DEFAULT '1234567',
    created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT polling_configurations_pkey PRIMARY KEY (id),
    CONSTRAINT fk_polling_server FOREIGN KEY (server_id) REFERENCES monitored_servers(id) ON DELETE CASCADE
);

CREATE INDEX idx_ash_server_name_time ON ash_history(server_name, snapshot_time);
CREATE INDEX idx_polling_server ON polling_configurations(server_id);
CREATE INDEX idx_servers_credential ON monitored_servers(credential_id);

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO $DB_USER;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO $DB_USER;

EOF

echo "Database $DB_NAME created successfully"
