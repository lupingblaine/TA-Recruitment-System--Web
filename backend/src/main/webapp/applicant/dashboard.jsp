<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/auth-check.jsp" %>
<jsp:include page="/common/layout-top.jsp"/>

<div class="space-y-8" id="page">
  <div>
    <h1 class="text-3xl font-bold tracking-tight text-zinc-900" id="greeting">Welcome back!</h1>
    <p class="text-zinc-500 mt-2 text-lg">Here's what's happening with your TA applications.</p>
  </div>

  <div class="grid grid-cols-1 md:grid-cols-3 gap-6" id="statsGrid">
    <div class="card fade-in">
      <div class="p-6 flex items-center gap-4">
        <div class="stat-icon"><i data-lucide="clock" class="h-6 w-6"></i></div>
        <div>
          <p class="text-sm font-medium text-zinc-500">Active Applications</p>
          <p class="text-2xl font-bold text-zinc-900" id="statActive">-</p>
        </div>
      </div>
    </div>
    <div class="card fade-in">
      <div class="p-6 flex items-center gap-4">
        <div class="stat-icon"><i data-lucide="calendar" class="h-6 w-6"></i></div>
        <div>
          <p class="text-sm font-medium text-zinc-500">Interviews Scheduled</p>
          <p class="text-2xl font-bold text-zinc-900" id="statInterviews">-</p>
        </div>
      </div>
    </div>
    <div class="card fade-in">
      <div class="p-6 flex items-center gap-4">
        <div class="stat-icon"><i data-lucide="check-circle-2" class="h-6 w-6"></i></div>
        <div>
          <p class="text-sm font-medium text-zinc-500">Profile Completion</p>
          <p class="text-2xl font-bold text-zinc-900" id="statProfile">-</p>
        </div>
      </div>
    </div>
  </div>

  <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
    <div class="lg:col-span-2 space-y-6">
      <div class="flex items-center justify-between">
        <h2 class="text-xl font-semibold text-zinc-900">Recent Applications</h2>
        <a href="<%= request.getContextPath() %>/applicant/applications.jsp" class="btn btn-ghost text-zinc-500 hover:text-zinc-900">View all</a>
      </div>
      <div class="card">
        <div class="divide-y divide-zinc-100" id="recentApps">
          <div class="p-6 text-center text-zinc-400">Loading...</div>
        </div>
      </div>
    </div>

    <div class="space-y-6">
      <h2 class="text-xl font-semibold text-zinc-900">Your Workload Tracker</h2>
      <div class="card bg-gradient-to-br from-indigo-50 to-purple-50 border-indigo-100">
        <div class="p-6">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-2">
              <i data-lucide="trending-up" class="h-5 w-5 text-indigo-600"></i>
              <span class="font-semibold text-indigo-900">Fall 2026 Term</span>
            </div>
            <span class="badge bg-white/50 text-indigo-700 border-indigo-200">Healthy Load</span>
          </div>
          <div class="space-y-2 mb-4">
            <div class="flex justify-between text-sm">
              <span class="text-indigo-800/70">Estimated Commitment</span>
              <span class="font-bold text-indigo-900" id="workloadHrs">12 / 20 hrs</span>
            </div>
            <div class="h-2 w-full bg-indigo-200/50 rounded-full overflow-hidden">
              <div class="h-full bg-indigo-500 rounded-full transition-all duration-1000" id="workloadBar" style="width:60%"></div>
            </div>
          </div>
          <div class="bg-white/60 rounded-lg p-3 text-sm flex gap-3 text-indigo-900">
            <i data-lucide="info" class="h-5 w-5 shrink-0 text-indigo-500"></i>
            <p>You can safely take on <strong>1 more standard TA module</strong> (approx 8 hrs/week) before reaching the recommended maximum.</p>
          </div>
        </div>
      </div>

      <h2 class="text-xl font-semibold text-zinc-900 mt-8">Upcoming Deadlines</h2>
      <div class="card">
        <div class="p-6 space-y-6" id="deadlines">
          <div class="text-center text-zinc-400 text-sm">Loading...</div>
        </div>
      </div>
    </div>
  </div>
</div>

<jsp:include page="/common/layout-bottom.jsp"/>
<script>
const ctx = document.querySelector('meta[name="ctx"]').content;

async function loadDashboard() {
  try {
    const data = await applicantApi.getDashboard();
    const apps = data.applications || [];
    const jobs = data.activeJobs || [];

    document.getElementById('statActive').textContent = apps.filter(a => !['rejected','hired'].includes(a.status)).length;
    document.getElementById('statInterviews').textContent = apps.filter(a => a.status === 'interview').length;
    document.getElementById('statProfile').textContent = data.profile ? '85%' : '0%';
    document.getElementById('greeting').textContent = 'Welcome back, ' + (data.profile?.name || '<%= session.getAttribute("userName") %>') + '!';

    const container = document.getElementById('recentApps');
    if (apps.length === 0) {
      container.innerHTML = '<div class="p-6 text-center text-zinc-400">No applications yet. <a href="' + ctx + '/applicant/jobs.jsp" class="text-zinc-900 font-medium hover:underline">Browse jobs</a></div>';
    } else {
      container.innerHTML = apps.slice(0, 5).map(app => {
        const badgeClass = app.status === 'interview' ? 'badge-warning' : app.status === 'rejected' ? 'badge-danger' : app.status === 'hired' ? 'badge-success' : 'badge-neutral';
        return '<a href="' + ctx + '/applicant/applications.jsp" class="p-6 flex items-center justify-between hover:bg-zinc-50/50 transition-colors cursor-pointer group block">' +
          '<div class="space-y-1"><h3 class="font-semibold text-zinc-900 group-hover:text-blue-600 transition-colors">' + (app.jobTitle || app.jobId) + '</h3>' +
          '<p class="text-sm text-zinc-500">' + (app.moduleCode || '') + ' • Applied ' + (app.appliedDate || '') + '</p></div>' +
          '<div class="flex items-center gap-4"><span class="badge ' + badgeClass + '">' + app.status + '</span>' +
          '<i data-lucide="chevron-right" class="h-5 w-5 text-zinc-300 group-hover:text-zinc-500 transition-colors"></i></div></a>';
      }).join('');
      lucide.createIcons();
    }

    const dl = document.getElementById('deadlines');
    if (jobs.length === 0) {
      dl.innerHTML = '<div class="text-center text-zinc-400 text-sm">No upcoming deadlines</div>';
    } else {
      dl.innerHTML = jobs.slice(0, 3).map(j =>
        '<div class="flex gap-4"><div class="mt-0.5 h-2 w-2 rounded-full bg-blue-500 shrink-0"></div><div>' +
        '<p class="font-medium text-zinc-900 text-sm">' + (j.moduleCode || '') + ' ' + j.title + '</p>' +
        '<p class="text-sm text-zinc-500 mt-1">Closes ' + (j.deadline || 'TBD') + '</p></div></div>'
      ).join('') + '<a href="' + ctx + '/applicant/jobs.jsp" class="btn btn-outline w-full mt-4">Browse more jobs</a>';
    }
  } catch (e) {
    console.error('加载仪表盘失败', e);
  }
}
loadDashboard();
</script>
