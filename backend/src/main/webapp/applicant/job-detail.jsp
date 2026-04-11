<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/auth-check.jsp" %>
<jsp:include page="/common/layout-top.jsp"/>

<div class="max-w-4xl mx-auto space-y-8" id="detailPage">
  <a href="<%= request.getContextPath() %>/applicant/jobs.jsp" class="inline-flex items-center text-sm font-medium text-zinc-500 hover:text-zinc-900 mb-2">
    <i data-lucide="chevron-left" class="mr-1 h-4 w-4"></i> Back to jobs
  </a>
  <div class="text-center text-zinc-400 py-12" id="loading">Loading job details...</div>
</div>

<!-- Apply Modal -->
<div id="applyModal" data-modal class="fixed inset-0 z-50 items-center justify-center bg-black/50 backdrop-blur-sm">
  <div class="bg-white rounded-2xl shadow-xl max-w-lg w-full mx-4 overflow-hidden">
    <div class="bg-zinc-50 border-b border-zinc-200/60 p-6">
      <h2 class="text-xl font-bold text-zinc-900" id="applyTitle">Apply for TA Position</h2>
      <p class="text-zinc-500 mt-1 text-sm">Provide your cover letter to complete the application.</p>
    </div>
    <div class="p-6 space-y-4">
      <div>
        <label class="block text-sm font-medium text-zinc-700 mb-1.5">Cover Letter</label>
        <textarea id="coverLetter" rows="6" class="input resize-none" placeholder="Explain why you're a great fit for this position..."></textarea>
      </div>
      <div id="applyError" class="hidden text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-4 py-3"></div>
    </div>
    <div class="p-6 border-t border-zinc-200 flex justify-end gap-3">
      <button onclick="closeApply()" class="btn btn-secondary">Cancel</button>
      <button onclick="submitApply()" class="btn btn-primary gap-2" id="applyBtn">
        <i data-lucide="check-circle-2" class="h-4 w-4"></i> Submit Application
      </button>
    </div>
  </div>
</div>

<jsp:include page="/common/layout-bottom.jsp"/>
<script>
const ctx = document.querySelector('meta[name="ctx"]').content;
const params = new URLSearchParams(window.location.search);
const jobId = params.get('id');
let jobData = null;

async function loadJobDetail() {
  if (!jobId) { document.getElementById('loading').textContent = 'No job ID provided.'; return; }
  try {
    const data = await applicantApi.getJobById(jobId);
    jobData = data.job || data;
    renderDetail(jobData);
  } catch (e) {
    document.getElementById('loading').textContent = 'Failed to load job details: ' + (e.message || '');
  }
}

