<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/auth-check.jsp" %>
<jsp:include page="/common/layout-top.jsp"/>

<div class="space-y-8 max-w-6xl mx-auto">
  <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
    <div>
      <h1 class="text-3xl font-bold tracking-tight text-zinc-900">Global Modules</h1>
      <p class="text-zinc-500 mt-2">Manage all courses and assign Module Organisers across the university.</p>
    </div>
    <button onclick="openModuleModal()" class="btn btn-primary gap-2 shrink-0">
      <i data-lucide="plus" class="h-4 w-4"></i> Add Module
    </button>
  </div>

  <div class="flex flex-col sm:flex-row gap-4 justify-between items-center">
    <div class="relative w-full sm:w-96">
      <i data-lucide="search" class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400"></i>
      <input id="searchInput" type="text" placeholder="Search modules, names, or organisers..." class="input pl-10" onkeyup="debounceSearch()">
    </div>
  </div>

  <div class="card overflow-hidden">
    <div class="overflow-x-auto">
      <table class="w-full text-sm text-left">
        <thead class="text-xs text-zinc-500 bg-zinc-50/50 border-b border-zinc-200 uppercase tracking-wider">
          <tr>
            <th class="px-6 py-4 font-medium">Module</th>
            <th class="px-6 py-4 font-medium">Organiser (MO)</th>
            <th class="px-6 py-4 font-medium">Department</th>
            <th class="px-6 py-4 font-medium">Term</th>
            <th class="px-6 py-4 font-medium">Status</th>
            <th class="px-6 py-4 font-medium text-right">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-zinc-200" id="modulesBody">
          <tr><td colspan="6" class="p-6 text-center text-zinc-400">Loading...</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</div>

<!-- Module Modal -->
<div id="moduleModal" data-modal class="fixed inset-0 z-50 items-center justify-center bg-black/50 backdrop-blur-sm">
  <div class="bg-white rounded-2xl shadow-xl max-w-lg w-full mx-4">
    <div class="bg-zinc-50 border-b border-zinc-200/60 p-6">
      <h2 class="text-xl font-bold text-zinc-900" id="modModalTitle">Add Module</h2>
    </div>
    <div class="p-6 space-y-4">
      <input type="hidden" id="editModId">
      <div class="grid grid-cols-2 gap-4">
        <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Code</label><input id="mCode" class="input" placeholder="CS201"></div>
        <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Name</label><input id="mName" class="input" placeholder="Data Structures"></div>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Department</label><input id="mDept" class="input" placeholder="Computer Science"></div>
        <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Term</label><input id="mTerm" class="input" placeholder="Fall 2026"></div>
      </div>
      <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Organiser ID</label><input id="mOrganiser" class="input" placeholder="User ID of the MO"></div>
      <div id="modError" class="hidden text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-4 py-3"></div>
    </div>
    <div class="p-6 border-t border-zinc-200 flex justify-end gap-3">
      <button onclick="closeModuleModal()" class="btn btn-secondary">Cancel</button>
      <button onclick="saveModule()" class="btn btn-primary" id="modSaveBtn">Create</button>
    </div>
  </div>
</div>

<jsp:include page="/common/layout-bottom.jsp"/>
<script>
let allModules = [];
let searchTimer;

function debounceSearch() { clearTimeout(searchTimer); searchTimer = setTimeout(loadModules, 300); }

async function loadModules() {
  try {
    const kw = document.getElementById('searchInput').value.trim();
    const data = await adminApi.getModules(kw || undefined);
    allModules = data.modules || data || [];
    renderModules(allModules);
  } catch (e) { console.error('加载模块列表失败', e); }
}

