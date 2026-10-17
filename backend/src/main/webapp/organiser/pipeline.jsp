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
          <label class="sr-only" for="pipelinePeriod">Application period</label>
          <select id="pipelinePeriod" class="input w-auto" onchange="loadPipeline()"><option value="">All periods</option></select>
          <select id="moduleFilter" aria-label="Course" class="input w-auto" onchange="refreshSectionFilter(); renderKanban()">
            <option value="">All Modules</option>
          </select>
        </div>
      </div>

      <div class="flex flex-wrap items-center gap-3">
        <label for="sectionFilter">Section</label>
        <select id="sectionFilter" class="input w-auto" onchange="renderKanban()"><option value="">All sections</option></select>
        <label for="matchFilter">Time matching</label>
        <select id="matchFilter" class="input w-auto" onchange="renderKanban()">
          <option value="">All results</option><option value="available">Available</option>
          <option value="conflict">Time conflict</option><option value="pending">Needs confirmation</option>
        </select>
        <p class="text-sm text-zinc-500">Uses submitted snapshots and current active sections. Open an application to review time conflicts.</p>
      </div>
      <div class="flex flex-wrap gap-3">
<label>Student level<select id="qualificationLevel" class="input" onchange="renderKanban()"><option value="">All levels</option><option value="undergraduate">Undergraduate</option><option value="masters">Master’s</option><option value="phd">PhD</option></select></label>
<label>Skill<input id="qualificationSkill" class="input" oninput="renderKanban()" placeholder="e.g. Python"></label><label>Interest<input id="qualificationInterest" class="input" oninput="renderKanban()"></label>
<label>Teaching experience<select id="qualificationExperience" class="input" onchange="renderKanban()"><option value="">All applicants</option><option value="yes">Reported teaching experience</option><option value="no">None reported</option></select></label>
<label>Qualification match<select id="qualificationMatch" class="input" onchange="renderKanban()"><option value="">All results</option><option value="meets_required">Meets required criteria</option><option value="missing_required">Missing required criteria</option><option value="needs_confirmation">Needs confirmation</option></select></label>
<label>Sort<select id="qualificationSort" class="input" onchange="renderKanban()"><option value="newest">Newest application</option><option value="name">Applicant name</option><option value="score">Qualification match score</option></select></label>
      </div>
      <div class="flex-1 overflow-x-auto pb-4">
        <div class="flex gap-6 h-full min-w-max" id="kanban">
          <div class="text-center text-zinc-400 py-12 w-full">Loading...</div>
        </div>
      </div>
    </div>

    <!-- Interview Schedule Modal -->
    <div id="interviewModal" class="fixed inset-0 z-50 hidden">
      <div class="absolute inset-0 bg-zinc-900/50 backdrop-blur-sm" onclick="closeInterviewModal()"></div>
      <div class="absolute inset-0 flex items-center justify-center p-4">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md">
          <div class="p-6 border-b border-zinc-200">
            <h2 class="text-lg font-bold text-zinc-900">Schedule Interview</h2>
            <p class="text-sm text-zinc-500 mt-1">Set up an interview for <span id="modalCandidateName"
                class="font-medium text-zinc-700"></span></p>
          </div>
          <form id="interviewForm" class="p-6 space-y-4" onsubmit="submitInterview(event)">
            <input type="hidden" id="modalAppId">
            <input type="hidden" id="modalCandidateId">
            <input type="hidden" id="modalModuleCode">
            <input type="hidden" id="modalModuleName">
            <div>
              <label class="block text-sm font-medium text-zinc-700 mb-1">Date *</label>
              <input type="date" id="interviewDate" required class="input w-full">
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-zinc-700 mb-1">Start Time *</label>
                <input type="time" id="interviewStart" required class="input w-full" onchange="syncInterviewEndTime()">
              </div>
              <div>
                <label class="block text-sm font-medium text-zinc-700 mb-1">End Time</label>
                <input type="time" id="interviewEnd" class="input w-full">
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-zinc-700 mb-1">Interview Type *</label>
              <select id="interviewType" required class="input w-full" onchange="toggleLocation()">
                <option value="video">Video Call</option>
                <option value="in-person">In-Person</option>
              </select>
            </div>
            <div id="locationField">
              <label class="block text-sm font-medium text-zinc-700 mb-1">Location</label>
              <input type="text" id="interviewLocation" placeholder="Location not needed for video call"
                class="input w-full" disabled>
            </div>
            <div class="flex gap-3 pt-4">
              <button type="button" onclick="closeInterviewModal()" class="btn btn-outline flex-1">Cancel</button>
              <button type="submit" class="btn btn-primary flex-1">Schedule Interview</button>
            </div>
          </form>
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

    <!-- Schedule Interview Modal -->
    <div id="scheduleModal" class="fixed inset-0 z-50 items-center justify-center bg-black/50 backdrop-blur-sm hidden">
      <div class="bg-white rounded-2xl shadow-xl max-w-lg w-full mx-4">
        <div class="bg-zinc-50 border-b border-zinc-200/60 p-6">
          <h2 class="text-xl font-bold text-zinc-900">Schedule Interview</h2>
        </div>
        <div class="p-6 space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Date</label><input id="iDate" type="date"
                class="input"></div>
            <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Start Time</label><input id="iTime"
                type="time" class="input"></div>
          </div>
          <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Location</label><input id="iLocation"
              class="input" placeholder="Location not needed for video call" disabled></div>
          <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Type</label>
            <select id="iType" class="input" onchange="toggleScheduleLocation()">
              <option value="video">Video Call</option>
              <option value="in_person">In Person</option>
            </select>
          </div>
          <div id="schedError" class="hidden text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-4 py-3">
          </div>
        </div>
        <div class="p-6 border-t border-zinc-200 flex justify-end gap-3">
          <button onclick="closeSchedule()" class="btn btn-secondary">Cancel</button>
          <button onclick="saveInterview()" class="btn btn-primary">Schedule</button>
        </div>
      </div>
    </div>

    <jsp:include page="/common/layout-bottom.jsp" />
    <script src="<%= request.getContextPath() %>/static/js/section-matching.js"></script>
    <script>
      const ctx = document.querySelector('meta[name="ctx"]').content;
      // Format ISO date like "2026-05-20T12:42:00" → "2026.5.20 12:42"
      function formatDate(isoStr) {
        if (!isoStr) return 'TBD';
        try {
          const d = new Date(isoStr);
          if (isNaN(d.getTime())) return isoStr;
          const y = d.getFullYear();
          const m = d.getMonth() + 1;
          const day = d.getDate();
          const hh = String(d.getHours()).padStart(2, '0');
          const mm = String(d.getMinutes()).padStart(2, '0');
          return y + '.' + m + '.' + day + ' ' + hh + ':' + mm;
        } catch (e) { return isoStr; }
      }
      const COLUMNS = [
        { id: 'applied', title: 'Applied', color: 'bg-zinc-100' },
        { id: 'review', title: 'Review', color: 'bg-blue-100 text-blue-800' },
        { id: 'shortlisted', title: 'Shortlist', color: 'bg-purple-100 text-purple-800' },
        { id: 'interview', title: 'Interview', color: 'bg-amber-100 text-amber-800' },
        { id: 'offer', title: 'Offer', color: 'bg-emerald-100 text-emerald-800' },
      ];
      const STATUS_ORDER = { applied: 0, review: 1, shortlisted: 2, interview: 3, offer: 4, rejected: 5 };
      let allApps = [];
      let pipelineData = {};

      let pipelineRequest = 0;
      async function loadPipeline() {
        const sequence = ++pipelineRequest;
        try {
          const data = await organiserApi.getPipeline(undefined, document.getElementById('pipelinePeriod').value);
          if (sequence !== pipelineRequest) return;
          // API returns {applied: [], review: [], shortlisted: [], interview: [], offer: []}
          pipelineData = data;
          // Flatten to array for backward compatibility while preserving stage info
          allApps = Object.values(data).flat();

          const mods = [...new Set(allApps.map(a => a.role).filter(Boolean))];
          const sel = document.getElementById('moduleFilter');
          const current = sel.value;
          sel.replaceChildren(new Option('All Modules', ''));
          mods.forEach(m => sel.add(new Option(m, m)));
          if (mods.includes(current)) sel.value = current;

          refreshSectionFilter();
          renderKanban();
        } catch (e) {
          if (sequence !== pipelineRequest) return;
          console.error('Failed to load pipeline', e);
          document.getElementById('kanban').innerHTML = '<div class="text-center text-red-500 py-12 w-full">Failed to load pipeline</div>';
        }
      }

      function refreshSectionFilter() {
        const select = document.getElementById('sectionFilter');
        const previous = select.value;
        const course = document.getElementById('moduleFilter').value;
        const sections = new Map();
        allApps.filter(a => !course || a.role === course).forEach(a => {
          ((a.sectionMatching || {}).sections || []).forEach(s => sections.set(s.sectionId, s));
        });
        select.replaceChildren(new Option('All sections', ''));
        sections.forEach(s => select.add(new Option(s.moduleCode + ' · ' + s.name, s.sectionId)));
        if (sections.has(previous)) select.value = previous;
      }
      function filterView() {
        renderKanban();
      }

      function renderKanban() {
        const search = (document.getElementById('searchInput').value || '').toLowerCase();
        const course = document.getElementById('moduleFilter').value;
        const sectionId = document.getElementById('sectionFilter').value;
        const status = document.getElementById('matchFilter').value;
        const apps = allApps.filter(a => {
          const q=a.submittedQualifications||{},qm=a.qualificationMatching||{};
          const level=document.getElementById('qualificationLevel').value,skill=document.getElementById('qualificationSkill').value.trim().toLowerCase(),interest=document.getElementById('qualificationInterest').value.trim().toLowerCase(),experience=document.getElementById('qualificationExperience').value,qstatus=document.getElementById('qualificationMatch').value;
          if(level&&q.studentLevel!==level)return false;
          if(skill&&!(q.skills||[]).some(v=>v.toLowerCase().includes(skill)))return false;
          if(interest&&!(q.interests||[]).some(v=>v.toLowerCase().includes(interest)))return false;
          if(experience&&((!!(q.teachingExperience||'').trim())!==(experience==='yes')))return false;
          if(qstatus&&qm.status!==qstatus)return false;
          const match = SectionMatching.selected(a, sectionId);
          return (!course || a.role === course) && (!sectionId || !!match) && (!status || (match && match.status === status)) &&
            (!search || (a.name || '').toLowerCase().includes(search) || (a.role || '').toLowerCase().includes(search));
        });

        const sort=document.getElementById('qualificationSort').value;
        apps.sort((a,b)=>sort==='name'?(a.name||'').localeCompare(b.name||'','en'):sort==='score'?((b.qualificationMatching||{}).score??-1)-((a.qualificationMatching||{}).score??-1):(b.date||'').localeCompare(a.date||''));
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

      function renderCard(original) {
        const a=escapeDisplay(original);
        let extraInfo = '';
        if (a.interview) {
          extraInfo = '<div class="text-xs text-zinc-500 mt-2"><i data-lucide="calendar" class="h-3 w-3 inline mr-1"></i>' + (a.interview.date || '') + ' ' + (a.interview.startTime || '') + '<br><i data-lucide="' + (a.interview.type === 'video' ? 'video' : 'user') + '" class="h-3 w-3 inline mr-1"></i>' + (a.interview.location || a.interview.type || '') + '</div>';
        }
        return '<div class="bg-white p-4 rounded-xl border border-zinc-200/60 shadow-sm hover:shadow-md transition-all cursor-grab kanban-card" draggable="true" data-id="' + a.id + '" ondragstart="onDragStart(event, \'' + a.id + '\')" onclick="showCandidate(\'' + a.id + '\')">' +
          '<div class="flex justify-between items-start mb-3">' +
          '<div class="flex items-center gap-3">' +
          '<div class="h-10 w-10 rounded-full bg-zinc-100 flex items-center justify-center font-bold text-zinc-700">' + ((a.name || 'A').charAt(0)) + '</div>' +
          '<div><h4 class="font-semibold text-sm text-zinc-900">' + (a.name || 'Applicant') + '</h4>' + '<p class="text-xs text-zinc-500">ID: ' + (a.id || '-') + '</p>' + '<p class="text-xs text-zinc-500">' + (a.role || '') + '</p></div>' +
          '</div>' +
          '</div>' +
          '<div class="mt-3">' + SectionMatching.badge(SectionMatching.selected(a, document.getElementById('sectionFilter').value)) + '</div>' +
          extraInfo +
          '<div class="flex items-center justify-between text-xs text-zinc-500 mt-4 pt-4 border-t border-zinc-100">' +
          '<span class="text-zinc-900 font-bold">Applied ' + formatDate(a.date) + '</span>' +
          '</div></div>';
      }

      let dragId = null;
      let pendingInterviewApp = null;

      function onDragStart(e, id) { dragId = id; e.dataTransfer.effectAllowed = 'move'; }
      function onDragOver(e) { e.preventDefault(); e.currentTarget.classList.add('drag-over'); }
      function onDrop(e, status) {
        e.preventDefault();
        e.currentTarget.classList.remove('drag-over');
        if (dragId) {
          const app = allApps.find(a => a.id === dragId);
          if (app && STATUS_ORDER[status] < STATUS_ORDER[app.status] && status !== 'rejected') {
            showToast('Cannot move candidate backwards once they are in a later stage.', 'error');
            dragId = null;
            return;
          }
          if (status === 'interview') {
            // store only appId to avoid stale object references
            pendingInterviewApp = { appId: dragId };
            const freshApp = allApps.find(a => a.id === dragId);
            openInterviewModal(freshApp);
          } else {
            updateStatus(dragId, status);
          }
          dragId = null;
        }
      }

      async function updateStatus(appId, newStatus) {
        const app = allApps.find(a => a.id === appId);
        if (!app) return;

        // If moving to interview, show the scheduling modal
        if (newStatus === 'interview' && app.status !== 'interview') {
          pendingInterviewApp = { appId, app };
          openInterviewModal(app);
          return;
        }

        try {
          await organiserApi.updateCandidateStatus(appId, newStatus);
          if (app) app.status = newStatus;
          renderKanban();
          showToast('Status updated!');
        } catch (e) {
          showToast(e.message || 'Update failed', 'error');
        }
      }

      function openInterviewModal(app) {
        // store only the id; resolve the app object fresh when needed
        pendingInterviewApp = { appId: app.id };
        document.getElementById('modalAppId').value = app.id;
        document.getElementById('modalCandidateName').textContent = app.name || 'Applicant';
        document.getElementById('modalModuleCode').value = app.role || '';
        document.getElementById('modalModuleName').value = '';
        // Set default date to tomorrow
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        document.getElementById('interviewDate').value = tomorrow.toISOString().split('T')[0];
        document.getElementById('interviewStart').value = '10:00';
        document.getElementById('interviewEnd').value = '10:30';
        document.getElementById('interviewType').value = 'video';
        document.getElementById('interviewLocation').value = '';
        document.getElementById('interviewLocation').disabled = true;
        document.getElementById('interviewLocation').placeholder = 'Location not needed for video call';
        document.getElementById('interviewModal').classList.remove('hidden');
      }

      function closeInterviewModal() {
        document.getElementById('interviewModal').classList.add('hidden');
        // only clear the pending booking info locally; do NOT alter any interviews or application statuses
        pendingInterviewApp = null;
      }

      function toggleLocation() {
        const type = document.getElementById('interviewType').value;
        const locationInput = document.getElementById('interviewLocation');
        if (type === 'video') {
          locationInput.disabled = true;
          locationInput.placeholder = 'Location not needed for video call';
          locationInput.value = '';
        } else {
          locationInput.disabled = false;
          locationInput.placeholder = 'e.g., Room 301, Building A';
        }
      }

      // Auto-set end time = start time + 30 min
      function syncInterviewEndTime() {
        const startVal = document.getElementById('interviewStart').value;
        const endInput = document.getElementById('interviewEnd');
        if (!startVal) return;
        const [h, m] = startVal.split(':').map(Number);
        let total = h * 60 + m + 30;
        if (total >= 24 * 60) total = 23 * 60 + 59;
        const nh = Math.floor(total / 60);
        const nm = total % 60;
        endInput.value = String(nh).padStart(2, '0') + ':' + String(nm).padStart(2, '0');
      }

      async function submitInterview(e) {
        e.preventDefault();
        if (!pendingInterviewApp) return;

        const { appId } = pendingInterviewApp;
        const app = allApps.find(a => a.id === appId);
        const startTime = document.getElementById('interviewStart').value;
        const endTime = document.getElementById('interviewEnd').value;

        const errDiv = document.getElementById('schedError');
        if (errDiv) {
          errDiv.classList.add('hidden');
        }

        // Validate end time is after start time
        if (startTime && endTime && startTime >= endTime) {
          showToast('End time must be after start time', 'error');
          return;
        }

        const data = {
          applicationId: appId,
          candidateName: app ? app.name : undefined,
          moduleCode: document.getElementById('modalModuleCode').value,
          date: document.getElementById('interviewDate').value,
          startTime: startTime,
          endTime: endTime,
          type: document.getElementById('interviewType').value,
          location: document.getElementById('interviewLocation').value
        };

        try {
          const previousStatus = app ? app.status : null;
          const interview = await organiserApi.createInterview(data);
          if (app) {
            app.status = 'interview';
            app.interview = interview;
          }
          if (app && previousStatus !== 'interview') {
            await organiserApi.updateCandidateStatus(appId, 'interview');
          }
          closeInterviewModal();
          renderKanban();
          showToast('Interview scheduled & candidate notified!');
          // clear pending after success
          pendingInterviewApp = null;
        } catch (err) {
          showToast(err.message || 'Failed to schedule interview', 'error');
        }
      }

      function showCandidate(id) {
        const original = allApps.find(a => a.id === id);
        const app=original?escapeDisplay(original):null;
        if (!app) return;
        const panel = document.getElementById('panelContent');
        let interviewInfo = '';
        if (app.interview) {
          interviewInfo = '<div class="p-4 rounded-xl bg-amber-50 border border-amber-100"><h4 class="text-sm font-semibold text-zinc-900 mb-2">Interview Scheduled</h4>' +
            '<p class="text-sm text-zinc-600"><i data-lucide="calendar" class="h-4 w-4 inline mr-1"></i>' + (app.interview.date || '') + ' at ' + (app.interview.startTime || '') + '</p>' +
            '<p class="text-sm text-zinc-600"><i data-lucide="' + (app.interview.type === 'video' ? 'video' : 'user') + '" class="h-4 w-4 inline mr-1"></i>' + (app.interview.location || app.interview.type || '') + '</p></div>';
        }
        panel.innerHTML = '<div class="flex items-center gap-4">' +
          '<div class="h-16 w-16 rounded-full bg-zinc-100 flex items-center justify-center text-xl font-bold text-zinc-700">' + ((app.name || 'A').charAt(0)) + '</div>' +
          '<div class="flex-1"><h3 class="text-xl font-bold text-zinc-900">' + (app.name || 'Applicant') + '</h3>' +
          '<p class="text-sm text-zinc-500">Application ID: ' + (app.id || '-') + '</p>' +
          '<p class="text-sm text-zinc-500">Applied for ' + (app.role || '') + '</p>' +
          '<span class="badge badge-neutral mt-2 capitalize">' + app.status + '</span></div></div>' +
          '<div class="grid grid-cols-2 gap-4">' +
          '<div class="p-4 rounded-xl bg-zinc-50 border border-zinc-100"><p class="text-xs text-zinc-500 mb-1">Applied On</p><p class="text-lg font-bold text-zinc-900">' + formatDate(app.date) + '</p></div>' +
          '<div class="p-4 rounded-xl bg-zinc-50 border border-zinc-100"><p class="text-xs text-zinc-500 mb-1">Status</p><p class="text-lg font-bold text-zinc-900 capitalize">' + app.status + '</p></div>' +
          '</div>' +
          interviewInfo +
          (app.coverLetter ? '<div><h4 class="text-sm font-semibold text-zinc-900 mb-2">Cover Letter</h4><p class="text-sm text-zinc-600 leading-relaxed">' + app.coverLetter.substring(0, 200) + '...</p></div>' : '');

        const matching = document.createElement('section');
        SectionMatching.render(matching, app.sectionMatching); panel.appendChild(matching);
        document.getElementById('viewFullApp').href = ctx + '/organiser/application-detail.jsp?id=' + app.id;
        document.getElementById('candidatePanel').classList.remove('hidden');
        lucide.createIcons();
      }

      function closePanel() { document.getElementById('candidatePanel').classList.add('hidden'); }

      let currentAppId = null;
      let currentInterviewId = null;
      function openSchedule(appId) {
        currentAppId = appId;
        const app = allApps.find(a => a.id === appId);
        currentInterviewId = app && app.interview ? app.interview.id : null;
        document.getElementById('iAppId').value = appId || '';
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        document.getElementById('iDate').value = app && app.interview ? app.interview.date || tomorrow.toISOString().split('T')[0] : tomorrow.toISOString().split('T')[0];
        document.getElementById('iTime').value = app && app.interview ? app.interview.startTime || '10:00' : '10:00';
        document.getElementById('iLocation').value = app && app.interview ? app.interview.location || '' : '';
        document.getElementById('iType').value = app && app.interview ? app.interview.type || 'video' : 'video';
        toggleScheduleLocation();
        document.getElementById('scheduleModal').classList.remove('hidden');
      }
      function closeSchedule() {
        document.getElementById('scheduleModal').classList.add('hidden');
        currentAppId = null;
        currentInterviewId = null;
      }

      function toggleScheduleLocation() {
        const type = document.getElementById('iType').value;
        const locationInput = document.getElementById('iLocation');
        if (type === 'video') {
          locationInput.disabled = true;
          locationInput.placeholder = 'Location not needed for video call';
          locationInput.value = '';
        } else {
          locationInput.disabled = false;
          locationInput.placeholder = 'Room 301, Building A';
        }
      }

      async function saveInterview() {
        const errDiv = document.getElementById('schedError');
        errDiv.classList.add('hidden');
        const payload = {
          applicationId: currentAppId,
          date: document.getElementById('iDate').value,
          startTime: document.getElementById('iTime').value,
          location: document.getElementById('iLocation').value.trim(),
          type: document.getElementById('iType').value,
        };
        try {
          let interview;
          if (currentInterviewId) {
            interview = await organiserApi.updateInterview(currentInterviewId, payload);
            showToast('Interview updated!');
          } else {
            interview = await organiserApi.createInterview(payload);
            if (currentAppId) {
              await organiserApi.updateCandidateStatus(currentAppId, 'interview');
              const app = allApps.find(a => a.id === currentAppId);
              if (app) {
                app.status = 'interview';
                app.interview = interview;
              }
            }
            showToast('Interview scheduled!');
          }
          currentInterviewId = interview.id || currentInterviewId;
          closeSchedule();
          loadPipeline(); // Reload to get interview information
        } catch (e) {
          errDiv.textContent = e.message || 'Schedule failed';
          errDiv.classList.remove('hidden');
        }
      }

      organiserApi.getApplicationPeriods().then(periods => {
        const select = document.getElementById('pipelinePeriod');
        periods.forEach(period => {
          const option = document.createElement('option');
          option.value = period.id; option.textContent = period.name; select.appendChild(option);
        });
      }).catch(error => showToast(error.message || 'Could not load application periods.', 'error'));
      loadPipeline();
    </script>