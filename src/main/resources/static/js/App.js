import { ApiClient } from "./core/ApiClient.js";
import { AppState } from "./core/AppState.js";
import { ServerGroupRenderer } from "./ui/ServerGroupRenderer.js";

export class App {
  constructor() {
    this.api = new ApiClient();
    this.state = new AppState();
    this.serverRenderer = new ServerGroupRenderer("servers-container");
    this.refreshInterval = null;
  }

  async init() {
    console.log("🚀 Initializing PostgreSQL Monitor...");
    try {
      const databases = await this.api.getDatabases();
      console.log("Frontend received databases:", databases);
      const servers = this._groupByServer(databases);
      this.state.setServers(servers);
      const enabledServers = this.state.getEnabledServers();
      if (enabledServers.length === 0) {
        this.showError("No enabled servers configured");
        return;
      }
      this.bindEvents();
      await this.refreshData();
      this.startAutoRefresh();
    } catch (error) {
      console.error("❌ Initialization failed:", error);
      this.showError(error.message);
    }
  }

  _groupByServer(databases) {
    const serverMap = {};
    for (const db of databases) {
      const key = `${db.host}:${db.port}`;
      if (!serverMap[key]) {
        serverMap[key] = {
          key: key,
          name: `${db.host}:${db.port}`,
          host: db.host,
          port: db.port,
          enabled: db.enabled !== false,
          databases: [],
        };
      }
      serverMap[key].databases.push(db);
    }
    return Object.values(serverMap);
  }

  _buildServersMap(servers) {
    const map = {};
    for (const server of servers) {
      const key = `${server.host}:${server.port}`;
      map[key] = server;
    }
    return map;
  }

  _buildMetricsMap() {
    const map = {};
    const servers = this.state.getEnabledServers();
    for (const server of servers) {
      const key = `${server.host}:${server.port}`;
      const serverMetrics = this.state.getServerAggregatedMetrics(key);
      map[key] = serverMetrics;
    }
    return map;
  }

  async refreshData() {
    const enabledServers = this.state.getEnabledServers();
    for (const server of enabledServers) {
      try {
        const metrics = await this.api.getMetricsForServer(
          server.host,
          server.port,
        );
        this.state.updateServerMetrics(server, metrics);
      } catch (e) {
        console.error(
          `Failed to update metrics for ${server.host}:${server.port}`,
          e,
        );
      }
    }
    const serversMap = this._buildServersMap(enabledServers);
    const metricsMap = this._buildMetricsMap();
    this.serverRenderer.setServersMap(serversMap);
    this.serverRenderer.setMetricsMap(metricsMap);
    this.serverRenderer.render(serversMap, metricsMap);
    this.updateStatsSummary();
    this.updateTimestamp();
    try {
      const alerts = await this.api.getAlerts();
      this.state.setAlerts(alerts);
      this.renderAlerts();
    } catch (e) {
      console.error("Failed to load alerts", e);
    }
  }

  startAutoRefresh() {
    if (this.refreshInterval) clearInterval(this.refreshInterval);
    const intervalSeconds =
      parseInt(document.body.dataset.refreshInterval) || 10;
    const intervalMs = intervalSeconds * 1000;
    console.log(`⏰ Auto-refresh set to ${intervalSeconds}s`);
    this.refreshInterval = setInterval(() => this.refreshData(), intervalMs);
  }

  bindEvents() {
    const refreshBtn = document.getElementById("refresh-btn");
    if (refreshBtn) {
      refreshBtn.addEventListener("click", () => this.refreshData());
    }
    document.addEventListener("click", (e) => {
      if (e.target.classList && e.target.classList.contains("refresh-server")) {
        const serverKey = e.target.dataset.server;
        if (serverKey) this.refreshSingleServer(serverKey);
      }
    });
  }

  async refreshSingleServer(serverKey) {
    const [host, port] = serverKey.split(":");
    const server = this.state
      .getEnabledServers()
      .find((s) => s.host === host && s.port === parseInt(port));
    if (server) {
      try {
        const metrics = await this.api.getMetricsForServer(
          host,
          parseInt(port),
        );
        this.state.updateServerMetrics(server, metrics);
        const serversMap = this._buildServersMap(
          this.state.getEnabledServers(),
        );
        const metricsMap = this._buildMetricsMap();
        this.serverRenderer.render(serversMap, metricsMap);
      } catch (e) {
        console.error(`Failed to refresh server ${serverKey}`, e);
      }
    }
  }

  updateStatsSummary() {
    const stats = this.state.getGlobalSummaryStats();
    const elTotalServers = document.getElementById("total-servers");
    const elTotalDbs = document.getElementById("total-dbs");
    const elTotalSize = document.getElementById("total-size");
    const elTotalConns = document.getElementById("total-connections");
    const elAvgCache = document.getElementById("avg-cache-hit");
    if (elTotalServers) elTotalServers.textContent = stats.totalServers;
    if (elTotalDbs) elTotalDbs.textContent = stats.totalDbs;
    if (elTotalSize) elTotalSize.textContent = `${stats.totalSizeGB} GB`;
    if (elTotalConns) elTotalConns.textContent = stats.totalConnections;
    if (elAvgCache) elAvgCache.textContent = stats.avgCacheHit;
  }

  renderAlerts() {
    const container = document.getElementById("alerts-container");
    if (!container) return;
    const alerts = this.state.alerts || [];
    if (alerts.length === 0) {
      container.innerHTML = "";
      return;
    }
    let html = "<h3>⚠️ Active Alerts</h3>";
    for (const alert of alerts) {
      const severity = alert.severity ? alert.severity.toLowerCase() : "low";
      html += `
        <div class="alert alert-${severity}">
          <div class="alert-title">[${this._escapeHtml(alert.database)}] ${this._escapeHtml(alert.type)}</div>
          <div class="alert-message">${this._escapeHtml(alert.message)}</div>
        </div>
      `;
    }
    container.innerHTML = html;
  }

  updateTimestamp() {
    const timestampEl = document.getElementById("timestamp");
    if (timestampEl) {
      timestampEl.textContent = `Last updated: ${new Date().toLocaleString()}`;
    }
  }

  showError(message) {
    const container = document.getElementById("servers-container");
    if (container) {
      container.innerHTML = `<div class="card" style="text-align: center; border-left-color: var(--danger);"><h3 style="color: var(--danger);">⚠️ Error</h3><p>${this._escapeHtml(message)}</p></div>`;
    }
    console.error(message);
  }

  _escapeHtml(text) {
    if (!text) return "";
    return text
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }
}
