<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/auth-check.jsp" %>
<jsp:include page="/common/layout-top.jsp"/>

<div class="space-y-8">
  <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
    <div>
      <h1 class="text-3xl font-bold tracking-tight text-zinc-900">TA Positions</h1>
      <p class="text-zinc-500 mt-2">Find and apply for teaching assistant roles.</p>
    </div>
    <div class="flex items-center gap-3">
      <div class="relative w-full md:w-64">
        <i data-lucide="search" class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400"></i>
        <input id="searchInput" type="text" placeholder="Search roles, modules..." class="input pl-9"
               onkeyup="debounceSearch()">
      </div>
      <select id="statusFilter" class="input w-auto" onchange="loadJobs()">
        <option value="">All Status</option>
        <option value="active">Active</option>
        <option value="closed">Closed</option>
      </select>
    </div>
  </div>

  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6" id="jobsGrid">
    <div class="col-span-full text-center text-zinc-400 py-12">Loading...</div>
  </div>
</div>

<jsp:include page="/common/layout-bottom.jsp"/>
<script>
const ctx = document.querySelector('meta[name="ctx"]').content;
let searchTimer;

function debounceSearch() {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(loadJobs, 300);
}

async function loadJobs() {
  try {
    const params = {};
    const kw = document.getElementById('searchInput').value.trim();
    const st = document.getElementById('statusFilter').value;
    if (kw) params.keyword = kw;
    if (st) params.status = st;

    const data = await applicantApi.getJobs(Object.keys(params).length ? params : undefined);
    const jobs = data.jobs || data || [];
    const grid = document.getElementById('jobsGrid');

    if (jobs.length === 0) {
      grid.innerHTML = '<div class="col-span-full text-center text-zinc-400 py-12">No positions found.</div>';
      return;
    }

    grid.innerHTML = jobs.map((job, i) => {
      const tags = (job.requirements || []).slice(0, 2);
      return '<div class="card flex flex-col h-full hover:shadow-md transition-shadow fade-in">' +
        '<div class="p-6 flex flex-col h-full">' +
          '<div class="flex justify-between items-start mb-4">' +
            '<span class="badge badge-neutral">' + (job.moduleCode || '') + '</span>' +
            '<span class="badge ' + (job.status === 'active' ? 'badge-success' : 'badge-danger') + '">' + job.status + '</span>' +
          '</div>' +
          '<h3 class="text-lg font-bold text-zinc-900 mb-2 line-clamp-2">' + job.title + '</h3>' +
          '<p class="text-sm text-zinc-600 mb-4">' + (job.department || '') + '</p>' +
          '<div class="flex items-center gap-4 text-sm text-zinc-500 mb-6">' +
            '<div class="flex items-center gap-1.5"><i data-lucide="clock" class="h-4 w-4"></i>' + (job.hoursPerWeek || '-') + ' hrs/week</div>' +
            '<div class="flex items-center gap-1.5"><i data-lucide="briefcase" class="h-4 w-4"></i>' + (job.type || 'Part-time') + '</div>' +
          '</div>' +
          (tags.length > 0 ? '<div class="flex flex-wrap gap-2 mb-6">' + tags.map(t => '<span class="badge badge-outline bg-zinc-50 text-zinc-600">' + t + '</span>').join('') + '</div>' : '') +
          '<div class="mt-auto pt-4 border-t border-zinc-100">' +
            '<a href="' + ctx + '/applicant/job-detail.jsp?id=' + job.id + '" class="btn btn-secondary w-full justify-between">View Details <span class="text-zinc-400">&rarr;</span></a>' +
          '</div>' +
        '</div></div>';
    }).join('');
    lucide.createIcons();
  } catch (e) {
    console.error('加载职位列表失败', e);
    document.getElementById('jobsGrid').innerHTML = '<div class="col-span-full text-center text-red-500 py-12">Failed to load jobs</div>';
  }
}
loadJobs();
</script>
