<%@ page pageEncoding="UTF-8" %>
  <%@ include file="/common/auth-check.jsp" %>
    <jsp:include page="/common/layout-top.jsp" />

    <div class="space-y-8 max-w-6xl mx-auto pb-12" id="detailPage">
      <div class="text-center text-zinc-400 py-12">Loading application details...</div>
    </div>

    <!-- Interview Schedule Modal (pre-filled from application) -->
    <div id="scheduleModal" class="fixed inset-0 z-50 hidden">
      <div class="absolute inset-0 bg-zinc-900/50 backdrop-blur-sm" onclick="closeScheduleModal()"></div>
      <div class="absolute inset-0 flex items-center justify-center p-4">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md">
          <div class="p-6 border-b border-zinc-200">
            <h2 class="text-lg font-bold text-zinc-900" id="schedModalTitle">Schedule Interview</h2>
            <p class="text-sm text-zinc-500 mt-1" id="schedCandidateInfo"></p>
          </div>
          <form id="scheduleForm" class="p-6 space-y-4" onsubmit="submitSchedule(event)">
            <input type="hidden" id="schedAppId">
            <input type="hidden" id="schedModuleCode">
            <div>
              <label class="block text-sm font-medium text-zinc-700 mb-1">Date *</label>
              <input type="date" id="schedDate" required class="input w-full">
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-zinc-700 mb-1">Start Time *</label>
                <input type="time" id="schedStart" required class="input w-full" onchange="syncEndTime()">
              </div>
              <div>
                <label class="block text-sm font-medium text-zinc-700 mb-1">End Time</label>
                <input type="time" id="schedEnd" class="input w-full">
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-zinc-700 mb-1">Interview Type *</label>
              <select id="schedType" required class="input w-full" onchange="toggleSchedLocation()">
                <option value="video">Video Call</option>
                <option value="in-person">In-Person</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-zinc-700 mb-1">Location</label>
              <input type="text" id="schedLocation" placeholder="Location not needed for video call"
                class="input w-full" disabled>
            </div>
            <div id="schedError"
              class="hidden text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-4 py-3"></div>
            <div class="flex gap-3 pt-4">
              <button type="button" onclick="closeScheduleModal()" class="btn btn-outline flex-1">Cancel</button>
              <button type="submit" class="btn btn-primary flex-1">Schedule</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Document Preview Modal -->
    <div id="previewModal" class="fixed inset-0 z-[60] hidden">
      <div class="absolute inset-0 bg-zinc-900/60 backdrop-blur-sm" onclick="closePreview()"></div>
      <div class="absolute inset-4 md:inset-8 flex flex-col bg-white rounded-2xl shadow-2xl overflow-hidden">
        <div class="flex items-center justify-between p-4 border-b border-zinc-200 shrink-0">
          <h3 class="font-semibold text-zinc-900 truncate" id="previewTitle"></h3>
          <div class="flex items-center gap-2">
            <a id="previewDownload" class="btn btn-outline text-sm gap-2"><i data-lucide="download" class="h-4 w-4"></i> Download</a>
            <button onclick="closePreview()" class="btn btn-ghost p-2"><i data-lucide="x" class="h-5 w-5"></i></button>
          </div>
        </div>
        <div class="flex-1 p-4 overflow-auto" id="previewBody"></div>
      </div>
    </div>

    <jsp:include page="/common/layout-bottom.jsp" />
