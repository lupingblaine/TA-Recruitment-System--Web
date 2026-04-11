<%@ page pageEncoding="UTF-8" %>
  <%@ include file="/common/auth-check.jsp" %>
    <jsp:include page="/common/layout-top.jsp" />

    <div class="space-y-6 h-full flex flex-col">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 class="text-3xl font-bold tracking-tight text-zinc-900">Recruitment Pipeline</h1>
          <p class="text-zinc-500 mt-2">Manage applicants through the hiring stages.</p>
        </div>
        <div class="flex items-center gap-3">
          <div class="relative w-full md:w-64">
            <i data-lucide="search" class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400"></i>
            <input id="searchInput" type="text" placeholder="Search candidates..." class="input pl-9 bg-white"
              onkeyup="filterView()">
          </div>
          <select id="moduleFilter" class="input w-auto" onchange="loadPipeline()">
            <option value="">All Modules</option>
          </select>
        </div>
      </div>

      <div class="flex-1 overflow-x-auto pb-4">
        <div class="flex gap-6 h-full min-w-max" id="kanban">
          <div class="text-center text-zinc-400 py-12 w-full">Loading...</div>
        </div>
      </div>
    </div>

    <!-- Candidate Detail Panel -->
    <div id="candidatePanel" class="fixed inset-0 z-50 hidden">
      <div class="absolute inset-0 bg-zinc-900/20 backdrop-blur-sm" onclick="closePanel()"></div>
      <div
        class="absolute inset-y-0 right-0 w-full md:w-[400px] bg-white border-l border-zinc-200 shadow-2xl flex flex-col">
        <div class="p-6 border-b border-zinc-200 flex items-center justify-between">
          <h2 class="text-lg font-bold text-zinc-900">Applicant Summary</h2>
          <button onclick="closePanel()" class="btn btn-ghost p-2"><i data-lucide="x" class="h-5 w-5"></i></button>
        </div>
        <div class="flex-1 overflow-y-auto p-6 space-y-6" id="panelContent"></div>
        <div class="p-6 border-t border-zinc-200 flex gap-3 bg-zinc-50">
          <a id="viewFullApp" href="#" class="btn btn-primary flex-1 text-center">View Full Application</a>
        </div>
      </div>
    </div>

    <jsp:include page="/common/layout-bottom.jsp" />
    <script>
      const ctx = document.querySelector('meta[name="ctx"]').content;
      const COLUMNS = [
        { id: 'applied', title: 'Applied', color: 'bg-zinc-100' },
        { id: 'review', title: 'Review', color: 'bg-blue-100 text-blue-800' },
        { id: 'shortlisted', title: 'Shortlist', color: 'bg-purple-100 text-purple-800' },
        { id: 'interview', title: 'Interview', color: 'bg-amber-100 text-amber-800' },
        { id: 'offer', title: 'Offer', color: 'bg-emerald-100 text-emerald-800' },
      ];
      let allApps = [];
      let pipelineData = {};

      async function loadPipeline() {
        const module = document.getElementById('moduleFilter').value;
        try {
          const data = await organiserApi.getPipeline(module || undefined);
          // API returns {applied: [], review: [], shortlisted: [], interview: [], offer: []}
          pipelineData = data;
          // Flatten to array for backward compatibility while preserving stage info
          allApps = Object.values(data).flat();

          const mods = [...new Set(allApps.map(a => a.role).filter(Boolean))];
          const sel = document.getElementById('moduleFilter');
          const current = sel.value;
          sel.innerHTML = '<option value="">All Modules</option>' + mods.map(m => '<option value="' + m + '"' + (m === current ? ' selected' : '') + '>' + m + '</option>').join('');

          renderKanban();
        } catch (e) {
          console.error('加载管道失败', e);
          document.getElementById('kanban').innerHTML = '<div class="text-center text-red-500 py-12 w-full">Failed to load pipeline</div>';
        }
      }

      function filterView() {
        renderKanban();
      }

      function renderKanban() {
        const search = (document.getElementById('searchInput').value || '').toLowerCase();
        const apps = allApps.filter(a => !search || (a.name || '').toLowerCase().includes(search) || (a.role || '').toLowerCase().includes(search));

        document.getElementById('kanban').innerHTML = COLUMNS.map(col => {
          const colApps = apps.filter(a => a.status === col.id);
          return '<div class="flex flex-col w-80 shrink-0 bg-zinc-50/50 rounded-2xl p-4 border border-transparent kanban-col" data-status="' + col.id + '" ondragover="onDragOver(event)" ondrop="onDrop(event, \'' + col.id + '\')">' +
            '<div class="flex items-center justify-between mb-4 px-2">' +
            '<h3 class="font-semibold text-zinc-900 flex items-center gap-2">' + col.title +
            ' <span class="px-2 py-0.5 rounded-full text-xs font-bold ' + col.color + '">' + colApps.length + '</span></h3>' +
            '</div>' +
            '<div class="flex-1 space-y-3 overflow-y-auto">' +
            colApps.map(a => renderCard(a)).join('') +
            '</div></div>';
        }).join('');
        lucide.createIcons();
      }

      function renderCard(a) {
        return '<div class="bg-white p-4 rounded-xl border border-zinc-200/60 shadow-sm hover:shadow-md transition-all cursor-grab kanban-card" draggable="true" data-id="' + a.id + '" ondragstart="onDragStart(event, \'' + a.id + '\')" onclick="showCandidate(\'' + a.id + '\')">' +
          '<div class="flex justify-between items-start mb-3">' +
          '<div class="flex items-center gap-3">' +
          '<div class="h-10 w-10 rounded-full bg-zinc-100 flex items-center justify-center font-bold text-zinc-700">' + ((a.name || 'A').charAt(0)) + '</div>' +
          '<div><h4 class="font-semibold text-sm text-zinc-900">' + (a.name || 'Applicant') + '</h4>' + '<p class="text-xs text-zinc-500">ID: ' + (a.id || '-') + '</p>' + '<p class="text-xs text-zinc-500">' + (a.role || '') + '</p></div>' +
          '</div>' +
          '</div>' +
          '<div class="flex items-center justify-between text-xs text-zinc-500 mt-4 pt-4 border-t border-zinc-100">' +
          '<span class="text-zinc-900 font-bold">Applied ' + (a.date || '') + '</span>' +
          '</div></div>';
      }

      let dragId = null;
      function onDragStart(e, id) { dragId = id; e.dataTransfer.effectAllowed = 'move'; }
      function onDragOver(e) { e.preventDefault(); e.currentTarget.classList.add('drag-over'); }
      function onDrop(e, status) {
        e.preventDefault();
        e.currentTarget.classList.remove('drag-over');
        if (dragId) updateStatus(dragId, status);
      }

      async function updateStatus(appId, newStatus) {
        try {
          await organiserApi.updateCandidateStatus(appId, newStatus);
          const app = allApps.find(a => a.id === appId);
          if (app) app.status = newStatus;
          renderKanban();
          showToast('Status updated!');
        } catch (e) {
          showToast(e.message || 'Update failed', 'error');
        }
      }

      function showCandidate(id) {
        const app = allApps.find(a => a.id === id);
        if (!app) return;
        const panel = document.getElementById('panelContent');
        panel.innerHTML = '<div class="flex items-center gap-4">' +
          '<div class="h-16 w-16 rounded-full bg-zinc-100 flex items-center justify-center text-xl font-bold text-zinc-700">' + ((app.name || 'A').charAt(0)) + '</div>' +
          '<div class="flex-1"><h3 class="text-xl font-bold text-zinc-900">' + (app.name || 'Applicant') + '</h3>' +
          '<p class="text-sm text-zinc-500">Application ID: ' + (app.id || '-') + '</p>' +
          '<p class="text-sm text-zinc-500">Applied for ' + (app.role || '') + '</p>' +
          '<span class="badge badge-neutral mt-2 capitalize">' + app.status + '</span></div></div>' +
          '<div class="grid grid-cols-2 gap-4">' +
          '<div class="p-4 rounded-xl bg-zinc-50 border border-zinc-100"><p class="text-xs text-zinc-500 mb-1">Applied On</p><p class="text-lg font-bold text-zinc-900">' + (app.date || '-') + '</p></div>' +
          '<div class="p-4 rounded-xl bg-zinc-50 border border-zinc-100"><p class="text-xs text-zinc-500 mb-1">Status</p><p class="text-lg font-bold text-zinc-900 capitalize">' + app.status + '</p></div>' +
          '</div>' +
          (app.coverLetter ? '<div><h4 class="text-sm font-semibold text-zinc-900 mb-2">Cover Letter</h4><p class="text-sm text-zinc-600 leading-relaxed">' + app.coverLetter.substring(0, 200) + '...</p></div>' : '');

        document.getElementById('viewFullApp').href = ctx + '/organiser/application-detail.jsp?id=' + app.id;
        document.getElementById('candidatePanel').classList.remove('hidden');
        lucide.createIcons();
      }

      function closePanel() { document.getElementById('candidatePanel').classList.add('hidden'); }

      loadPipeline();
    </script>