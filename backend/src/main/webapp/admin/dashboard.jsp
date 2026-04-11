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
    </div><div class="mt-4 flex items-center text-sm"><span class="text-emerald-500 font-medium">+12%</span><span class="text-zinc-500 ml-2">from last month</span></div></div></div>
    <div class="card fade-in"><div class="p-6"><div class="flex justify-between items-start">
      <div><p class="text-sm font-medium text-zinc-500">Active Postings</p><p class="text-3xl font-bold text-zinc-900 mt-2" id="statPostings">-</p></div>
      <div class="stat-icon"><i data-lucide="briefcase" class="h-5 w-5"></i></div>
    </div><div class="mt-4 flex items-center text-sm"><span class="text-emerald-500 font-medium">+4</span><span class="text-zinc-500 ml-2">from last month</span></div></div></div>
    <div class="card fade-in"><div class="p-6"><div class="flex justify-between items-start">
      <div><p class="text-sm font-medium text-zinc-500">Offers Accepted</p><p class="text-3xl font-bold text-zinc-900 mt-2" id="statOffers">85%</p></div>
      <div class="stat-icon"><i data-lucide="trending-up" class="h-5 w-5"></i></div>
    </div><div class="mt-4 flex items-center text-sm"><span class="text-emerald-500 font-medium">+2%</span><span class="text-zinc-500 ml-2">from last month</span></div></div></div>
  </div>

  <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
    <div class="card p-6">
      <h3 class="text-lg font-bold text-zinc-900 mb-6">Application Trends</h3>
      <div class="h-72"><canvas id="areaChart"></canvas></div>
    </div>
    <div class="card p-6">
      <h3 class="text-lg font-bold text-zinc-900 mb-6">Applicants per Module</h3>
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
async function loadDashboard() {
  try {
    const data = await adminApi.getDashboard();
    document.getElementById('statApplicants').textContent = data.totalUsers || '-';
    document.getElementById('statPostings').textContent = data.totalJobs || '-';
    document.getElementById('alertText').textContent = (data.alerts && data.alerts.length) ? data.alerts.join('. ') : 'No active alerts.';
  } catch (e) { console.error('加载仪表盘失败', e); }
}

const areaCtx = document.getElementById('areaChart').getContext('2d');
new Chart(areaCtx, {
  type: 'line',
  data: {
    labels: ['Week 1', 'Week 2', 'Week 3', 'Week 4', 'Week 5', 'Week 6'],
    datasets: [{
      label: 'Applicants',
      data: [45, 82, 120, 250, 310, 342],
      fill: true,
      backgroundColor: 'rgba(24,24,27,0.08)',
      borderColor: '#18181b',
      borderWidth: 2,
      tension: 0.4,
      pointRadius: 4,
      pointBackgroundColor: '#18181b',
    }]
  },
  options: {
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: { grid: { display: false }, ticks: { color: '#71717a', font: { size: 12 } } },
      y: { grid: { color: '#e4e4e7', drawBorder: false }, ticks: { color: '#71717a', font: { size: 12 } } }
    }
  }
});

const barCtx = document.getElementById('barChart').getContext('2d');
new Chart(barCtx, {
  type: 'bar',
  data: {
    labels: ['CS101', 'CS201', 'CS301', 'CS405', 'CS502'],
    datasets: [{
      label: 'Applicants',
      data: [120, 85, 40, 65, 32],
      backgroundColor: '#18181b',
      borderRadius: 4,
      barThickness: 40,
    }]
  },
  options: {
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: { grid: { display: false }, ticks: { color: '#71717a', font: { size: 12 } } },
      y: { grid: { color: '#e4e4e7', drawBorder: false }, ticks: { color: '#71717a', font: { size: 12 } } }
    }
  }
});

loadDashboard();
</script>
