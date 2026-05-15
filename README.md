# PostgreSQL Monitoring System

Система детального мониторинга PostgreSQL на Spring Boot с использованием Java 21+ Records.

## Возможности

### Метрики БД
- Размер базы данных
- Активные/idle/ожидающие соединения
- Cache hit ratio
- Transaction commit ratio
- Статистика INSERT/UPDATE/DELETE/FETCH
- Dead tuples и статистика VACUUM

### Анализ запросов
- Самые медленные запросы
- Самые частые запросы
- Детальная статистика по каждому запросу (время, строки, блоки)

### Мониторинг блокировок
- Обнаружение блокирующих locks
- Определение блокирующего и заблокированного запросов
- Время ожидания

### Индексы
- Неиспользуемые индексы
- Размер индексов
- Статистика использования

### Таблицы
- Самые большие таблицы
- Размер таблиц и индексов

### Мониторинг VACUUM
- Dead tuples по таблицам
- Время последнего VACUUM/ANALYZE
- Счётчики VACUUM

## API Endpoints

### Конфигурация
| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/config/databases` | Список БД для мониторинга |
| GET | `/api/config/server` | Порт сервера |
| GET | `/api/config/monitoring` | Настройки мониторинга |

### Метрики
| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/metrics/all/{dbName}` | Все метрики по БД |
| GET | `/api/metrics/connections/{dbName}` | Активные соединения |
| GET | `/api/metrics/slow-queries/{dbName}` | Медленные запросы |
| GET | `/api/metrics/locks/{dbName}` | Блокировки |
| GET | `/api/metrics/unused-indexes/{dbName}` | Неиспользуемые индексы |
| GET | `/api/metrics/vacuum/{dbName}` | Статистика VACUUM |

### Здоровье
| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/health` | Статус всех БД |
| GET | `/api/health/{dbName}` | Статус конкретной БД |

### Алерты
| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/alerts` | Активные алерты |

### Таблицы
| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/tables/largest/{dbName}` | Самые большие таблицы |

### Prometheus
| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/actuator/prometheus` | Метрики для Prometheus |
| GET | `/actuator/health` | Health check Spring Boot |

## Настройка

### `monitoring-config.json`

```json
{
  "server": { "port": 3001 },
  "databases": [
    {
      "name": "gentle",
      "host": "192.168.50.11",
      "port": 5432,
      "database": "avr",
      "username": "avr",
      "password": "1",
      "enabled": true,
      "monitoringIntervalSeconds": 30
    }
  ],
  "monitoring": {
    "defaultIntervalSeconds": 30,
    "collectQueries": true,
    "collectConnections": true,
    "collectLocks": true,
    "collectCacheHitRatio": true,
    "collectTableSizes": true,
    "collectIndexUsage": true,
    "collectBloat": true,
    "collectReplication": true,
    "collectVacuum": true,
    "slowQueryThresholdMs": 1000,
    "maxSlowQueries": 10,
    "bloatWarningPercent": 20,
    "deadTupleWarningPercent": 10
  },
  "alerting": {
    "enabled": true,
    "webhookUrl": "http://localhost:9090/alerts",
    "connectionThresholds": {
      "maxConnections": 100,
      "minAvailableConnections": 5
    },
    "lockThresholdSeconds": 30,
    "cacheHitRatioMinPercent": 95
  }
}
