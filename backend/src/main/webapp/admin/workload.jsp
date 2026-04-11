<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/auth-check.jsp" %>
<jsp:include page="/common/layout-top.jsp"/>

<div class="space-y-8 max-w-6xl mx-auto">
  <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
    <div>
      <h1 class="text-3xl font-bold tracking-tight text-zinc-900 flex items-center gap-2">
        Workload Intelligence
        <span class="badge bg-gradient-to-r from-indigo-500 to-purple-500 text-white border-0 text-xs px-2 py-1">AI Assisted</span>
      </h1>
      <p class="text-zinc-500 mt-2">Monitor TA workload distribution and prevent burnout or unfair allocation.</p>
    </div>
  </div>

  <div class="grid grid-cols-1 md:grid-cols-4 gap-4" id="summaryCards">
    <div class="card p-4 bg-gradient-to-br from-zinc-50 to-white border-zinc-200">
      <p class="text-sm font-medium text-zinc-500 mb-1">Total Active TAs</p>
      <p class="text-2xl font-bold text-zinc-900" id="statTotal">-</p>
    </div>
    <div class="card p-4 bg-gradient-to-br from-red-50 to-white border-red-100">
      <p class="text-sm font-medium text-red-600 mb-1">Overloaded TAs</p>
      <div class="flex items-center justify-between">
        <p class="text-2xl font-bold text-red-700" id="statOverloaded">-</p>
        <span class="badge bg-red-100 text-red-700 border-0 text-xs">Requires Action</span>
      </div>
    </div>
    <div class="card p-4 bg-gradient-to-br from-emerald-50 to-white border-emerald-100">
      <p class="text-sm font-medium text-emerald-600 mb-1">Optimally Loaded</p>
      <p class="text-2xl font-bold text-emerald-700" id="statOptimal">-</p>
    </div>
    <div class="card p-4 bg-gradient-to-br from-blue-50 to-white border-blue-100">
      <p class="text-sm font-medium text-blue-600 mb-1">Available Capacity</p>
      <p class="text-2xl font-bold text-blue-700" id="statLight">-</p>
    </div>
  </div>

  <div class="flex flex-col sm:flex-row gap-4 justify-between items-center">
    <div class="relative w-full sm:w-96">
      <i data-lucide="search" class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400"></i>
      <input id="searchInput" type="text" placeholder="Search TAs by name..." class="input pl-10" onkeyup="filterTAs()">
    </div>
  </div>

  <div class="space-y-4" id="workloadList">
    <div class="text-center text-zinc-400 py-12">Loading...</div>
  </div>
</div>

<jsp:include page="/common/layout-bottom.jsp"/>
<script>
let allWorkload = [];

/** Map GET /admin/workload row to UI fields (matches AdminService.getWorkload JSON). */
function normalizeWorkloadRow(w) {
  const assignments = Array.isArray(w.assignments) ? w.assignments : [];
  const assignedModules = assignments.map(function (a) {
    const code = a.moduleCode != null ? String(a.moduleCode) : '';
    const h = a.hoursPerWeek != null ? Number(a.hoursPerWeek) : 0;
    return code + (h ? ' (' + h + 'h/wk)' : '');
  });
  return {
    id: w.taUserId,
    name: w.taName || '',
    department: w.department || '',
    currentHours: w.totalHours != null ? Number(w.totalHours) : 0,
    maxHours: w.maxHours != null ? Number(w.maxHours) : 20,
    apiStatus: w.status || '',
    assignedModules: assignedModules,
    _assignments: assignments
  };
}

const STATUS_UI = {
  overloaded: { label: 'Overloaded', statusClass: 'text-red-600 bg-red-50 border-red-200', barColor: 'bg-red-500' },
  'at-capacity': { label: 'At Capacity', statusClass: 'text-amber-600 bg-amber-50 border-amber-200', barColor: 'bg-amber-500' },
  optimal: { label: 'Optimal', statusClass: 'text-emerald-600 bg-emerald-50 border-emerald-200', barColor: 'bg-emerald-500' },
  light: { label: 'Light Load', statusClass: 'text-blue-600 bg-blue-50 border-blue-200', barColor: 'bg-blue-500' }
};

async function loadWorkload() {
  try {
    const data = await adminApi.getWorkload();
    const raw = data.workload || data;
    const list = Array.isArray(raw) ? raw : [];
    allWorkload = list.map(normalizeWorkloadRow);
    renderSummary();
    renderTAs(allWorkload);
  } catch (e) {
    console.error('加载工作量失败', e);
    document.getElementById('workloadList').innerHTML =
      '<div class="text-center py-12 text-red-600">Failed to load workload. Please refresh.</div>';
  }
}

function renderSummary() {
  const total = allWorkload.length;
  const overloaded = allWorkload.filter(function (t) { return t.apiStatus === 'overloaded'; }).length;
  const optimal = allWorkload.filter(function (t) {
    return t.apiStatus === 'optimal' || t.apiStatus === 'at-capacity';
  }).length;
  const light = allWorkload.filter(function (t) { return t.apiStatus === 'light'; }).length;
  document.getElementById('statTotal').textContent = total;
  document.getElementById('statOverloaded').textContent = overloaded;
  document.getElementById('statOptimal').textContent = optimal;
  document.getElementById('statLight').textContent = light;
}

