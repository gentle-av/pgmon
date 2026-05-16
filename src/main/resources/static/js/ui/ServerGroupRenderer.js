export class ServerGroupRenderer {
  constructor(containerId) {
    this.container = document.getElementById(containerId);
    this.openServers = new Set();
  }

  render(serversMap, metricsMap) {
    if (!this.container) return;
    if (Object.keys(serversMap).length === 0) {
      this.container.innerHTML =
        '<div class="card">No servers configured</div>';
      return;
    }
    this.container.innerHTML = "";
    const sortedServers = Object.keys(serversMap).sort();
    for (const serverKey of sortedServers) {
      const server = serversMap[serverKey];
      const metrics = metricsMap[serverKey] || {};
      const spoilerDiv = this._createServerSpoiler(server, metrics, serverKey);
      this.container.appendChild(spoilerDiv);
    }
  }

  _createServerSpoiler(server, metrics, serverKey) {
    const container = document.createElement("div");
    container.className = "server-spoiler";
    container.dataset.serverKey = serverKey;
    const header = document.createElement("div");
    header.className = "spoiler-header";
    header.onclick = (e) => {
      if (e.target.tagName !== "BUTTON") {
        this._toggleSpoiler(container, serverKey);
      }
    };
    const serverInfo = document.createElement("div");
    serverInfo.className = "server-info";
    const isOpen = this.openServers.has(serverKey);
    serverInfo.innerHTML = `
      <span class="spoiler-icon">${isOpen ? "▼" : "▶"}</span>
      <span class="server-name">🖥️ ${this._escapeHtml(server.name)}</span>
      <span class="server-badge">${this._escapeHtml(server.host)}:${server.port}</span>
      <span class="server-stats-badge">
        📊 ${metrics.totalDbs || 0} DBs |
        🔌 ${metrics.totalConnections || 0} conns |
        💾 ${metrics.totalSizeGB || 0} GB
      </span>
    `;
    header.appendChild(serverInfo);
    const actions = document.createElement("div");
    actions.className = "server-actions";
    actions.innerHTML = `<button class="btn btn-sm btn-outline refresh-server" data-server="${serverKey}">🔄</button>`;
    header.appendChild(actions);
    container.appendChild(header);
    const content = document.createElement("div");
    content.className = `spoiler-content ${isOpen ? "open" : ""}`;
    if (isOpen) {
      this._populateContent(content, server, metrics, serverKey);
    } else {
      content.innerHTML =
        '<div class="spoiler-placeholder">▼ Click to expand server details</div>';
    }
    container.appendChild(content);
    return container;
  }

  _populateContent(content, server, metrics, serverKey) {
    const dbs = server.databases || [];
    const safeKey = serverKey.replace(/[^a-zA-Z0-9]/g, "-");
    content.innerHTML = `
      <div class="server-charts">
        <div class="chart-container">
          <h4>📈 Active Sessions History - ${this._escapeHtml(server.name)}</h4>
          <div id="ash-chart-${safeKey}" style="width: 100%; height: 350px;"></div>
        </div>
        <div class="charts-mini-grid">
          <div class="chart-container">
            <h4>💾 Database Size (GB)</h4>
            <canvas id="size-chart-${safeKey}"></canvas>
          </div>
          <div class="chart-container">
            <h4>🎯 Cache Hit Ratio</h4>
            <canvas id="cache-chart-${safeKey}"></canvas>
          </div>
          <div class="chart-container">
            <h4>🐌 Running Queries</h4>
            <canvas id="queries-chart-${safeKey}"></canvas>
          </div>
        </div>
      </div>
      <div class="server-databases">
        <h4>🗄️ Databases on ${this._escapeHtml(server.name)}</h4>
        <div class="card-grid" id="dbs-grid-${safeKey}">
          ${this._renderDatabasesGrid(dbs, metrics.databases || {})}
        </div>
      </div>
    `;
  }

  _renderDatabasesGrid(databases, dbMetricsMap) {
    if (!databases || databases.length === 0) {
      return '<div class="card">No databases found</div>';
    }
    let html = "";
    for (const db of databases) {
      const metrics = dbMetricsMap[db.name] || {};
      const stats = metrics.database_stats || {};
      const connWarning = (stats.activeConnections || 0) > 50;
      html += `
        <div class="card database-card">
          <div class="card-header">
            <h3 class="card-title">🐘 ${this._escapeHtml(db.name)}</h3>
            <div class="card-badge">${this._escapeHtml(db.host)}:${db.port}</div>
          </div>
          <div class="card-body">
            <div class="metric"><span class="metric-label">💾 Size:</span><span class="metric-value status-info">${stats.sizeHuman || "N/A"}</span></div>
            <div class="metric"><span class="metric-label">🔌 Active Connections:</span><span class="metric-value ${connWarning ? "status-warning" : "status-ok"}">${stats.activeConnections || 0}</span></div>
            <div class="metric"><span class="metric-label">📊 Cache Hit Ratio:</span><span class="metric-value status-info">${stats.cacheHitRatio || 0}%</span></div>
            <div class="metric"><span class="metric-label">🏃 Running Queries:</span><span class="metric-value">${metrics.running_queries_count || 0}</span></div>
            <div class="metric"><span class="metric-label">🔒 Locks:</span><span class="metric-value">${metrics.total_locks || 0}</span></div>
          </div>
        </div>
      `;
    }
    return html;
  }

  _toggleSpoiler(container, serverKey) {
    const content = container.querySelector(".spoiler-content");
    const icon = container.querySelector(".spoiler-icon");
    if (this.openServers.has(serverKey)) {
      this.openServers.delete(serverKey);
      content.classList.remove("open");
      icon.textContent = "▶";
      content.innerHTML =
        '<div class="spoiler-placeholder">▼ Click to expand server details</div>';
    } else {
      this.openServers.add(serverKey);
      content.classList.add("open");
      icon.textContent = "▼";
      const server = this.serversMap?.[serverKey];
      const metrics = this.metricsMap?.[serverKey];
      if (server) {
        this._populateContent(content, server, metrics || {}, serverKey);
      }
    }
  }

  setServersMap(map) {
    this.serversMap = map;
  }

  setMetricsMap(map) {
    this.metricsMap = map;
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
