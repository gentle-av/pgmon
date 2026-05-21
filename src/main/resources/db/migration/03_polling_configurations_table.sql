-- 03_polling_configurations_table.sql
CREATE TABLE IF NOT EXISTS polling_configurations (
    id VARCHAR(36) PRIMARY KEY,
    server_id VARCHAR(36) NOT NULL REFERENCES monitored_servers(id) ON DELETE CASCADE,
    is_active BOOLEAN DEFAULT TRUE,
    priority INTEGER DEFAULT 5,
    schedule_type VARCHAR(50) DEFAULT 'interval',
    polling_interval_seconds INTEGER DEFAULT 30,
    polling_interval_ms INTEGER DEFAULT 5000,
    metric_collection_interval_seconds INTEGER DEFAULT 30,
    ash_collection_interval_seconds INTEGER DEFAULT 2,
    ash_collection_interval_ms INTEGER DEFAULT 2000,
    sessions_collection_interval_ms INTEGER DEFAULT 1000,
    cron_expression VARCHAR(100),
    timezone VARCHAR(50) DEFAULT 'UTC',
    start_time TIME,
    end_time TIME,
    active_days VARCHAR(13) DEFAULT '1234567',
    cache_ttl_seconds INTEGER DEFAULT 30,
    enable_caching BOOLEAN DEFAULT TRUE,
    collect_queries BOOLEAN DEFAULT TRUE,
    collect_connections BOOLEAN DEFAULT TRUE,
    collect_locks BOOLEAN DEFAULT TRUE,
    collect_cache_hit_ratio BOOLEAN DEFAULT TRUE,
    collect_table_sizes BOOLEAN DEFAULT TRUE,
    collect_unused_indexes BOOLEAN DEFAULT FALSE,
    collect_vacuum_stats BOOLEAN DEFAULT TRUE,
    collect_ash_data BOOLEAN DEFAULT TRUE,
    store_empty_snapshots BOOLEAN DEFAULT TRUE,
    slow_query_threshold_ms INTEGER,
    max_slow_queries INTEGER DEFAULT 10,
    lock_threshold_seconds INTEGER DEFAULT 30,
    dead_tuple_ratio_threshold_percent NUMERIC(5,2) DEFAULT 10.0,
    max_connection_threshold INTEGER,
    cache_evict_cron VARCHAR(100),
    retry_count INTEGER DEFAULT 3,
    retry_delay_seconds INTEGER DEFAULT 5,
    fallback_to_cache_on_error BOOLEAN DEFAULT TRUE,
    last_polling_start TIMESTAMP,
    last_polling_end TIMESTAMP,
    last_polling_duration_ms INTEGER,
    last_polling_error TEXT,
    polling_success_count INTEGER DEFAULT 0,
    polling_error_count INTEGER DEFAULT 0,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_polling_interval CHECK (
        (schedule_type = 'interval' AND polling_interval_seconds >= 5) OR
        (schedule_type = 'cron' AND cron_expression IS NOT NULL) OR
        (schedule_type = 'manual')
    ),
    CONSTRAINT chk_time_window CHECK (
        (start_time IS NULL AND end_time IS NULL) OR
        (start_time IS NOT NULL AND end_time IS NOT NULL AND start_time < end_time)
    )
);

CREATE INDEX IF NOT EXISTS idx_polling_active_priority ON polling_configurations(is_active, priority);
CREATE INDEX IF NOT EXISTS idx_polling_server ON polling_configurations(server_id);
CREATE INDEX IF NOT EXISTS idx_polling_schedule ON polling_configurations(schedule_type, is_active);
COMMENT ON COLUMN polling_configurations.polling_interval_ms IS 'Интервал опроса в миллисекундах (мин 100мс)';
COMMENT ON COLUMN polling_configurations.ash_collection_interval_ms IS 'Интервал сбора ASH в миллисекундах';
COMMENT ON COLUMN polling_configurations.sessions_collection_interval_ms IS 'Интервал сбора активных сессий в миллисекундах';
COMMENT ON COLUMN polling_configurations.store_empty_snapshots IS 'Записывать нулевые снапшоты при отсутствии активных сессий';
