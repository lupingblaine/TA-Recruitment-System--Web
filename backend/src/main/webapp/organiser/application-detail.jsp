<%@ page pageEncoding="UTF-8" %>
  <%@ include file="/common/auth-check.jsp" %>
    <jsp:include page="/common/layout-top.jsp" />

    <div class="space-y-8 max-w-6xl mx-auto pb-12" id="detailPage">
      <div class="text-center text-zinc-400 py-12">Loading application details...</div>
    </div>

    <jsp:include page="/common/layout-bottom.jsp" />
    <script>
      const ctx = document.querySelector('meta[name="ctx"]').content;
      const appId = new URLSearchParams(window.location.search).get('id');

      async function loadDetail() {
        if (!appId) { document.getElementById('detailPage').innerHTML = '<div class="text-center text-zinc-400 py-12">No application ID</div>'; return; }
        try {
          const data = await organiserApi.getApplicationDetails(appId);
          const app = data.application || data;
          renderDetail(app);
        } catch (e) {
          document.getElementById('detailPage').innerHTML = '<div class="text-center text-red-500 py-12">Failed to load: ' + (e.message || '') + '</div>';
        }
      }

      function renderDetail(app) {
        const page = document.getElementById('detailPage');
        page.innerHTML =
          '<div class="flex items-center gap-4">' +
          '<a href="' + ctx + '/organiser/pipeline.jsp" class="btn btn-ghost p-2 shrink-0"><i data-lucide="arrow-left" class="h-5 w-5"></i></a>' +
          '<div class="flex-1">' +
          '<h1 class="text-2xl font-bold tracking-tight text-zinc-900 flex items-center gap-3">' +
          (app.applicantName || 'Applicant') + '\'s Application' +
          ' <span class="badge badge-neutral capitalize">' + app.status + '</span></h1>' +
          '<p class="text-zinc-500 mt-1">Applying for ' + (app.moduleCode || '') + ' - ' + (app.jobTitle || '') + '</p>' +
          ((app.status === 'shortlisted' || app.status === 'interview') ? '<p class="text-sm text-amber-600 mt-2">This candidate is shortlisted. Next step: schedule their interview.</p>' : '') +
          '</div>' +
          '<div class="flex items-center gap-3 shrink-0">' +
          ((app.status === 'shortlisted' || app.status === 'interview')
            ? '<button onclick="navigateToSchedule()" class="btn btn-primary gap-2"><i data-lucide="calendar" class="h-4 w-4"></i> Schedule Interview</button>'
            : '<button onclick="doAction(\'shortlist\')" class="btn btn-primary gap-2"><i data-lucide="check-circle-2" class="h-4 w-4"></i> Shortlist</button>') +
          '</div>' +
          '</div>' +
          '<div class="grid grid-cols-1 lg:grid-cols-3 gap-8">' +
          '<div class="lg:col-span-2 space-y-6">' +
          '<div class="card p-6"><h2 class="text-lg font-bold text-zinc-900 mb-4 flex items-center gap-2"><i data-lucide="file-text" class="h-5 w-5 text-zinc-400"></i> Cover Letter</h2>' +
          '<div class="text-sm text-zinc-600 whitespace-pre-wrap leading-relaxed">' + (app.coverLetter || 'No cover letter provided.') + '</div></div>' +
          '<div class="card p-6"><h2 class="text-lg font-bold text-zinc-900 mb-4 flex items-center gap-2"><i data-lucide="file-text" class="h-5 w-5 text-zinc-400"></i> Documents</h2>' +
          '<div class="h-32 border-2 border-dashed border-zinc-200 rounded-xl flex flex-col items-center justify-center bg-zinc-50 text-zinc-500">' +
          '<i data-lucide="file-text" class="h-8 w-8 text-zinc-300 mb-2"></i><p class="text-sm">Documents available for download</p></div></div>' +
          '</div>' +
          '<div class="space-y-6">' +
          '<div class="card p-6"><h3 class="font-bold text-zinc-900 mb-4">Applicant Profile</h3>' +
          '<div class="flex items-center gap-4 mb-6">' +
          '<div class="h-14 w-14 rounded-full bg-zinc-100 flex items-center justify-center text-xl font-bold text-zinc-700">' + ((app.applicantName || 'A').charAt(0)) + '</div>' +
          '<div><p class="font-semibold text-zinc-900">' + (app.applicantName || '') + '</p><p class="text-sm text-zinc-500">' + (app.applicantEmail || '') + '</p></div>' +
          '</div></div>' +
          '<div class="card p-6"><h3 class="font-bold text-zinc-900 mb-4">Application Actions</h3><div class="space-y-3">' +
          ((app.status === 'shortlisted' || app.status === 'interview')
            ? '<button onclick="navigateToSchedule()" class="btn w-full justify-start gap-2 bg-blue-50 text-blue-700 hover:bg-blue-100 border border-blue-200"><i data-lucide="calendar" class="h-4 w-4"></i> Schedule Interview</button>'
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

      function navigateToSchedule() {
        window.location.href = ctx + '/organiser/interviews.jsp?applicationId=' + appId;
      }

      loadDetail();
    </script>