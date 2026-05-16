export class ChartManager {
  constructor(themeManager) {
    this.charts = {};
    this.themeManager = themeManager;
  }
  initAshChart(domElement) {
    if (!domElement || this.charts.ash) return;
    this.charts.ash = echarts.init(domElement);
    this.charts.ash.setOption({
      title: { text: "Active Session History (ASH)" },
      tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
      legend: { data: [], left: "left" },
      grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
      xAxis: { type: "category", boundaryGap: false },
      yAxis: { type: "value", name: "Active Sessions" },
    });
  }

  updateAshChart(data) {
    if (!this.charts.ash) return;
    this.charts.ash.setOption({
      legend: { data: data.series.map((s) => s.name) },
      xAxis: { data: data.times },
      series: data.series,
    });
  }

  updateAllThemes() {
    const textColor = this.themeManager.getTextColor();
    Object.values(this.charts).forEach((chart) => {
      if (chart) {
        chart.setOption({
          textStyle: { color: textColor },
          xAxis: { axisLabel: { color: textColor } },
          yAxis: { axisLabel: { color: textColor } },
        });
      }
    });
  }
}