function renderDetail(job) {
  const page = document.getElementById('detailPage');
  const backLink = page.querySelector('a');
  page.innerHTML = '';
  page.appendChild(backLink);

  const html = '<div class="bg-white rounded-2xl border border-zinc-200 shadow-sm overflow-hidden">' +
    '<div class="p-8 sm:p-10 border-b border-zinc-200 bg-gradient-to-br from-zinc-50 to-white">' +
      '<div class="flex flex-col sm:flex-row justify-between items-start gap-6">' +
        '<div class="space-y-4">' +
          '<div class="flex items-center gap-3">' +
            '<span class="badge badge-neutral">' + (job.moduleCode || '') + '</span>' +
            '<span class="badge badge-success"><i data-lucide="sparkles" class="h-3 w-3 inline"></i> ' + (job.status || 'active') + '</span>' +
          '</div>' +
          '<h1 class="text-3xl font-bold text-zinc-900 tracking-tight">' + job.title + '</h1>' +
          '<div class="flex flex-wrap items-center gap-4 text-sm text-zinc-500">' +
            '<span class="flex items-center gap-1.5"><i data-lucide="users" class="h-4 w-4"></i> ' + (job.department || '') + '</span>' +
            '<span class="flex items-center gap-1.5"><i data-lucide="clock" class="h-4 w-4"></i> ' + (job.hoursPerWeek || '-') + ' hrs/week</span>' +
            '<span class="flex items-center gap-1.5"><i data-lucide="calendar" class="h-4 w-4"></i> Closes ' + (job.deadline || 'TBD') + '</span>' +
          '</div>' +
        '</div>' +
        '<div class="flex flex-col items-start sm:items-end gap-3 w-full sm:w-auto">' +
          (job.status === 'active' ? '<button onclick="openApply()" class="btn btn-primary text-base px-6 py-3 w-full sm:w-auto">Apply Now</button>' : '<span class="badge badge-danger">Closed</span>') +
        '</div>' +
      '</div>' +
    '</div>' +
    '<div class="p-8 sm:p-10 grid grid-cols-1 md:grid-cols-3 gap-10">' +
      '<div class="md:col-span-2 space-y-8">' +
        '<section><h3 class="text-lg font-bold text-zinc-900 mb-4">Description</h3>' +
          '<p class="text-zinc-600 leading-relaxed">' + (job.description || 'No description provided.') + '</p></section>' +
        (job.requirements && job.requirements.length ? '<section><h3 class="text-lg font-bold text-zinc-900 mb-4">Requirements</h3><ul class="space-y-3">' +
          job.requirements.map(r => '<li class="flex items-start gap-3 text-zinc-600"><i data-lucide="check-circle-2" class="h-5 w-5 shrink-0 text-emerald-500 mt-0.5"></i><span>' + r + '</span></li>').join('') +
        '</ul></section>' : '') +
      '</div>' +
      '<div class="space-y-6">' +
        '<div class="bg-zinc-50 rounded-xl p-6 border border-zinc-200/60">' +
          '<h4 class="font-semibold text-zinc-900 mb-4">Job Overview</h4>' +
          '<div class="space-y-4">' +
            '<div><p class="text-sm text-zinc-500 mb-1">Positions</p><p class="font-medium text-zinc-900">' + (job.positions || '-') + '</p></div>' +
            '<div><p class="text-sm text-zinc-500 mb-1">Type</p><p class="font-medium text-zinc-900">' + (job.type || 'Part-time') + '</p></div>' +
            '<div><p class="text-sm text-zinc-500 mb-1">Department</p><p class="font-medium text-zinc-900">' + (job.department || '-') + '</p></div>' +
          '</div>' +
        '</div>' +
        '<div class="bg-blue-50/50 rounded-xl p-6 border border-blue-100">' +
          '<div class="flex items-start gap-3"><i data-lucide="alert-circle" class="h-5 w-5 text-blue-500 shrink-0 mt-0.5"></i>' +
          '<p class="text-sm text-blue-800">Applications are reviewed on a rolling basis. Early application is encouraged.</p></div>' +
        '</div>' +
      '</div>' +
    '</div></div>';

  page.insertAdjacentHTML('beforeend', html);
  document.getElementById('applyTitle').textContent = 'Apply for ' + (job.moduleCode || '') + ' TA';
  lucide.createIcons();
}

function openApply() { document.getElementById('applyModal').classList.add('show'); }
function closeApply() { document.getElementById('applyModal').classList.remove('show'); }

async function submitApply() {
  const errDiv = document.getElementById('applyError');
  errDiv.classList.add('hidden');
  const cl = document.getElementById('coverLetter').value.trim();
  if (!cl) { errDiv.textContent = 'Cover letter is required.'; errDiv.classList.remove('hidden'); return; }

  try {
    await applicantApi.applyForJob(jobId, cl, []);
    closeApply();
    showToast('Application submitted successfully!');
    setTimeout(() => window.location.href = ctx + '/applicant/applications.jsp', 1500);
  } catch (e) {
    errDiv.textContent = e.message || 'Application failed';
    errDiv.classList.remove('hidden');
  }
}

loadJobDetail();
</script>
