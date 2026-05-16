export class ApiClient {
  async getDatabases() {
    if (
      window.__INITIAL_DATABASES__ &&
      window.__INITIAL_DATABASES__.length > 0
    ) {
      return window.__INITIAL_DATABASES__;
    }
    const response = await fetch("/api/config/databases");
    if (!response.ok) throw new Error("Failed to load databases");
    return response.json();
  }

  async getMetricsForDatabase(dbName) {
    const response = await fetch(`/api/metrics/all/${dbName}`);
    if (!response.ok) throw new Error(`Failed to load metrics for ${dbName}`);
    return response.json();
  }

  async getMetricsForServer(host, port) {
    const databases = await this.getDatabases();
    const dbForServer = databases.find(
      (db) => db.host === host && db.port === port,
    );
    if (!dbForServer) {
      throw new Error(`No database found for ${host}:${port}`);
    }
    return this.getMetricsForDatabase(dbForServer.name);
  }

  async getAlerts() {
    const response = await fetch("/api/alerts");
    if (!response.ok) throw new Error("Failed to load alerts");
    return response.json();
  }
}
