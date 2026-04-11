<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/auth-check.jsp" %>
<jsp:include page="/common/layout-top.jsp"/>
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

<div class="space-y-8 max-w-6xl mx-auto">
  <div>
    <h1 class="text-3xl font-bold tracking-tight text-zinc-900">Platform Analytics</h1>
    <p class="text-zinc-500 mt-2">Overview of recruitment metrics and system status.</p>
  </div>

  <div class="grid grid-cols-1 md:grid-cols-3 gap-6" id="statsGrid">
    <div class="card fade-in"><div class="p-6"><div class="flex justify-between items-start">
      <div><p class="text-sm font-medium text-zinc-500">Total Applicants</p><p class="text-3xl font-bold text-zinc-900 mt-2" id="statApplicants">-</p></div>
      <div class="stat-icon"><i data-lucide="users" class="h-5 w-5"></i></div>
    </div><p class="mt-4 text-xs text-zinc-400">Users with role &quot;applicant&quot; in <code class="text-zinc-500">data.json</code></p></div></div>
    <div class="card fade-in"><div class="p-6"><div class="flex justify-between items-start">
      <div><p class="text-sm font-medium text-zinc-500">Active Postings</p><p class="text-3xl font-bold text-zinc-900 mt-2" id="statPostings">-</p></div>
      <div class="stat-icon"><i data-lucide="briefcase" class="h-5 w-5"></i></div>
    </div><p class="mt-4 text-xs text-zinc-400">Jobs with status &quot;active&quot;</p></div></div>
    <div class="card fade-in"><div class="p-6"><div class="flex justify-between items-start">
      <div><p class="text-sm font-medium text-zinc-500">Offer rate (apps)</p><p class="text-3xl font-bold text-zinc-900 mt-2" id="statOffers">-</p></div>
      <div class="stat-icon"><i data-lucide="trending-up" class="h-5 w-5"></i></div>
    </div><p class="mt-4 text-xs text-zinc-400">Applications with status &quot;offer&quot; &divide; total applications</p></div></div>
  </div>

  <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
    <div class="card p-6">
      <h3 class="text-lg font-bold text-zinc-900">Application volume by week</h3>
      <p class="text-sm text-zinc-500 mt-1 mb-6">New applications per week by <span class="font-medium text-zinc-600">appliedAt</span> (last 4 weeks, Monday-based weeks, anchored to the latest submission).</p>
      <div class="h-72"><canvas id="areaChart"></canvas></div>
    </div>
    <div class="card p-6">
      <h3 class="text-lg font-bold text-zinc-900">Applications per job</h3>
      <p class="text-sm text-zinc-500 mt-1 mb-6">Count of applications linked to each job (<code class="text-zinc-500">jobId</code>), labelled by module code.</p>
      <div class="h-72"><canvas id="barChart"></canvas></div>
    </div>
  </div>

  <div class="card p-6 bg-red-50/50 border-red-100">
    <div class="flex items-start gap-4">
      <div class="h-10 w-10 rounded-full bg-red-100 flex items-center justify-center shrink-0">
        <i data-lucide="alert-triangle" class="h-5 w-5 text-red-600"></i>
      </div>
      <div>
        <h3 class="text-base font-semibold text-red-900">System Alerts</h3>
        <p class="text-sm text-red-700/80 mt-1" id="alertText">Checking system status...</p>
      </div>
    </div>
  </div>
</div>

<jsp:include page="/common/layout-bottom.jsp"/>
<script>
const chartOpts = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: {
    x: { grid: { display: false }, ticks: { color: '#71717a', font: { size: 12 } } },
    y: { grid: { color: '#e4e4e7', drawBorder: false }, ticks: { color: '#71717a', font: { size: 12 } } }
  }
};

const areaCtx = document.getElementById('areaChart').getContext('2d');
const areaChart = new Chart(areaCtx, {
  type: 'line',
  data: {
    labels: [],
    datasets: [{
      label: 'New applications',
      data: [],
      fill: true,
      backgroundColor: 'rgba(24,24,27,0.08)',
      borderColor: '#18181b',
      borderWidth: 2,
      tension: 0.4,
      pointRadius: 4,
      pointBackgroundColor: '#18181b',
    }]
  },
  options: chartOpts
});

const barCtx = document.getElementById('barChart').getContext('2d');
const barChart = new Chart(barCtx, {
  type: 'bar',
  data: {
    labels: [],
    datasets: [{
      label: 'Applications',
      data: [],
      backgroundColor: '#18181b',
      borderRadius: 4,
      barThickness: 40,
    }]
  },
  options: chartOpts
});

function formatSystemAlerts(alerts) {
  if (!alerts || !alerts.length) return 'No active alerts.';
  return alerts.map(a => {
    const mod = a.module != null ? a.module : '';
    const n = a.unfilled != null ? a.unfilled : 0;
    return mod + ': ' + n + ' unfilled position' + (n === 1 ? '' : 's');
  }).join('. ');
}

function updateChartsFromDashboard(data) {
  const trend = data.trendData || [];
  if (trend.length) {
    areaChart.data.labels = trend.map(p => p.name || '');
    areaChart.data.datasets[0].data = trend.map(p => Number(p.applicants) || 0);
  } else {
    areaChart.data.labels = ['—'];
    areaChart.data.datasets[0].data = [0];
  }
  areaChart.update();

  const mods = data.moduleStats || [];
  if (mods.length) {
    barChart.data.labels = mods.map(m => m.name || '');
    barChart.data.datasets[0].data = mods.map(m => Number(m.applicants) || 0);
  } else {
    barChart.data.labels = ['—'];
    barChart.data.datasets[0].data = [0];
  }
  barChart.update();
}

async function loadDashboard() {
  try {
    const data = await adminApi.getDashboard();
    document.getElementById('statApplicants').textContent =
      data.totalApplicants != null ? String(data.totalApplicants) : '-';
    document.getElementById('statPostings').textContent =
      data.activePostings != null ? String(data.activePostings) : '-';
    document.getElementById('statOffers').textContent =
      data.acceptanceRate != null ? String(data.acceptanceRate) : '-';
    document.getElementById('alertText').textContent = formatSystemAlerts(data.systemAlerts);
    updateChartsFromDashboard(data);
  } catch (e) {
    console.error('加载仪表盘失败', e);
    document.getElementById('alertText').textContent = 'Failed to load dashboard. Please refresh.';
  }
}

loadDashboard();
</script>
