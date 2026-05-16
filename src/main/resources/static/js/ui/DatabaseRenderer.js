export class DatabaseRenderer {
  constructor(containerId) {
    this.container = document.getElementById(containerId);
  }

  render(databases, metricsMap) {
    if (!this.container) return;
    this.container.innerHTML = "";
    databases.forEach((db) => {
      const metrics = metricsMap[db.name] || {};
      const card = this._createCard(db, metrics);
      this.container.appendChild(card);
    });
  }

  _createCard(db, metrics) {
    const card = document.createElement("div");
    card.className = "card database-card";
    if (metrics.error) {
      card.innerHTML = this._getErrorHtml(db, metrics);
    } else {
      card.innerHTML = this._getMetricHtml(db, metrics);
    }
    return card;
  }

  _getErrorHtml(db, metrics) {
    return `
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
            </div>`;
  }

  _getMetricHtml(db, metrics) {
    const stats = metrics.database_stats || {};
    const getStatusClass = (condition, warnClass, okClass) =>
      condition ? warnClass : okClass;
    return `
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
                    <span class="metric-value ${getStatusClass((stats.activeConnections || 0) > 50, "status-warning", "status-ok")}">
                        ${stats.activeConnections || 0}
                    </span>
                </div>
                <!-- Add other metrics similarly -->
            </div>`;
  }
}
