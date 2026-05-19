<%@ page pageEncoding="UTF-8" %>
  <%@ include file="/common/auth-check.jsp" %>
    <jsp:include page="/common/layout-top.jsp" />

    <div class="space-y-8 max-w-6xl mx-auto">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 class="text-3xl font-bold tracking-tight text-zinc-900">TA Positions</h1>
          <p class="text-zinc-500 mt-2">Manage your current job postings and create new ones.</p>
        </div>
        <button onclick="openCreate()" class="btn btn-primary gap-2 shrink-0">
          <i data-lucide="plus" class="h-4 w-4"></i> Create Posting
        </button>
      </div>

      <div class="grid grid-cols-1 gap-6" id="jobsList">
        <div class="text-center text-zinc-400 py-12">Loading...</div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div id="jobModal" data-modal class="fixed inset-0 z-50 items-center justify-center bg-black/50 backdrop-blur-sm">
      <div class="bg-white rounded-2xl shadow-xl max-w-lg w-full mx-4 max-h-[90vh] overflow-y-auto">
        <div class="bg-zinc-50 border-b border-zinc-200/60 p-6">
          <h2 class="text-xl font-bold text-zinc-900" id="modalTitle">Create New Posting</h2>
        </div>
        <div class="p-6 space-y-4">
          <input type="hidden" id="editJobId">
          <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Title</label><input id="jTitle"
              class="input" placeholder="Teaching Assistant - Data Structures"></div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-zinc-700 mb-1.5">Module</label>
              <select id="jModule" class="input" onchange="onModuleChange()">
                <option value="">Loading your modules...</option>
              </select>
              <p id="jModuleHint" class="hidden text-xs text-amber-600 mt-1.5">You are not assigned to any module yet. Please ask an admin to assign you as the organiser of a module before posting jobs.</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-zinc-700 mb-1.5">Department</label>
              <input id="jDept" class="input bg-zinc-50" placeholder="Auto-filled from module" readonly>
            </div>
          </div>
          <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Description</label><textarea id="jDesc"
              rows="3" class="input resize-none" placeholder="Describe the position..."></textarea></div>
          <div class="grid grid-cols-3 gap-4">
            <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Positions</label><input id="jPositions"
                type="number" class="input" value="1"></div>
            <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Hours/Week</label><input id="jHours"
                type="number" class="input" value="10"></div>
            <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Type</label>
              <select id="jType" class="input">
                <option>Part-time</option>
                <option>Full-time</option>
              </select>
            </div>
          </div>
          <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Deadline</label><input id="jDeadline"
              type="date" class="input"></div>
          <div id="jobError" class="hidden text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-4 py-3">
          </div>
        </div>
        <div class="p-6 border-t border-zinc-200 flex justify-end gap-3">
          <button onclick="closeModal()" class="btn btn-secondary">Cancel</button>
          <button onclick="saveJob()" class="btn btn-primary" id="saveBtn">Create</button>
        </div>
      </div>
    </div>

    <jsp:include page="/common/layout-bottom.jsp" />
    <script>
      const ctx = document.querySelector('meta[name="ctx"]').content;

      let myModules = [];

      async function loadMyModules() {
        try {
          const data = await organiserApi.getMyModules();
          myModules = data.modules || data || [];
          renderModuleOptions();
        } catch (e) {
          console.error('加载我负责的模块失败', e);
          myModules = [];
          renderModuleOptions();
        }
      }

      function renderModuleOptions(selectedCode) {
        const sel = document.getElementById('jModule');
        const hint = document.getElementById('jModuleHint');
        if (!sel) return;
        if (!myModules || myModules.length === 0) {
          sel.innerHTML = '<option value="">No modules assigned to you</option>';
          sel.disabled = true;
          if (hint) hint.classList.remove('hidden');
          return;
        }
        sel.disabled = false;
        if (hint) hint.classList.add('hidden');
        const opts = ['<option value="">Select a module...</option>']
          .concat(myModules.map(m => {
            const label = (m.code || '') + (m.name ? ' \u2014 ' + m.name : '');
            const isSel = selectedCode && selectedCode === m.code ? ' selected' : '';
            return '<option value="' + m.code + '"' + isSel + '>' + label + '</option>';
          }));
        sel.innerHTML = opts.join('');
      }

      function onModuleChange() {
        const code = document.getElementById('jModule').value;
        const mod = myModules.find(m => m.code === code);
        document.getElementById('jDept').value = mod && mod.department ? mod.department : '';
      }

      async function loadJobs() {
        try {
          const data = await organiserApi.getJobs();
          const jobs = data.jobs || data || [];
          const container = document.getElementById('jobsList');

          if (jobs.length === 0) {
            container.innerHTML = '<div class="text-center text-zinc-400 py-12">No postings yet. Create your first one!</div>';
            return;
          }

          container.innerHTML = jobs.map(job => {
            const statusBadge = job.status === 'active' ? 'badge-success' : 'badge-outline';
            return '<div class="card hover:shadow-md transition-shadow fade-in"><div class="p-6">' +
              '<div class="flex flex-col md:flex-row gap-6 justify-between">' +
              '<div class="space-y-3">' +
              '<div class="flex items-center gap-3"><span class="badge badge-neutral">' + (job.moduleCode || '') + '</span>' +
              '<span class="badge ' + statusBadge + '">' + (job.status || 'active') + '</span></div>' +
              '<h3 class="text-xl font-bold text-zinc-900">' + job.title + '</h3>' +
              '<div class="flex flex-wrap items-center gap-4 text-sm text-zinc-500">' +
              '<span class="flex items-center gap-1.5"><i data-lucide="clock" class="h-4 w-4"></i> Deadline: ' + (job.deadline || 'TBD') + '</span>' +
              '</div>' +
              '</div>' +
              '<div class="flex flex-row md:flex-col justify-between items-end gap-4 md:gap-0">' +
              '<div class="flex items-center gap-6 text-center">' +
              '<div class="flex flex-col items-center"><span class="text-2xl font-bold text-zinc-900">' + (job.applicationCount || 0) + '</span><span class="text-xs text-zinc-500">Current</span></div>' +
              '<div class="h-8 w-px bg-zinc-200 hidden md:block"></div>' +
              '<div class="flex flex-col items-center"><span class="text-2xl font-bold text-zinc-900">' + (job.totalApplications || 0) + '</span><span class="text-xs text-zinc-500">Total</span></div>' +
              '<div class="h-8 w-px bg-zinc-200 hidden md:block"></div>' +
              '<div class="flex flex-col items-center"><span class="text-2xl font-bold text-zinc-900">' + (job.positions || 0) + '</span><span class="text-xs text-zinc-500">Positions</span></div>' +
              '</div>' +
              '<div class="flex items-center gap-2 mt-4">' +
              '<button onclick="editJob(\'' + job.id + '\')" class="btn btn-outline text-sm gap-2"><i data-lucide="edit-2" class="h-3.5 w-3.5"></i> Edit</button>' +
              '<button onclick="deleteJob(\'' + job.id + '\')" class="btn btn-ghost text-red-500 hover:text-red-600 hover:bg-red-50 p-2"><i data-lucide="trash-2" class="h-4 w-4"></i></button>' +
              '</div>' +
              '</div>' +
              '</div></div></div>';
          }).join('');
          lucide.createIcons();
        } catch (e) {
          console.error('加载职位列表失败', e);
        }
      }

      let allJobs = [];

      function openCreate() {
        if (!myModules || myModules.length === 0) {
          showToast('You have no modules assigned. Please ask an admin to assign you as the organiser of a module first.', 'error');
          return;
        }
        document.getElementById('editJobId').value = '';
        document.getElementById('modalTitle').textContent = 'Create New Posting';
        document.getElementById('saveBtn').textContent = 'Create';
        document.getElementById('jTitle').value = '';
        renderModuleOptions('');
        document.getElementById('jDept').value = '';
        document.getElementById('jDesc').value = '';
        document.getElementById('jPositions').value = '1';
        document.getElementById('jHours').value = '10';
        document.getElementById('jDeadline').value = '';
        document.getElementById('jDeadline').min = new Date().toISOString().split('T')[0];
        document.getElementById('jobError').classList.add('hidden');
        document.getElementById('jobModal').classList.add('show');
      }

      async function editJob(id) {
        try {
          const data = await organiserApi.getJobs();
          const job = (data.jobs || data).find(j => j.id === id);
          if (!job) return;
          document.getElementById('editJobId').value = id;
          document.getElementById('modalTitle').textContent = 'Edit Posting';
          document.getElementById('saveBtn').textContent = 'Update';
          document.getElementById('jTitle').value = job.title || '';
          renderModuleOptions(job.moduleCode || '');
          document.getElementById('jDept').value = job.department || '';
          document.getElementById('jDesc').value = job.description || '';
          document.getElementById('jPositions').value = job.positions || 1;
          document.getElementById('jHours').value = job.hoursPerWeek || 10;
          document.getElementById('jDeadline').value = job.deadline || '';
          document.getElementById('jDeadline').min = new Date().toISOString().split('T')[0];
          document.getElementById('jobError').classList.add('hidden');
          document.getElementById('jobModal').classList.add('show');
        } catch (e) { showToast(e.message, 'error'); }
      }

      function closeModal() { document.getElementById('jobModal').classList.remove('show'); }

      async function saveJob() {
        const errDiv = document.getElementById('jobError');
        errDiv.classList.add('hidden');
        const id = document.getElementById('editJobId').value;
        const moduleCode = document.getElementById('jModule').value.trim();
        const mod = myModules.find(m => m.code === moduleCode);
        if (!moduleCode || !mod) {
          errDiv.textContent = 'Please select a module you are responsible for.';
          errDiv.classList.remove('hidden');
          return;
        }
        const payload = {
          title: document.getElementById('jTitle').value.trim(),
          moduleId: mod.id,
          moduleCode: moduleCode,
          department: document.getElementById('jDept').value.trim(),
          description: document.getElementById('jDesc').value.trim(),
          positions: parseInt(document.getElementById('jPositions').value) || 1,
          hoursPerWeek: parseInt(document.getElementById('jHours').value) || 10,
          type: document.getElementById('jType').value,
          deadline: document.getElementById('jDeadline').value,
        };
        try {
          if (id) { await organiserApi.updateJob(id, payload); }
          else { await organiserApi.createJob(payload); }
          closeModal();
          showToast(id ? 'Job updated!' : 'Job created!');
          loadJobs();
        } catch (e) {
          errDiv.textContent = e.message || 'Save failed';
          errDiv.classList.remove('hidden');
        }
      }

      async function deleteJob(id) {
        if (!confirm('Delete this posting?')) return;
        try {
          await organiserApi.deleteJob(id);
          showToast('Job deleted');
          loadJobs();
        } catch (e) { showToast(e.message, 'error'); }
      }

      (async function init() {
        await loadMyModules();
        await loadJobs();
        if (new URLSearchParams(window.location.search).get('create') === 'true') openCreate();
      })();
    </script>