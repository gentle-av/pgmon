const App = {
  data: {
    databases: [],
    metrics: {},
    refreshInterval: null,
  },

  async init() {
    Framework.init({ debug: true });
    await this.loadDatabases();
    await this.loadAllMetrics();
    this.startAutoRefresh();
    this.bindEvents();
  },

  async loadDatabases() {
    try {
      this.data.databases = await HTTP.get("/api/config/databases");
      Framework.log("Databases loaded:", this.data.databases);
    } catch (error) {
      Framework.error("Failed to load databases:", error);
      this.showError("Failed to load databases");
    }
  },

  async loadAllMetrics() {
    for (const db of this.data.databases) {
      if (!db.enabled) continue;
      await this.loadDatabaseMetrics(db.name);
    }
    this.render();
    this.updateTimestamp();
  },

  async loadDatabaseMetrics(dbName) {
    try {
      const metrics = await HTTP.get(`/api/metrics/all/${dbName}`);
      this.data.metrics[dbName] = metrics;
      Framework.log(`Metrics loaded for ${dbName}:`, metrics);
    } catch (error) {
      Framework.error(`Failed to load metrics for ${dbName}:`, error);
      this.data.metrics[dbName] = { error: error.message };
    }
  },

  async refreshData() {
    Framework.log("Refreshing data...");
    await this.loadAllMetrics();
    Framework.log("Data refreshed");
  },

  startAutoRefresh() {
    if (this.data.refreshInterval) {
      clearInterval(this.data.refreshInterval);
    }

    const interval =
      parseInt(document.body.dataset.refreshInterval || "10") * 1000;
    this.data.refreshInterval = setInterval(() => {
      this.refreshData();
    }, interval);
  },

  bindEvents() {
    const refreshBtn = DOM.get("#refresh-btn");
    if (refreshBtn) {
      Events.on(refreshBtn, "click", () => this.refreshData());
    }
  },

  render() {
    const container = DOM.get("#databases-container");
    if (!container) return;

    container.innerHTML = "";

    for (const db of this.data.databases) {
      if (!db.enabled) continue;
      const card = this.createDatabaseCard(db);
      container.appendChild(card);
    }
  },

  createDatabaseCard(db) {
    const metrics = this.data.metrics[db.name] || {};
    const stats = metrics.database_stats || {};
    const connections = metrics.active_connections || [];
    const slowQueries = metrics.slow_queries || [];
    const locks = metrics.blocking_locks || [];

    const card = DOM.create("div", { class: "card database-card" });

    const header = DOM.create("div", { class: "card-header" });
    const title = DOM.create("h3", { class: "card-title" }, [`🗄️ ${db.name}`]);
    header.appendChild(title);

    const body = DOM.create("div", { class: "card-body" });

    const metricsList = [
      { label: "Size", value: stats.sizeHuman || "N/A", status: "info" },
      {
        label: "Active Connections",
        value: stats.activeConnections || connections.length || 0,
        status: stats.activeConnections > 50 ? "warning" : "ok",
      },
      {
        label: "Cache Hit Ratio",
        value: `${stats.cacheHitRatio || 0}%`,
        status: (stats.cacheHitRatio || 0) < 90 ? "warning" : "ok",
      },
      {
        label: "Running Queries",
        value: metrics.running_queries_count || 0,
        status: "info",
      },
      {
        label: "Waiting Queries",
        value: metrics.waiting_queries_count || 0,
        status: metrics.waiting_queries_count > 5 ? "warning" : "ok",
      },
      {
        label: "Slow Queries",
        value: slowQueries.length,
        status: slowQueries.length > 0 ? "warning" : "ok",
      },
      {
        label: "Blocking Locks",
        value: locks.length,
        status: locks.length > 0 ? "error" : "ok",
      },
      {
        label: "Unused Indexes",
        value: metrics.unused_indexes?.length || 0,
        status: "warning",
      },
    ];

    metricsList.forEach((metric) => {
      const div = DOM.create("div", { class: "metric" });
      const label = DOM.create("span", { class: "metric-label" }, [
        metric.label,
      ]);
      const value = DOM.create(
        "span",
        { class: `metric-value status-${metric.status}` },
        [String(metric.value)],
      );
      div.appendChild(label);
      div.appendChild(value);
      body.appendChild(div);
    });

    card.appendChild(header);
    card.appendChild(body);

    return card;
  },

  showError(message) {
    const container = DOM.get("#databases-container");
    if (container) {
      const errorDiv = DOM.create("div", { class: "card" }, [message]);
      container.appendChild(errorDiv);
    }
    console.error(message);
  },

  updateTimestamp() {
    const timestamp = DOM.get("#timestamp");
    if (timestamp) {
      const now = new Date();
      timestamp.textContent = `Last updated: ${now.toLocaleTimeString()}`;
    }
  },
};

document.addEventListener("DOMContentLoaded", () => {
  App.init();
});
