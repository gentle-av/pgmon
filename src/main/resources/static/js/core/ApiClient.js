export class ApiClient {
  async getDatabases() {
    if (window.INITIAL_DATABASES && window.INITIAL_DATABASES.length > 0) {
      return window.INITIAL_DATABASES;
    }
    const response = await fetch("/api/databases");
    if (!response.ok) throw new Error("Failed to load databases");
    return response.json();
  }

  async getMetricsForServer(host, port) {
    const response = await fetch(`/api/metrics/all/${host}/${port}`);
    if (!response.ok)
      throw new Error(`Failed to load metrics for ${host}:${port}`);
    return response.json();
  }

  async getAlerts() {
    const response = await fetch("/api/alerts");
    if (!response.ok) throw new Error("Failed to load alerts");
    return response.json();
  }
}