function filterTAs() {
  const q = document.getElementById('searchInput').value.toLowerCase();
  renderTAs(allWorkload.filter(function (t) {
    return (t.name || '').toLowerCase().includes(q) || (t.department || '').toLowerCase().includes(q);
  }));
}

function escapeAttr(s) {
  return String(s).replace(/\\/g, '\\\\').replace(/'/g, "\\'");
}

function renderTAs(tas) {
  const container = document.getElementById('workloadList');
  if (tas.length === 0) {
    container.innerHTML = '<div class="text-center py-12 text-zinc-500">No TAs found.</div>';
    return;
  }

  container.innerHTML = tas.map(function (ta) {
    const maxH = ta.maxHours || 20;
    const cur = ta.currentHours || 0;
    const pct = Math.min((cur / maxH) * 100, 100);
    const overPct = cur > maxH ? Math.min(((cur - maxH) / maxH) * 100, 50) : 0;
    const ui = STATUS_UI[ta.apiStatus] || STATUS_UI.light;
    const status = ui.label;
    const statusClass = ui.statusClass;
    const barColor = ui.barColor;

    const mods = (ta.assignedModules || []).map(function (m) {
      return '<span class="text-xs px-2 py-1 bg-zinc-100 text-zinc-700 rounded-md border border-zinc-200">' + m + '</span>';
    }).join('');

    const safeId = escapeAttr(ta.id);

    return '<div class="card p-5 overflow-hidden fade-in ' + (status === 'Overloaded' ? 'border-red-200 shadow-sm shadow-red-100' : '') + '">' +
      '<div class="flex flex-col lg:flex-row gap-6 items-start lg:items-center justify-between">' +
        '<div class="flex items-center gap-4 min-w-[250px]">' +
          '<div class="h-12 w-12 rounded-full flex items-center justify-center font-bold text-lg border ' +
            (status === 'Overloaded' ? 'bg-red-50 text-red-700 border-red-200' : 'bg-zinc-100 text-zinc-700 border-zinc-200') + '">' + ((ta.name || 'T').charAt(0)) + '</div>' +
          '<div><h3 class="font-bold text-zinc-900">' + (ta.name || '') + '</h3><p class="text-sm text-zinc-500">' + (ta.department || '') + '</p></div>' +
        '</div>' +
        '<div class="flex-1 w-full max-w-2xl">' +
          '<div class="flex items-center justify-between text-sm mb-2"><span class="font-medium text-zinc-700">Workload Capacity</span>' +
          '<span class="font-medium text-zinc-900">' + cur + ' / ' + maxH + ' hrs/week</span></div>' +
          '<div class="h-3 w-full bg-zinc-100 rounded-full overflow-hidden flex">' +
            '<div class="h-full ' + barColor + ' transition-all duration-1000" style="width:' + pct + '%"></div>' +
            (overPct > 0 ? '<div class="h-full bg-red-600/80" style="width:' + overPct + '%"></div>' : '') +
          '</div>' +
          '<div class="flex flex-wrap items-center gap-2 mt-3">' + mods + '</div>' +
        '</div>' +
        '<div class="flex flex-col sm:flex-row items-start sm:items-center gap-3 min-w-[200px] justify-end w-full lg:w-auto">' +
          '<span class="px-3 py-1.5 rounded-full text-xs font-semibold flex items-center gap-1.5 border ' + statusClass + '">' + status + '</span>' +
          (status === 'Overloaded' ? '<button onclick="reassignTA(\'' + safeId + '\')" class="btn btn-outline text-red-600 border-red-200 hover:bg-red-50 text-sm">Reassign</button>' : '') +
        '</div></div></div>';
  }).join('');
}

async function reassignTA(sourceTaId) {
  const targetId = prompt('Enter target TA user ID to move one module assignment to:');
  if (!targetId || !targetId.trim()) return;
  const moduleCode = prompt('Module code to move (must match an assignment on the overloaded TA):');
  if (!moduleCode || !moduleCode.trim()) return;
  const code = moduleCode.trim();
  const source = allWorkload.find(function (t) { return t.id === sourceTaId; });
  if (!source || !source._assignments) {
    showToast('Source TA not found in list.', 'error');
    return;
  }
  const match = source._assignments.find(function (a) {
    return (a.moduleCode || '').toLowerCase() === code.toLowerCase();
  });
  if (!match) {
    showToast('That module is not assigned to this TA.', 'error');
    return;
  }
  const hours = match.hoursPerWeek != null ? Number(match.hoursPerWeek) : 0;
  try {
    await adminApi.reassignWorkload(sourceTaId, { removeModules: [match.moduleCode] });
    await adminApi.reassignWorkload(targetId.trim(), {
      addModules: [{ moduleCode: match.moduleCode, hoursPerWeek: hours }]
    });
    showToast('Workload reassigned!');
    loadWorkload();
  } catch (e) {
    showToast(e.message || 'Reassign failed', 'error');
    loadWorkload();
  }
}

loadWorkload();
</script>