<script src="<%= request.getContextPath() %>/static/js/availability-view.js"></script>
<script src="<%= request.getContextPath() %>/static/js/section-matching.js"></script>
<script src="<%= request.getContextPath() %>/static/js/qualification-view.js"></script>
<script src="<%= request.getContextPath() %>/static/js/criteria-view.js"></script>
    <script>
      const ctx = document.querySelector('meta[name="ctx"]').content;
      const appId = new URLSearchParams(window.location.search).get('id');
      let currentApp = null; // store the full application data

      async function loadDetail() {
        if (!appId) { document.getElementById('detailPage').innerHTML = '<div class="text-center text-zinc-400 py-12">No application ID</div>'; return; }
        try {
          const data = await organiserApi.getApplicationDetails(appId);
          const app = data.application || data;
          currentApp = app;
          renderDetail(escapeDisplay(app));
          const qualifications=document.createElement('section');qualifications.className='card p-6';
          QualificationView.render(qualifications,app.qualificationSnapshot,app.id);document.getElementById('detailPage').appendChild(qualifications);
          const criteria=document.createElement('section');criteria.className='card p-6';CriteriaView.render(criteria,app.qualificationMatching);document.getElementById('detailPage').appendChild(criteria);
          const matching = document.createElement('section');
          SectionMatching.render(matching, app.sectionMatching);
          document.getElementById('detailPage').appendChild(matching);
          const section = document.createElement('section'); section.className = 'card p-6 space-y-3';
          const title = document.createElement('h2'); title.className = 'text-lg font-bold'; title.textContent = 'Weekly availability · submitted snapshot'; section.appendChild(title);
          const snapshot = document.createElement('div'); AvailabilityView.render(snapshot, app.availabilitySnapshot); section.appendChild(snapshot);
          document.getElementById('detailPage').appendChild(section);
        } catch (e) {
          document.getElementById('detailPage').innerHTML = '<div class="text-center text-red-500 py-12">Failed to load: ' + (e.message || '') + '</div>';
        }
      }

      function renderDetail(app) {
        // Build documents HTML
        const docs = app.documents || [];
        let docsHtml = '';
        if (docs.length === 0) {
          docsHtml = '<div class="h-32 border-2 border-dashed border-zinc-200 rounded-xl flex flex-col items-center justify-center bg-zinc-50 text-zinc-500">' +
            '<i data-lucide="file-text" class="h-8 w-8 text-zinc-300 mb-2"></i><p class="text-sm">No documents uploaded</p></div>';
        } else {
          docsHtml = '<div class="divide-y divide-zinc-100 border border-zinc-200 rounded-xl overflow-hidden">' +
            docs.map(doc => {
              const icon = doc.type === 'cv' ? 'file-text' : 'file';
              const typeLabel = doc.type === 'cv' ? 'CV' : (doc.type === 'transcript' ? 'Transcript' : 'Document');
              return '<div class="flex items-center justify-between p-4 hover:bg-zinc-50 transition-colors">' +
                '<div class="flex items-center gap-3">' +
                '<div class="h-10 w-10 rounded-lg bg-blue-50 flex items-center justify-center">' +
                '<i data-lucide="' + icon + '" class="h-5 w-5 text-blue-600"></i></div>' +
                '<div>' +
                '<p class="text-sm font-medium text-zinc-900">' + (doc.name || 'Untitled') + '</p>' +
                '<p class="text-xs text-zinc-500">' + typeLabel + (doc.size ? ' · ' + doc.size : '') + '</p>' +
                '</div>' +
                '</div>' +
                '<div class="flex items-center gap-2">' +
                '<button onclick="previewDocument(\'' + encodeURIComponent(doc.name || '') + '\',\''+encodeURIComponent(doc.id)+'\')" class="btn btn-ghost p-2 text-blue-600 hover:bg-blue-50" title="Preview"><i data-lucide="eye" class="h-4 w-4"></i></button>' +
                '<a href="' + ctx + '/api/documents/' + encodeURIComponent(doc.id) + '?applicationId='+encodeURIComponent(appId) + '" download class="btn btn-ghost p-2 text-blue-600 hover:bg-blue-50" title="Download"><i data-lucide="download" class="h-4 w-4"></i></a>' +
                '</div></div>';
            }).join('') + '</div>';
        }

        const profile = app.applicantProfile || {};
        let profileDetailHtml = '';
        const pFields = [
          {label: 'Email', val: profile.email},
          {label: 'Phone', val: profile.phone},
          {label: 'Department', val: profile.department},
          {label: 'Student ID', val: profile.studentId},
          {label: 'Degree', val: profile.degree},
          {label: 'Year', val: profile.year},
          {label: 'GPA', val: profile.gpa ? (profile.gpa + ' / ' + (profile.maxGpa || '4.0')) : null},
        ];
        pFields.forEach(f => {
          if (f.val) profileDetailHtml += '<div class="flex justify-between py-1.5 border-b border-zinc-100 last:border-0"><span class="text-xs text-zinc-500">' + f.label + '</span><span class="text-xs font-medium text-zinc-800">' + f.val + '</span></div>';
        });

        const pSkills = profile.skills || [];
        let skillsHtml = '';
        if (pSkills.length > 0) {
          skillsHtml = '<div class="mt-3"><p class="text-xs text-zinc-500 mb-1.5">Skills</p><div class="flex flex-wrap gap-1">' +
            pSkills.map(s => '<span class="px-2 py-0.5 bg-zinc-100 text-zinc-700 rounded-full text-xs border border-zinc-200">' + s + '</span>').join('') + '</div></div>';
        }

        const pExps = profile.experience || [];
        let expHtml = '';
        if (pExps.length > 0) {
          expHtml = '<div class="mt-3"><p class="text-xs text-zinc-500 mb-1.5">Experience</p>' +
            pExps.map(e => '<div class="py-1.5 border-b border-zinc-100 last:border-0"><p class="text-xs font-medium text-zinc-800">' + (e.role || '') + '</p>' +
              '<p class="text-xs text-zinc-500">' + (e.company || '') + (e.term ? ' · ' + e.term : '') + '</p>' +
              (e.description ? '<p class="text-xs text-zinc-400 mt-0.5">' + e.description + '</p>' : '') +
            '</div>').join('') + '</div>';
        }

        const page = document.getElementById('detailPage');
        const canSchedule = (app.status === 'shortlisted' || app.status === 'interview');
        const scheduleLabel = app.status === 'interview' ? 'Modify Interview Time' : 'Schedule Interview';

        page.innerHTML =
          '<div class="flex items-center gap-4">' +
          '<a href="' + ctx + '/organiser/pipeline.jsp" class="btn btn-ghost p-2 shrink-0"><i data-lucide="arrow-left" class="h-5 w-5"></i></a>' +
          '<div class="flex-1">' +
          '<h1 class="text-2xl font-bold tracking-tight text-zinc-900 flex items-center gap-3">' +
          (app.applicantName || 'Applicant') + '\'s Application' +
          ' <span class="badge badge-neutral capitalize">' + app.status + '</span></h1>' +
          '<p class="text-zinc-500 mt-1">Applying for ' + (app.moduleCode || '') + ' - ' + (app.jobTitle || '') + '</p>' +
          (app.status === 'interview'
            ? '<p class="text-sm text-amber-600 mt-2">This candidate is now in the interview stage. You can modify the interview schedule or reject the candidate.</p>'
            : (app.status === 'shortlisted' ? '<p class="text-sm text-amber-600 mt-2">This candidate is shortlisted. Next step: schedule their interview.</p>' : '')) +
          (app.interview ? '<p class="text-sm text-zinc-500 mt-2">Interview on ' + (app.interview.date || 'TBD') + ' at ' + (app.interview.startTime || 'TBD') + ' (' + (app.interview.type === 'video' ? 'Video Call' : 'In-Person') + ')</p>' : '') +
          '</div>' +
          '<div class="flex items-center gap-3 shrink-0">' +
          (canSchedule
            ? '<button onclick="openScheduleModal()" class="btn btn-primary gap-2"><i data-lucide="calendar" class="h-4 w-4"></i> ' + scheduleLabel + '</button>'
            : '<button onclick="doAction(\'shortlist\')" class="btn btn-primary gap-2"><i data-lucide="check-circle-2" class="h-4 w-4"></i> Shortlist</button>') +
          '</div>' +
          '</div>' +
          '<div class="grid grid-cols-1 lg:grid-cols-3 gap-8">' +
          '<div class="lg:col-span-2 space-y-6">' +
          '<div class="card p-6"><h2 class="text-lg font-bold text-zinc-900 mb-4 flex items-center gap-2"><i data-lucide="file-text" class="h-5 w-5 text-zinc-400"></i> Cover Letter</h2>' +
          '<div class="text-sm text-zinc-600 whitespace-pre-wrap leading-relaxed">' + (app.coverLetter || 'No cover letter provided.') + '</div></div>' +
          '<div class="card p-6"><h2 class="text-lg font-bold text-zinc-900 mb-4 flex items-center gap-2"><i data-lucide="folder-open" class="h-5 w-5 text-zinc-400"></i> Documents</h2>' +
          docsHtml + '</div>' +
          '</div>' +
          '<div class="space-y-6">' +
          '<div class="card p-6"><h3 class="font-bold text-zinc-900 mb-4">Applicant Profile</h3>' +
          '<div class="flex items-center gap-4 mb-4">' +
          '<div class="h-14 w-14 rounded-full bg-zinc-100 flex items-center justify-center text-xl font-bold text-zinc-700">' + ((app.applicantName || 'A').charAt(0)) + '</div>' +
          '<div><p class="font-semibold text-zinc-900">' + (app.applicantName || '') + '</p><p class="text-sm text-zinc-500">' + (app.applicantEmail || '') + '</p></div>' +
          '</div>' +
          '<div class="divide-y-0">' + profileDetailHtml + '</div>' +
          skillsHtml + expHtml +
          '</div>' +
          '<div class="card p-6"><h3 class="font-bold text-zinc-900 mb-4">Application Actions</h3><div class="space-y-3">' +
          (canSchedule
            ? '<button onclick="openScheduleModal()" class="btn w-full justify-start gap-2 bg-blue-50 text-blue-700 hover:bg-blue-100 border border-blue-200"><i data-lucide="calendar" class="h-4 w-4"></i> ' + scheduleLabel + '</button>'
            : '<button onclick="doAction(\'shortlist\')" class="btn w-full justify-start gap-2 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 border border-emerald-200"><i data-lucide="check-circle-2" class="h-4 w-4"></i> Shortlist</button>') +
          '<button onclick="doAction(\'reject\')" class="btn w-full justify-start gap-2 bg-red-50 text-red-700 hover:bg-red-100 border border-red-200"><i data-lucide="x-circle" class="h-4 w-4"></i> Reject</button>' +
          '</div></div>' +
          '</div>' +
          '</div>';
        lucide.createIcons();
      }

      async function doAction(action) {
        try {
          await organiserApi.applicationAction(appId, action);
          showToast('Action applied: ' + action);
          loadDetail();
        } catch (e) {
          showToast(e.message || 'Action failed', 'error');
        }
      }

      // ---- Interview Schedule Modal ----
      function openScheduleModal() {
        if (!currentApp) return;
        const app = currentApp;
        document.getElementById('schedAppId').value = appId;
        document.getElementById('schedModuleCode').value = app.moduleCode || '';
        document.getElementById('schedModalTitle').textContent = app.status === 'interview' ? 'Modify Interview' : 'Schedule Interview';
        document.getElementById('schedCandidateInfo').textContent = 'Candidate: ' + (app.applicantName || 'Applicant');

        // Pre-fill if there's an existing interview
        if (app.interview) {
          document.getElementById('schedDate').value = app.interview.date || '';
          document.getElementById('schedStart').value = app.interview.startTime || '10:00';
          document.getElementById('schedEnd').value = app.interview.endTime || '';
          document.getElementById('schedType').value = app.interview.type === 'in-person' ? 'in-person' : 'video';
          document.getElementById('schedLocation').value = app.interview.location || '';
        } else {
          const tomorrow = new Date();
          tomorrow.setDate(tomorrow.getDate() + 1);
          document.getElementById('schedDate').value = tomorrow.toISOString().split('T')[0];
          document.getElementById('schedStart').value = '10:00';
          document.getElementById('schedEnd').value = '10:30';
          document.getElementById('schedType').value = 'video';
          document.getElementById('schedLocation').value = '';
        }
        toggleSchedLocation();
        document.getElementById('schedError').classList.add('hidden');
        document.getElementById('scheduleModal').classList.remove('hidden');
      }

      function closeScheduleModal() {
        document.getElementById('scheduleModal').classList.add('hidden');
      }

      function toggleSchedLocation() {
        const type = document.getElementById('schedType').value;
        const loc = document.getElementById('schedLocation');
        if (type === 'video') {
          loc.disabled = true;
          loc.placeholder = 'Location not needed for video call';
          loc.value = '';
        } else {
          loc.disabled = false;
          loc.placeholder = 'e.g., Room 301, Building A';
        }
      }

      // Auto-set end time = start time + 30 min
      function syncEndTime() {
        const startVal = document.getElementById('schedStart').value;
        const endInput = document.getElementById('schedEnd');
        if (!startVal) return;
        const [h, m] = startVal.split(':').map(Number);
        let total = h * 60 + m + 30;
        if (total >= 24 * 60) total = 23 * 60 + 59;
        const nh = Math.floor(total / 60);
        const nm = total % 60;
        endInput.value = String(nh).padStart(2, '0') + ':' + String(nm).padStart(2, '0');
      }

      async function submitSchedule(e) {
        e.preventDefault();
        const errDiv = document.getElementById('schedError');
        errDiv.classList.add('hidden');

        const startTime = document.getElementById('schedStart').value;
        const endTime = document.getElementById('schedEnd').value;

        // Validate end time is after start time
        if (startTime && endTime && startTime >= endTime) {
          errDiv.textContent = 'End time must be after start time';
          errDiv.classList.remove('hidden');
          return;
        }

        const payload = {
          applicationId: document.getElementById('schedAppId').value,
          candidateName: currentApp ? currentApp.applicantName : undefined,
          moduleCode: document.getElementById('schedModuleCode').value,
          date: document.getElementById('schedDate').value,
          startTime: startTime,
          endTime: endTime,
          type: document.getElementById('schedType').value,
          location: document.getElementById('schedLocation').value
        };

        try {
          await organiserApi.createInterview(payload);
          // If status is not interview yet, update it
          if (currentApp && currentApp.status !== 'interview') {
            await organiserApi.updateCandidateStatus(appId, 'interview');
          }
          closeScheduleModal();
          showToast('Interview scheduled!');
          loadDetail();
        } catch (err) {
          errDiv.textContent = err.message || 'Failed to schedule interview';
          errDiv.classList.remove('hidden');
        }
      }

      function previewDocument(encodedName,encodedId) {
        const rawDoc=(currentApp.documents||[]).find(d=>d.id===decodeURIComponent(encodedId));
        const name = rawDoc?rawDoc.name:decodeURIComponent(encodedName);
        const url = ctx + '/api/documents/' + encodedId + '?inline=1&applicationId='+encodeURIComponent(appId);
        const ext = name.split('.').pop().toLowerCase();
        const modal = document.getElementById('previewModal');
        const body = document.getElementById('previewBody');
        document.getElementById('previewTitle').textContent = name;
        document.getElementById('previewDownload').href = url;
        document.getElementById('previewDownload').download = name;

        if (ext === 'pdf') {
          body.innerHTML = '<iframe src="' + url + '" class="w-full h-full border-0 rounded-lg"></iframe>';
        } else if (['jpg','jpeg','png','gif','webp','svg'].includes(ext)) {
          body.innerHTML = '<div class="flex items-center justify-center h-full bg-zinc-50 rounded-lg"><img src="' + url + '" class="max-w-full max-h-full object-contain"></div>';
        } else if (ext === 'docx') {
          body.innerHTML = '<div class="flex items-center justify-center h-full"><div class="animate-spin h-8 w-8 border-4 border-zinc-300 border-t-zinc-700 rounded-full"></div></div>';
          fetch(url).then(r => r.arrayBuffer()).then(buf => {
            return mammoth.convertToHtml({arrayBuffer: buf});
          }).then(result => {
            body.innerHTML = '<div class="w-full h-full overflow-auto p-6 bg-white rounded-lg prose prose-sm max-w-none">' + sanitizeDocumentHtml(result.value) + '</div>';
          }).catch(() => {
            body.innerHTML = '<div class="flex flex-col items-center justify-center h-full text-zinc-500 bg-zinc-50 rounded-lg">' +
              '<i data-lucide="file-text" class="h-16 w-16 mb-4 text-zinc-300"></i>' +
              '<p class="text-lg font-medium mb-2">Failed to preview .docx file</p>' +
              '<p class="text-sm">Please download the file to view it.</p></div>';
            lucide.createIcons();
          });
        } else if (ext === 'doc') {
          body.innerHTML = '<div class="flex flex-col items-center justify-center h-full text-zinc-500 bg-zinc-50 rounded-lg">' +
            '<i data-lucide="file-text" class="h-16 w-16 mb-4 text-zinc-300"></i>' +
            '<p class="text-lg font-medium mb-2">.doc format is not supported for preview</p>' +
            '<p class="text-sm">Please download the file, or re-upload as .docx / .pdf for preview.</p></div>';
          lucide.createIcons();
        } else if (ext === 'txt' || ext === 'rtf') {
          fetch(url).then(r => r.text()).then(text => {
            body.innerHTML = '<pre class="w-full h-full overflow-auto p-6 bg-zinc-50 rounded-lg text-sm text-zinc-800 whitespace-pre-wrap">' +
              text.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;') + '</pre>';
          }).catch(() => {
            body.innerHTML = '<div class="flex flex-col items-center justify-center h-full text-zinc-500"><p>Failed to load file.</p></div>';
          });
        } else {
          body.innerHTML = '<div class="flex flex-col items-center justify-center h-full text-zinc-500 bg-zinc-50 rounded-lg">' +
            '<i data-lucide="file-text" class="h-16 w-16 mb-4 text-zinc-300"></i>' +
            '<p class="text-lg font-medium mb-2">Preview not available for .' + ext + ' files</p>' +
            '<p class="text-sm">Please download the file to view it.</p></div>';
          lucide.createIcons();
        }
        modal.classList.remove('hidden');
      }

      function closePreview() {
        document.getElementById('previewModal').classList.add('hidden');
        document.getElementById('previewBody').innerHTML = '';
      }

      loadDetail();
    </script>