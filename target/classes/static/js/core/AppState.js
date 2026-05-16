export class AppState {
  constructor() {
    this.servers = [];
    this.serverMetrics = {};
    this.alerts = [];
  }

  setServers(servers) {
    this.servers = servers;
  }

  getEnabledServers() {
    return this.servers.filter((server) => server.enabled !== false);
  }

  updateServerMetrics(server, metrics) {
    const key = `${server.host}:${server.port}`;
    this.serverMetrics[key] = metrics;
  }

  getServerAggregatedMetrics(serverKey) {
    const metrics = this.serverMetrics[serverKey] || {};
    const databases = metrics.databases || {};
    let totalDbs = 0;
    let totalConnections = 0;
    let totalSizeGB = 0;
    let totalCacheHit = 0;
    let dbCount = 0;
    for (const dbName in databases) {
      const dbMetrics = databases[dbName];
      const stats = dbMetrics.database_stats || {};
      totalDbs++;
      totalConnections += stats.activeConnections || 0;
      totalSizeGB += (stats.sizeBytes || 0) / 1024 / 1024 / 1024;
      if (stats.cacheHitRatio) {
        totalCacheHit += stats.cacheHitRatio;
        dbCount++;
      }
    }
    return {
      totalDbs: totalDbs,
      totalConnections: totalConnections,
      totalSizeGB: totalSizeGB.toFixed(2),
      avgCacheHit:
        dbCount > 0 ? (totalCacheHit / dbCount).toFixed(1) + "%" : "0%",
      databases: databases,
    };
  }

  getGlobalSummaryStats() {
    let totalServers = this.getEnabledServers().length;
    let totalDbs = 0;
    let totalConnections = 0;
    let totalSizeGB = 0;
    let totalCacheHit = 0;
    let cacheCount = 0;
    for (const key in this.serverMetrics) {
      const agg = this.getServerAggregatedMetrics(key);
      totalDbs += agg.totalDbs;
      totalConnections += agg.totalConnections;
      totalSizeGB += parseFloat(agg.totalSizeGB) || 0;
      const cacheVal = parseFloat(agg.avgCacheHit);
      if (!isNaN(cacheVal)) {
        totalCacheHit += cacheVal;
        cacheCount++;
      }
    }
    return {
      totalServers: totalServers,
      totalDbs: totalDbs,
      totalConnections: totalConnections,
      totalSizeGB: totalSizeGB.toFixed(2),
      avgCacheHit:
        cacheCount > 0 ? (totalCacheHit / cacheCount).toFixed(1) + "%" : "0%",
    };
  }

  setAlerts(alerts) {
    this.alerts = alerts;
  }
}
