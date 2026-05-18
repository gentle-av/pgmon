-- Функция обновления updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Триггеры
DROP TRIGGER IF EXISTS trigger_update_servers_updated_at ON monitored_servers;
CREATE TRIGGER trigger_update_servers_updated_at
    BEFORE UPDATE ON monitored_servers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trigger_update_polling_updated_at ON polling_configurations;
CREATE TRIGGER trigger_update_polling_updated_at
    BEFORE UPDATE ON polling_configurations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Функция обновления статуса сервера
CREATE OR REPLACE FUNCTION update_server_status()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'online' OR NEW.status = 'offline' THEN
        NEW.last_checked = CURRENT_TIMESTAMP;
        IF NEW.status = 'offline' THEN
            NEW.last_error = 'Connection failed at ' || CURRENT_TIMESTAMP;
        ELSE
            NEW.last_error = NULL;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_update_server_status ON monitored_servers;
CREATE TRIGGER trigger_update_server_status
    BEFORE UPDATE OF status ON monitored_servers
    FOR EACH ROW
    EXECUTE FUNCTION update_server_status();

-- Функция очистки ASH данных
CREATE OR REPLACE FUNCTION clean_old_ash_data(retention_days INTEGER DEFAULT 7)
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM pash_ash_history
    WHERE snapshot_time < NOW() - (retention_days || ' days')::INTERVAL;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Функция очистки истории опросов
CREATE OR REPLACE FUNCTION clean_old_polling_history(retention_days INTEGER DEFAULT 30)
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM polling_history
    WHERE created_at < NOW() - (retention_days || ' days')::INTERVAL;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Функция получения активных конфигураций
CREATE OR REPLACE FUNCTION get_active_polling_configs()
RETURNS TABLE(
    server_id VARCHAR(36),
    server_name VARCHAR(255),
    credential_id VARCHAR(36),
    priority INTEGER,
    polling_interval_seconds INTEGER,
    ash_collection_interval_seconds INTEGER,
    slow_query_threshold_ms INTEGER,
    collect_queries BOOLEAN,
    collect_locks BOOLEAN,
    jdbc_url TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        ms.id AS server_id,
        ms.server_name,
        ms.credential_id,
        pc.priority,
        COALESCE(pc.polling_interval_seconds, 30) AS polling_interval_seconds,
        COALESCE(pc.ash_collection_interval_seconds, 2) AS ash_collection_interval_seconds,
        COALESCE(pc.slow_query_threshold_ms, 1000) AS slow_query_threshold_ms,
        pc.collect_queries,
        pc.collect_locks,
        FORMAT('jdbc:postgresql://%s:%d/%s',
            sdc.host, sdc.port, sdc.database_name) AS jdbc_url
    FROM monitored_servers ms
    INNER JOIN stored_database_credentials sdc ON ms.credential_id = sdc.id
    INNER JOIN polling_configurations pc ON ms.id = pc.server_id
    WHERE ms.enabled = TRUE
      AND pc.is_active = TRUE
      AND (
          pc.start_time IS NULL OR
          CURRENT_TIME BETWEEN pc.start_time AND pc.end_time
      )
      AND (
          pc.active_days IS NULL OR
          POSITION(CAST(EXTRACT(DOW FROM CURRENT_TIMESTAMP) + 1 AS VARCHAR) IN pc.active_days) > 0
      )
    ORDER BY pc.priority ASC;
END;
$$ LANGUAGE plpgsql;
