const App = {
  data: {
    databases: [],
    metrics: {},
    alerts: [],
    charts: {
      connections: null,
      sizes: null,
      cache: null,
      queries: null,
    },
    history: {
      connections: {},
      sizes: {},
      cacheHits: {},
      queries: {},
    },
    refreshInterval: null,
    theme: "dark",
    lastUpdate: null,
  },

  async init() {
    console.log("🚀 Initializing PostgreSQL Monitor...");

    // Get databases from window object
    if (
      window.__INITIAL_DATABASES__ &&
      window.__INITIAL_DATABASES__.length > 0
    ) {
      this.data.databases = window.__INITIAL_DATABASES__;
      console.log(
        `✅ Loaded ${this.data.databases.length} databases from server`,
      );
    } else {
      console.error("❌ No databases from server!");
      this.showError("No databases configured");
      return;
    }

    this.data.theme = document.body.dataset.theme || "dark";

    const enabledDatabases = this.data.databases.filter((db) => db.enabled);
    console.log(`📊 Enabled databases: ${enabledDatabases.length}`);
    enabledDatabases.forEach((db) => {
      console.log(`  - ${db.name}: ${db.host}:${db.port}`);
    });

    if (enabledDatabases.length === 0) {
      this.showError("No enabled databases configured");
      return;
    }

    // Initialize history arrays
    for (const db of enabledDatabases) {
      this.data.history.connections[db.name] = [];
      this.data.history.sizes[db.name] = [];
      this.data.history.cacheHits[db.name] = [];
      this.data.history.queries[db.name] = [];
    }

    // Initialize charts
    this.initCharts();

    // Load initial data
    await this.loadAllMetrics();
    await this.loadAlerts();

    // Start auto-refresh
    this.startAutoRefresh();
    this.bindEvents();
  },

  showError(message) {
    const container = document.getElementById("databases-container");
    if (container) {
      container.innerHTML = `
        <div class="card" style="text-align: center; border-left-color: var(--danger);">
          <h3 style="color: var(--danger);">⚠️ Error</h3>
          <p>${message}</p>
        </div>
      `;
    }
    console.error(message);
  },

  async loadAllMetrics() {
    const enabledDatabases = this.data.databases.filter((db) => db.enabled);

    for (const db of enabledDatabases) {
      await this.loadDatabaseMetrics(db.name);
    }

    this.renderDatabases();
    this.updateCharts();
    this.updateStatsSummary();
    this.updateTimestamp();
  },

  async loadDatabaseMetrics(dbName) {
    try {
      const metrics = await HTTP.get(`/api/metrics/all/${dbName}`);
      this.data.metrics[dbName] = metrics;

      const stats = metrics.database_stats || {};
      const now = new Date();
      const timeStr = now.toLocaleTimeString();

      // Add to history (keep last 20 points)
      const connections = stats.activeConnections || 0;
      const sizeGB = (stats.sizeBytes || 0) / 1024 / 1024 / 1024;
      const cacheHit = stats.cacheHitRatio || 0;
      const runningQueries = metrics.running_queries_count || 0;

      this.data.history.connections[dbName].push({
        time: timeStr,
        value: connections,
      });
      this.data.history.sizes[dbName].push({ time: timeStr, value: sizeGB });
      this.data.history.cacheHits[dbName].push({
        time: timeStr,
        value: cacheHit,
      });
      this.data.history.queries[dbName].push({
        time: timeStr,
        value: runningQueries,
      });

      // Keep only last 20 points
      const maxHistory = 20;
      if (this.data.history.connections[dbName].length > maxHistory)
        this.data.history.connections[dbName].shift();
      if (this.data.history.sizes[dbName].length > maxHistory)
        this.data.history.sizes[dbName].shift();
      if (this.data.history.cacheHits[dbName].length > maxHistory)
        this.data.history.cacheHits[dbName].shift();
      if (this.data.history.queries[dbName].length > maxHistory)
        this.data.history.queries[dbName].shift();

      console.log(
        `📈 Updated metrics for ${dbName}: connections=${connections}, cache=${cacheHit}%`,
      );
    } catch (error) {
      console.error(`❌ Failed to load metrics for ${dbName}:`, error);
      this.data.metrics[dbName] = { error: error.message };
    }
  },

  renderDatabases() {
    const container = document.getElementById("databases-container");
    if (!container) return;

    const enabledDatabases = this.data.databases.filter((db) => db.enabled);
    container.innerHTML = "";

    for (const db of enabledDatabases) {
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
    const hasError = metrics.error;

    const card = document.createElement("div");
    card.className = "card database-card";

    if (hasError) {
      card.innerHTML = `
        <div class="card-header">
          <h3 class="card-title">🐘 ${db.name}</h3>
          <div class="card-badge">${db.host}:${db.port}</div>
        </div>
        <div class="card-body">
          <div class="metric">
            <span class="metric-label">Status:</span>
            <span class="metric-value status-error">❌ Connection Failed</span>
          </div>
          <div class="metric">
            <span class="metric-label">Error:</span>
            <span class="metric-value status-error">${metrics.error}</span>
          </div>
        </div>
      `;
    } else {
      card.innerHTML = `
        <div class="card-header">
          <h3 class="card-title">🐘 ${db.name}</h3>
          <div class="card-badge">${db.host}:${db.port}</div>
        </div>
        <div class="card-body">
          <div class="metric">
            <span class="metric-label">💾 Size:</span>
            <span class="metric-value status-info">${stats.sizeHuman || "N/A"}</span>
          </div>
          <div class="metric">
            <span class="metric-label">🔌 Active Connections:</span>
            <span class="metric-value ${(stats.activeConnections || 0) > 50 ? "status-warning" : "status-ok"}">
              ${stats.activeConnections || connections.length || 0}
            </span>
          </div>
          <div class="metric">
            <span class="metric-label">🎯 Cache Hit Ratio:</span>
            <span class="metric-value ${(stats.cacheHitRatio || 0) < 90 ? "status-warning" : "status-ok"}">
              ${stats.cacheHitRatio || 0}%
            </span>
          </div>
          <div class="metric">
            <span class="metric-label">🏃 Running Queries:</span>
            <span class="metric-value status-info">${metrics.running_queries_count || 0}</span>
          </div>
          <div class="metric">
            <span class="metric-label">⏳ Waiting Queries:</span>
            <span class="metric-value ${(metrics.waiting_queries_count || 0) > 5 ? "status-warning" : "status-ok"}">
              ${metrics.waiting_queries_count || 0}
            </span>
          </div>
          <div class="metric">
            <span class="metric-label">🐌 Slow Queries:</span>
            <span class="metric-value ${slowQueries.length > 0 ? "status-warning" : "status-ok"}">
              ${slowQueries.length}
            </span>
          </div>
          <div class="metric">
            <span class="metric-label">🔒 Blocking Locks:</span>
            <span class="metric-value ${locks.length > 0 ? "status-error" : "status-ok"}">
              ${locks.length}
            </span>
          </div>
        </div>
      `;
    }

    return card;
  },

  updateStatsSummary() {
    const enabledDatabases = this.data.databases.filter((db) => db.enabled);
    let totalSize = 0;
    let totalConnections = 0;
    let totalCacheHit = 0;
    let dbCount = 0;

    for (const db of enabledDatabases) {
      const metrics = this.data.metrics[db.name];
      if (metrics?.database_stats && !metrics.error) {
        totalSize += metrics.database_stats.sizeBytes || 0;
        totalConnections += metrics.database_stats.activeConnections || 0;
        totalCacheHit += metrics.database_stats.cacheHitRatio || 0;
        dbCount++;
      }
    }

    const totalSizeGB = totalSize / 1024 / 1024 / 1024;
    document.getElementById("total-dbs").textContent = enabledDatabases.length;
    document.getElementById("total-size").textContent =
      `${totalSizeGB.toFixed(1)} GB`;
    document.getElementById("total-connections").textContent = totalConnections;
    document.getElementById("avg-cache-hit").textContent =
      dbCount > 0 ? `${(totalCacheHit / dbCount).toFixed(1)}%` : "0%";
  },

  async loadAlerts() {
    try {
      this.data.alerts = await HTTP.get("/api/alerts");
      this.renderAlerts();
    } catch (error) {
      console.error("Failed to load alerts:", error);
    }
  },

  renderAlerts() {
    const container = document.getElementById("alerts-container");
    if (!container) return;

    if (this.data.alerts.length === 0) {
      container.innerHTML = "";
      return;
    }

    container.innerHTML = "<h3>⚠️ Active Alerts</h3>";

    this.data.alerts.forEach((alert) => {
      const severity = alert.severity?.toLowerCase() || "low";
      const alertDiv = document.createElement("div");
      alertDiv.className = `alert alert-${severity}`;
      alertDiv.innerHTML = `
        <div class="alert-title">[${alert.database}] ${alert.type}</div>
        <div class="alert-message">${alert.message}</div>
      `;
      container.appendChild(alertDiv);
    });
  },

  async refreshData() {
    console.log("🔄 Refreshing data...");
    await this.loadAllMetrics();
    await this.loadAlerts();
    console.log("✅ Data refreshed");
  },

  startAutoRefresh() {
    if (this.data.refreshInterval) {
      clearInterval(this.data.refreshInterval);
    }

    const interval =
      (parseInt(document.body.dataset.refreshInterval) || 10) * 1000;
    console.log(`⏰ Auto-refresh every ${interval / 1000} seconds`);

    this.data.refreshInterval = setInterval(() => {
      this.refreshData();
    }, interval);
  },

  bindEvents() {
    const refreshBtn = document.getElementById("refresh-btn");
    if (refreshBtn) {
      refreshBtn.addEventListener("click", () => this.refreshData());
    }

    const themeBtn = document.getElementById("toggle-theme");
    if (themeBtn) {
      themeBtn.addEventListener("click", () => {
        document.body.classList.toggle("light-theme");
        this.data.theme = document.body.classList.contains("light-theme")
          ? "light"
          : "dark";
        this.updateChartsTheme();
      });
    }
  },

  updateTimestamp() {
    const timestamp = document.getElementById("timestamp");
    if (timestamp) {
      const now = new Date();
      timestamp.textContent = `Last updated: ${now.toLocaleString()}`;
    }
  },

  initCharts() {
    const textColor = this.data.theme === "dark" ? "#ebdbb2" : "#3c3836";
    const gridColor = this.data.theme === "dark" ? "#3c3836" : "#d5c4a1";

    // Connections chart
    const connectionsCtx = document
      .getElementById("connections-chart")
      ?.getContext("2d");
    if (connectionsCtx) {
      this.data.charts.connections = new Chart(connectionsCtx, {
        type: "line",
        data: { labels: [], datasets: [] },
        options: {
          responsive: true,
          maintainAspectRatio: true,
          animation: { duration: 500 },
          plugins: {
            legend: { position: "bottom", labels: { color: textColor } },
            tooltip: { mode: "index", intersect: false },
          },
          scales: {
            y: {
              beginAtZero: true,
              grid: { color: gridColor },
              ticks: { color: textColor, stepSize: 1 },
            },
            x: {
              ticks: {
                color: textColor,
                maxRotation: 45,
                autoSkip: true,
                maxTicksLimit: 10,
              },
            },
          },
        },
      });
    }

    // Size chart
    const sizeCtx = document.getElementById("size-chart")?.getContext("2d");
    if (sizeCtx) {
      this.data.charts.sizes = new Chart(sizeCtx, {
        type: "bar",
        data: { labels: [], datasets: [] },
        options: {
          responsive: true,
          maintainAspectRatio: true,
          plugins: {
            legend: { position: "bottom", labels: { color: textColor } },
          },
          scales: {
            y: {
              beginAtZero: true,
              title: { display: true, text: "GB", color: textColor },
              ticks: { color: textColor },
            },
            x: { ticks: { color: textColor } },
          },
        },
      });
    }

    // Cache chart
    const cacheCtx = document.getElementById("cache-chart")?.getContext("2d");
    if (cacheCtx) {
      this.data.charts.cache = new Chart(cacheCtx, {
        type: "line",
        data: { labels: [], datasets: [] },
        options: {
          responsive: true,
          maintainAspectRatio: true,
          plugins: {
            legend: { position: "bottom", labels: { color: textColor } },
          },
          scales: {
            y: {
              beginAtZero: true,
              max: 100,
              title: { display: true, text: "%", color: textColor },
              ticks: { color: textColor },
            },
            x: {
              ticks: {
                color: textColor,
                maxRotation: 45,
                autoSkip: true,
                maxTicksLimit: 10,
              },
            },
          },
        },
      });
    }

    // Queries chart
    const queriesCtx = document
      .getElementById("queries-chart")
      ?.getContext("2d");
    if (queriesCtx) {
      this.data.charts.queries = new Chart(queriesCtx, {
        type: "line",
        data: { labels: [], datasets: [] },
        options: {
          responsive: true,
          maintainAspectRatio: true,
          plugins: {
            legend: { position: "bottom", labels: { color: textColor } },
          },
          scales: {
            y: {
              beginAtZero: true,
              title: {
                display: true,
                text: "Running Queries",
                color: textColor,
              },
              ticks: { color: textColor, stepSize: 1 },
            },
            x: {
              ticks: {
                color: textColor,
                maxRotation: 45,
                autoSkip: true,
                maxTicksLimit: 10,
              },
            },
          },
        },
      });
    }

    console.log("📊 Charts initialized");
  },

  updateCharts() {
    const colors = [
      "#83a598",
      "#d3869b",
      "#b8bb26",
      "#fabd2f",
      "#8ec07c",
      "#fb4934",
    ];
    const enabledDatabases = this.data.databases.filter((db) => db.enabled);

    // Get all unique time labels from history
    let allTimes = [];
    for (const db of enabledDatabases) {
      const history = this.data.history.connections[db.name] || [];
      for (const point of history) {
        if (!allTimes.includes(point.time)) {
          allTimes.push(point.time);
        }
      }
    }
    allTimes.sort();

    // Update connections chart
    if (this.data.charts.connections && enabledDatabases.length > 0) {
      const datasets = [];
      for (let i = 0; i < enabledDatabases.length; i++) {
        const db = enabledDatabases[i];
        const history = this.data.history.connections[db.name] || [];
        const dataMap = new Map(history.map((h) => [h.time, h.value]));
        const data = allTimes.map((time) => dataMap.get(time) ?? null);

        datasets.push({
          label: db.name,
          data: data,
          borderColor: colors[i % colors.length],
          backgroundColor: colors[i % colors.length] + "20",
          tension: 0.3,
          fill: false,
          pointRadius: 3,
          pointHoverRadius: 5,
        });
      }

      this.data.charts.connections.data.labels = allTimes;
      this.data.charts.connections.data.datasets = datasets;
      this.data.charts.connections.update();
    }

    // Update cache chart
    if (this.data.charts.cache && enabledDatabases.length > 0) {
      const datasets = [];
      for (let i = 0; i < enabledDatabases.length; i++) {
        const db = enabledDatabases[i];
        const history = this.data.history.cacheHits[db.name] || [];
        const dataMap = new Map(history.map((h) => [h.time, h.value]));
        const data = allTimes.map((time) => dataMap.get(time) ?? null);

        datasets.push({
          label: db.name,
          data: data,
          borderColor: colors[i % colors.length],
          backgroundColor: "transparent",
          tension: 0.3,
          fill: false,
          pointRadius: 3,
        });
      }

      this.data.charts.cache.data.labels = allTimes;
      this.data.charts.cache.data.datasets = datasets;
      this.data.charts.cache.update();
    }

    // Update queries chart
    if (this.data.charts.queries && enabledDatabases.length > 0) {
      const datasets = [];
      for (let i = 0; i < enabledDatabases.length; i++) {
        const db = enabledDatabases[i];
        const history = this.data.history.queries[db.name] || [];
        const dataMap = new Map(history.map((h) => [h.time, h.value]));
        const data = allTimes.map((time) => dataMap.get(time) ?? null);

        datasets.push({
          label: db.name,
          data: data,
          borderColor: colors[i % colors.length],
          backgroundColor: "transparent",
          tension: 0.3,
          fill: false,
          pointRadius: 3,
        });
      }

      this.data.charts.queries.data.labels = allTimes;
      this.data.charts.queries.data.datasets = datasets;
      this.data.charts.queries.update();
    }

    // Update size chart (bar chart - current values only)
    if (this.data.charts.sizes && enabledDatabases.length > 0) {
      const labels = [];
      const data = [];
      const backgroundColors = [];

      for (let i = 0; i < enabledDatabases.length; i++) {
        const db = enabledDatabases[i];
        const history = this.data.history.sizes[db.name] || [];
        const lastValue =
          history.length > 0 ? history[history.length - 1].value : 0;

        labels.push(db.name);
        data.push(lastValue);
        backgroundColors.push(colors[i % colors.length]);
      }

      this.data.charts.sizes.data.labels = labels;
      this.data.charts.sizes.data.datasets = [
        {
          label: "Size (GB)",
          data: data,
          backgroundColor: backgroundColors,
          borderRadius: 4,
        },
      ];
      this.data.charts.sizes.update();
    }

    console.log(`📊 Charts updated with ${allTimes.length} time points`);
  },

  updateChartsTheme() {
    const textColor = this.data.theme === "dark" ? "#ebdbb2" : "#3c3836";
    const gridColor = this.data.theme === "dark" ? "#3c3836" : "#d5c4a1";

    Object.values(this.data.charts).forEach((chart) => {
      if (chart) {
        if (chart.options.plugins?.legend?.labels) {
          chart.options.plugins.legend.labels.color = textColor;
        }
        if (chart.options.scales?.y?.ticks) {
          chart.options.scales.y.ticks.color = textColor;
        }
        if (chart.options.scales?.x?.ticks) {
          chart.options.scales.x.ticks.color = textColor;
        }
        if (chart.options.scales?.y?.grid) {
          chart.options.scales.y.grid.color = gridColor;
        }
        if (chart.options.scales?.y?.title) {
          chart.options.scales.y.title.color = textColor;
        }
        chart.update();
      }
    });
  },
};

document.addEventListener("DOMContentLoaded", () => {
  App.init();
});
