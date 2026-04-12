<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/auth-check.jsp" %>
<jsp:include page="/common/layout-top.jsp"/>

<div class="space-y-8 max-w-6xl mx-auto">
  <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
    <div>
      <h1 class="text-3xl font-bold tracking-tight text-zinc-900">System Logs</h1>
      <p class="text-zinc-500 mt-2">Monitor platform activity, security events, and audit trails.</p>
    </div>
  </div>

  <div class="flex flex-col sm:flex-row gap-4 justify-between items-center">
    <div class="relative w-full sm:w-96">
      <i data-lucide="search" class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400"></i>
      <input id="searchInput" type="text" placeholder="Search events, actors, or IDs..." class="input pl-10" onkeyup="debounceSearch()">
    </div>
    <div class="flex items-center gap-3">
      <div id="autoRefreshIndicator" class="flex items-center gap-2 text-xs text-emerald-600">
        <span class="relative flex h-2 w-2">
          <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
          <span class="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
        </span>
        Live
      </div>
      <select id="typeFilter" class="input w-auto" onchange="loadLogs()">
        <option value="">All Types</option>
        <option value="info">Info</option>
        <option value="success">Success</option>
        <option value="warning">Warning</option>
        <option value="error">Error</option>
      </select>
    </div>
  </div>

  <div class="card overflow-hidden">
    <div class="overflow-x-auto">
      <table class="w-full text-sm text-left">
        <thead class="text-xs text-zinc-500 bg-zinc-50/50 border-b border-zinc-200 uppercase tracking-wider">
          <tr>
            <th class="px-6 py-4 font-medium">Event ID / Time</th>
            <th class="px-6 py-4 font-medium">Action</th>
            <th class="px-6 py-4 font-medium">Actor</th>
            <th class="px-6 py-4 font-medium w-1/3">Details</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-zinc-200" id="logsBody">
          <tr><td colspan="4" class="p-6 text-center text-zinc-400">Loading...</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</div>

<jsp:include page="/common/layout-bottom.jsp"/>
<script>
let searchTimer;
let pollTimer;
const POLL_INTERVAL = 5000;

function debounceSearch() { clearTimeout(searchTimer); searchTimer = setTimeout(loadLogs, 300); }

async function loadLogs() {
  try {
    const params = {};
    const kw = document.getElementById('searchInput').value.trim();
    const type = document.getElementById('typeFilter').value;
    if (kw) params.keyword = kw;
    if (type) params.type = type;
    const data = await adminApi.getLogs(Object.keys(params).length ? params : undefined);
    const logs = data.logs || data || [];
    renderLogs(logs);
  } catch (e) { console.error('Failed to load logs', e); }
}

function renderLogs(logs) {
  const tbody = document.getElementById('logsBody');
  if (logs.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4" class="p-8 text-center text-zinc-500"><div class="flex flex-col items-center gap-3"><i data-lucide="file-text" class="h-8 w-8 text-zinc-300"></i><p>No audit logs found.</p></div></td></tr>';
    lucide.createIcons();
    return;
  }

  const typeStyles = {
    info: { bg: 'bg-blue-50 text-blue-700 border-blue-200', icon: 'info' },
    success: { bg: 'bg-emerald-50 text-emerald-700 border-emerald-200', icon: 'check-circle-2' },
    warning: { bg: 'bg-amber-50 text-amber-700 border-amber-200', icon: 'alert-circle' },
    error: { bg: 'bg-red-50 text-red-700 border-red-200', icon: 'shield-alert' },
  };

  tbody.innerHTML = logs.map(log => {
    const ts = typeStyles[log.type] || typeStyles.info;
    return '<tr class="hover:bg-zinc-50/50 transition-colors">' +
      '<td class="px-6 py-4 whitespace-nowrap"><div class="flex flex-col gap-1"><span class="font-mono text-xs text-zinc-900 font-medium">' + (log.id || '') + '</span>' +
        '<span class="text-zinc-500 text-xs">' + (log.timestamp || '') + '</span></div></td>' +
      '<td class="px-6 py-4 whitespace-nowrap"><div class="inline-flex items-center gap-2 px-2.5 py-1 rounded-md border text-xs font-medium ' + ts.bg + '">' +
        '<i data-lucide="' + ts.icon + '" class="h-4 w-4"></i>' + (log.action || '') + '</div></td>' +
      '<td class="px-6 py-4 whitespace-nowrap text-zinc-600 font-mono text-xs">' + (log.actor || '') + '</td>' +
      '<td class="px-6 py-4 text-zinc-600 text-sm">' + (log.details || '') + '</td></tr>';
  }).join('');
  lucide.createIcons();
}

function startPolling() {
  stopPolling();
  pollTimer = setInterval(loadLogs, POLL_INTERVAL);
}

function stopPolling() {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null; }
}

document.addEventListener('visibilitychange', function() {
  if (document.hidden) { stopPolling(); } else { loadLogs(); startPolling(); }
});

loadLogs();
startPolling();
</script>
