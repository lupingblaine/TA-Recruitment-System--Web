<%@ page pageEncoding="UTF-8" %>
  <%@ include file="/common/auth-check.jsp" %>
    <jsp:include page="/common/layout-top.jsp" />

    <div class="space-y-8 max-w-6xl mx-auto pb-12" id="detailPage">
      <div class="text-center text-zinc-400 py-12">Loading application details...</div>
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
          const selection=document.createElement('section');TeacherSelection.render(selection,app);document.getElementById('detailPage').appendChild(selection);
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
        page.innerHTML =
          '<div class="flex items-center gap-4">' +
          '<a href="' + ctx + '/organiser/pipeline.jsp" class="btn btn-ghost p-2 shrink-0"><i data-lucide="arrow-left" class="h-5 w-5"></i></a>' +
          '<div class="flex-1">' +
          '<h1 class="text-2xl font-bold tracking-tight text-zinc-900 flex items-center gap-3">' +
          (app.applicantName || 'Applicant') + '\'s Application' +
          ' <span class="badge badge-neutral capitalize">' + app.status + '</span></h1>' +
          '<p class="text-zinc-500 mt-1">Applying for ' + (app.moduleCode || '') + ' - ' + (app.jobTitle || '') + '</p>' +
          (app.status === 'shortlisted' ? '<p class="text-sm text-zinc-500 mt-2">This candidate is shortlisted. Continue approval in Review Workspace.</p>' : '') +
          '</div>' +
          '<div class="flex items-center gap-3 shrink-0">' +
          '<a href="' + ctx + '/review/dashboard.jsp" class="btn btn-primary gap-2">Review Workspace</a>' +
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
          ((app.canApprove === true || app.canRecommend === true) && ['applied', 'review'].includes(app.status) ? '<button onclick="doAction(\'shortlist\')" class="btn w-full justify-start gap-2 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 border border-emerald-200"><i data-lucide="check-circle-2" class="h-4 w-4"></i> Shortlist</button>' : '') +
          '<a href="' + ctx + '/review/dashboard.jsp" class="btn btn-outline w-full justify-start gap-2">Review Workspace</a>' +
          (app.canApprove === true ? '<button onclick="doAction(\'reject\')" class="btn w-full justify-start gap-2 bg-red-50 text-red-700 hover:bg-red-100 border border-red-200"><i data-lucide="x-circle" class="h-4 w-4"></i> Reject</button>' : '') +
          '</div></div>' +
          '</div>' +
          '</div>';
        lucide.createIcons();
      }

      async function doAction(action) {
        if (!currentApp) return;
        const allowed = action === 'reject' ? currentApp.canApprove === true :
          action === 'shortlist' && (currentApp.canApprove === true || currentApp.canRecommend === true);
        if (!allowed) { showToast('You do not have permission for this action.', 'error'); return; }
        let note;
        if (action === 'reject') {
          const reason = prompt('Enter the decision reason for rejection (10–4000 characters):');
          if (reason === null) return;
          note = reason.trim();
          if (note.length < 10 || note.length > 4000) { showToast('A decision reason must contain 10–4000 characters.', 'error'); return; }
        }
        try {
          await organiserApi.applicationAction(appId, action, note);
          showToast('Action applied: ' + action);
          loadDetail();
        } catch (e) {
          showToast(e.message || 'Action failed', 'error');
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