function renderModules(modules) {
  const tbody = document.getElementById('modulesBody');
  if (modules.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="p-8 text-center text-zinc-500">No modules found.</td></tr>';
    return;
  }
  tbody.innerHTML = modules.map(m => {
    const statusBadge = m.status === 'active' ? 'badge-success' : m.status === 'draft' ? 'badge-warning' : 'badge-neutral';
    return '<tr class="table-row hover:bg-zinc-50/50 transition-colors">' +
      '<td class="px-6 py-4 whitespace-nowrap"><div class="flex items-center gap-3">' +
        '<div class="h-8 w-8 rounded-md bg-zinc-100 flex items-center justify-center text-zinc-500"><i data-lucide="book-open" class="h-4 w-4"></i></div>' +
        '<div><div class="font-semibold text-zinc-900">' + m.code + '</div><div class="text-zinc-500 text-xs">' + (m.name || '') + '</div></div></div></td>' +
      '<td class="px-6 py-4 whitespace-nowrap text-zinc-700">' + (m.organiserName || m.organiserId || '-') + '</td>' +
      '<td class="px-6 py-4 whitespace-nowrap text-zinc-600">' + (m.department || '-') + '</td>' +
      '<td class="px-6 py-4 whitespace-nowrap text-zinc-600">' + (m.term || '-') + '</td>' +
      '<td class="px-6 py-4 whitespace-nowrap"><span class="badge ' + statusBadge + ' capitalize">' + (m.status || 'active') + '</span></td>' +
      '<td class="px-6 py-4 whitespace-nowrap text-right"><div class="actions flex items-center justify-end gap-2">' +
        '<button onclick="editModule(\'' + m.id + '\')" class="btn btn-ghost p-2 text-zinc-500 hover:text-zinc-900"><i data-lucide="edit-2" class="h-4 w-4"></i></button>' +
        '<button onclick="deleteModule(\'' + m.id + '\')" class="btn btn-ghost p-2 text-zinc-500 hover:text-red-600"><i data-lucide="trash-2" class="h-4 w-4"></i></button>' +
      '</div></td></tr>';
  }).join('');
  lucide.createIcons();
}

function openModuleModal() {
  document.getElementById('editModId').value = '';
  document.getElementById('modModalTitle').textContent = 'Add Module';
  document.getElementById('modSaveBtn').textContent = 'Create';
  ['mCode','mName','mDept','mTerm','mOrganiser'].forEach(id => document.getElementById(id).value = '');
  document.getElementById('moduleModal').classList.add('show');
}

function editModule(id) {
  const m = allModules.find(x => x.id === id);
  if (!m) return;
  document.getElementById('editModId').value = id;
  document.getElementById('modModalTitle').textContent = 'Edit Module';
  document.getElementById('modSaveBtn').textContent = 'Update';
  document.getElementById('mCode').value = m.code || '';
  document.getElementById('mName').value = m.name || '';
  document.getElementById('mDept').value = m.department || '';
  document.getElementById('mTerm').value = m.term || '';
  document.getElementById('mOrganiser').value = m.organiserId || '';
  document.getElementById('moduleModal').classList.add('show');
}

function closeModuleModal() { document.getElementById('moduleModal').classList.remove('show'); }

async function saveModule() {
  const errDiv = document.getElementById('modError');
  errDiv.classList.add('hidden');
  const id = document.getElementById('editModId').value;
  const payload = {
    code: document.getElementById('mCode').value.trim(),
    name: document.getElementById('mName').value.trim(),
    department: document.getElementById('mDept').value.trim(),
    term: document.getElementById('mTerm').value.trim(),
    organiserId: document.getElementById('mOrganiser').value.trim(),
  };
  try {
    if (id) { await adminApi.updateModule(id, payload); }
    else { await adminApi.createModule(payload); }
    closeModuleModal();
    showToast(id ? 'Module updated!' : 'Module created!');
    loadModules();
  } catch (e) { errDiv.textContent = e.message || 'Save failed'; errDiv.classList.remove('hidden'); }
}

async function deleteModule(id) {
  if (!confirm('Delete this module?')) return;
  try { await adminApi.deleteModule(id); showToast('Module deleted'); loadModules(); }
  catch (e) { showToast(e.message, 'error'); }
}

loadModules();
</script>
