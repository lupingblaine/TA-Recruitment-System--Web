<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/auth-check.jsp" %>
<jsp:include page="/common/layout-top.jsp"/>
<jsp:include page="/common/application-period-banner.jsp"/>

<div class="max-w-4xl mx-auto space-y-8" id="detailPage">
  <a href="<%= request.getContextPath() %>/applicant/jobs.jsp" class="inline-flex items-center text-sm font-medium text-zinc-500 hover:text-zinc-900 mb-2">
    <i data-lucide="chevron-left" class="mr-1 h-4 w-4"></i> Back to jobs
  </a>
  <div class="text-center text-zinc-400 py-12" id="loading">Loading job details...</div>
</div>

<!-- Apply Modal (with pre-filled profile review) -->
<div id="applyModal" data-modal class="fixed inset-0 z-50 items-center justify-center bg-black/50 backdrop-blur-sm">
  <div class="bg-white rounded-2xl shadow-xl max-w-2xl w-full mx-4 overflow-hidden max-h-[90vh] flex flex-col">
    <div class="bg-zinc-50 border-b border-zinc-200/60 p-6 shrink-0">
      <h2 class="text-xl font-bold text-zinc-900" id="applyTitle">Apply for TA Position</h2>
      <p class="text-zinc-500 mt-1 text-sm">Review your information below. The MO will see everything listed here.</p>
    </div>
    <div class="p-6 space-y-5 overflow-y-auto flex-1">
      <!-- Profile Summary Table -->
      <div class="border border-zinc-200 rounded-xl overflow-hidden">
        <table class="w-full text-sm">
          <tbody>
            <tr class="border-b border-zinc-100"><td class="px-4 py-3 bg-zinc-50 font-medium text-zinc-600 w-32">Name</td><td class="px-4 py-3 text-zinc-900" id="applyName">-</td></tr>
            <tr class="border-b border-zinc-100"><td class="px-4 py-3 bg-zinc-50 font-medium text-zinc-600">Email</td><td class="px-4 py-3 text-zinc-900" id="applyEmail">-</td></tr>
            <tr class="border-b border-zinc-100"><td class="px-4 py-3 bg-zinc-50 font-medium text-zinc-600">Degree</td><td class="px-4 py-3 text-zinc-900" id="applyDegree">-</td></tr>
            <tr class="border-b border-zinc-100"><td class="px-4 py-3 bg-zinc-50 font-medium text-zinc-600">Year</td><td class="px-4 py-3 text-zinc-900" id="applyYear">-</td></tr>
            <tr class="border-b border-zinc-100"><td class="px-4 py-3 bg-zinc-50 font-medium text-zinc-600">GPA</td><td class="px-4 py-3 text-zinc-900" id="applyGpa">-</td></tr>
            <tr><td class="px-4 py-3 bg-zinc-50 font-medium text-zinc-600">Skills</td><td class="px-4 py-3" id="applySkills">-</td></tr>
          </tbody>
        </table>
      </div>
      <p class="text-xs text-zinc-400 flex items-center gap-1"><i data-lucide="info" class="h-3 w-3"></i> Update your profile details on the <a href="profile.jsp" class="text-blue-600 underline">Profile page</a>.</p>

      <!-- Document Selection -->
      <div>
        <label class="block text-sm font-bold text-zinc-800 mb-2">Attach Documents</label>
        <div id="applyDocList" class="space-y-2">
          <p class="text-sm text-zinc-400">Loading documents...</p>
        </div>
        <div class="mt-3">
          <label class="text-xs text-zinc-500 flex items-center gap-2 cursor-pointer hover:text-zinc-700">
            <input type="file" id="applyNewDoc" class="hidden" accept=".pdf,.doc,.docx,.txt,.rtf" onchange="uploadAndAttach(event)">
            <span class="btn btn-outline text-xs gap-1"><i data-lucide="upload" class="h-3 w-3"></i> Upload New</span>
          </label>
        </div>
      </div>

      <section><h3 class="font-semibold mb-2">Weekly availability for this application</h3><div id="applyAvailability" class="space-y-1"></div><a id="editApplyAvailability" href="availability.jsp" class="text-sm text-blue-600 underline">Manage availability before submitting</a></section>
      <!-- Cover Letter -->
      <div>
        <label class="block text-sm font-bold text-zinc-800 mb-1.5">Cover Letter *</label>
        <textarea id="coverLetter" rows="5" class="input resize-none" placeholder="Explain why you're a great fit for this position..."></textarea>
      </div>
      <div id="applyError" class="hidden text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-4 py-3"></div>
    </div>
    <div class="p-6 border-t border-zinc-200 flex justify-end gap-3 shrink-0">
      <button onclick="closeApply()" class="btn btn-secondary">Cancel</button>
      <button onclick="submitApply()" class="btn btn-primary gap-2" id="applyBtn">
        <i data-lucide="check-circle-2" class="h-4 w-4"></i> Submit Application
      </button>
    </div>
  </div>
</div>

<jsp:include page="/common/layout-bottom.jsp"/>
<script src="<%= request.getContextPath() %>/static/js/availability-view.js"></script>
<script>
const ctx = document.querySelector('meta[name="ctx"]').content;
const params = new URLSearchParams(window.location.search);
const jobId = params.get('id');
let jobData = null;
let applicationPeriodState = { accepting: false, period: null };
let myApplicationStatus = null; // null | 'applied' | 'interview' | 'offer' | 'hired' | 'rejected'

async function loadJobDetail() {
  if (!jobId) { document.getElementById('loading').textContent = 'No job ID provided.'; return; }
  try {
    const [jobResp, appsResp, periodResp] = await Promise.all([
      applicantApi.getJobById(jobId),
      applicantApi.getApplications().catch(() => []),
      applicantApi.getApplicationPeriod()
    ]);
    jobData = jobResp.job || jobResp;
    const apps = Array.isArray(appsResp) ? appsResp : (appsResp.applications || []);
    applicationPeriodState = periodResp;
    const myApp = apps.find(a => a.jobId === jobId && (!periodResp.period || a.periodId === periodResp.period.id));
    myApplicationStatus = myApp ? myApp.status : null;
    renderDetail(jobData);
  } catch (e) {
    document.getElementById('loading').textContent = 'Failed to load job details: ' + (e.message || '');
  }
}

function buildWorkScheduleSummaryHtml(job) {
  var sections = job.sections || [];
  if (!sections.length) return '<div class="rounded-xl p-6 border"><h4 class="font-semibold">Section Schedule</h4><p>Section schedules have not been configured. Please contact the course organiser.</p></div>';
  var days = ['', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
  return '<div class="bg-emerald-50/50 rounded-xl p-6 border border-emerald-100"><h4 class="font-semibold mb-3">Sections &amp; Weekly Schedule</h4><p class="text-sm mb-3">America/Los_Angeles (UCI local time). This posting covers these Sections; your individual assignments will be confirmed separately.</p>' + sections.map(function(section) {
    return '<div class="border-t py-3"><strong>' + section.name + '</strong> (' + section.type + ')<p class="text-sm">' + (section.meetingTimes || []).map(function(m) { return days[m.dayOfWeek] + ' ' + m.startTime + '–' + m.endTime; }).join('; ') + '</p></div>';
  }).join('') + '</div>';
}

function postingAcceptsCycle(job) {
  return !Array.isArray(job.sectionIds) || (job.sections || []).some(section => section.periodId === applicationPeriodState.period?.id);
}

function renderDetail(original) {
  const job=escapeDisplay(original);
  const page = document.getElementById('detailPage');
  const backLink = page.querySelector('a');
  page.innerHTML = '';
  page.appendChild(backLink);

  var isHired = (myApplicationStatus === 'offer' || myApplicationStatus === 'hired');
  var hasApplied = myApplicationStatus != null;

  var actionHtml = '';
  if (isHired) {
    actionHtml = '<div class="relative"><div class="border-4 border-emerald-500 rounded-xl px-6 py-4 text-center transform rotate-[-6deg] shadow-md bg-white/90">' +
      '<p class="text-emerald-600 font-black text-lg tracking-wide uppercase">Position Obtained</p>' +
      '<p class="text-emerald-500 text-xs font-medium mt-0.5">Congratulations!</p></div></div>';
  } else if (hasApplied) {
    var statusBadge = myApplicationStatus === 'rejected' ? 'badge-danger' : myApplicationStatus === 'interview' ? 'badge-warning' : 'badge-neutral';
    actionHtml = '<span class="badge ' + statusBadge + ' text-sm px-4 py-2">' + myApplicationStatus.charAt(0).toUpperCase() + myApplicationStatus.slice(1) + '</span>';
  } else if (job.status === 'active' && applicationPeriodState.accepting && postingAcceptsCycle(job)) {
    actionHtml = '<button onclick="openApply()" class="btn btn-primary text-base px-6 py-3 w-full sm:w-auto">Apply Now</button>';
  } else {
    actionHtml = '<span class="badge badge-danger">' + (job.status !== 'active' ? 'Position closed' : applicationPeriodState.accepting && !postingAcceptsCycle(job) ? 'No Sections in current cycle' : 'Intake closed') + '</span>';
  }

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
          actionHtml +
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
        buildWorkScheduleSummaryHtml(job) +
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

let profileDocs = [];

async function openApply() {
  try {
    applicationPeriodState = await applicantApi.getApplicationPeriod();
    if (!applicationPeriodState.accepting || !postingAcceptsCycle(jobData)) {
      renderDetail(jobData); showToast('This position is not accepting applications in the current cycle.', 'error'); return;
    }
  } catch (error) { showToast('Could not verify the application period.', 'error'); return; }
  document.getElementById('applyTitle').textContent = 'Apply for ' + (jobData.moduleCode || '') + ' · ' + applicationPeriodState.period.name;
  document.getElementById('applyBtn').disabled = true;
  document.getElementById('applyModal').classList.add('show');
  try {
    const [data, schedule] = await Promise.all([applicantApi.getProfile(), applicantApi.getAvailability(applicationPeriodState.period.id)]);
    AvailabilityView.render(document.getElementById('applyAvailability'), schedule.availability);
    if (!schedule.availability) document.getElementById('applyAvailability').textContent = 'Save your weekly availability for this period before submitting.';
    document.getElementById('editApplyAvailability').href = 'availability.jsp?periodId=' + encodeURIComponent(applicationPeriodState.period.id);
    document.getElementById('applyBtn').disabled = !schedule.availability;

    const user = data.user || {};
    const profile = data.profile || {};
    document.getElementById('applyName').textContent = user.name || '-';
    document.getElementById('applyEmail').textContent = user.email || '-';
    document.getElementById('applyDegree').textContent = profile.degree || '-';
    document.getElementById('applyYear').textContent = profile.year || '-';
    document.getElementById('applyGpa').textContent = profile.gpa ? profile.gpa + ' / ' + (profile.maxGpa || '4.0') : '-';
    const skills = profile.skills || [];
    document.getElementById('applySkills').innerHTML = skills.length
      ? skills.map(s => '<span class="px-2 py-0.5 bg-zinc-100 text-zinc-700 rounded-full text-xs border border-zinc-200">' + s + '</span>').join(' ')
      : '<span class="text-zinc-400">-</span>';

    profileDocs = profile.documents || [];
    renderApplyDocs();
    lucide.createIcons();
  } catch (e) {
    console.error('Failed to load profile for apply', e);
    document.getElementById('applyAvailability').textContent = 'Could not load your saved availability. Close this window and retry.';
  }
}

function renderApplyDocs() {
  const el = document.getElementById('applyDocList');
  if (profileDocs.length === 0) {
    el.innerHTML = '<p class="text-sm text-zinc-400">No documents uploaded. Upload one below or on your <a href="profile.jsp" class="text-blue-600 underline">Profile page</a>.</p>';
    return;
  }
  el.innerHTML = profileDocs.map(original => { const d=escapeDisplay(original); return '<label class="flex items-center gap-3 p-3 border border-zinc-200 rounded-lg hover:bg-zinc-50 cursor-pointer transition-colors">' +
    '<input type="checkbox" class="applyDocCb accent-blue-600" value="' + d.id + '" checked>' +
    '<div class="h-8 w-8 bg-red-100 text-red-600 rounded-lg flex items-center justify-center shrink-0"><i data-lucide="file-text" class="h-4 w-4"></i></div>' +
    '<div class="flex-1 min-w-0"><p class="text-sm font-medium text-zinc-900 truncate">' + (d.name || 'Untitled') + '</p>' +
    '<p class="text-xs text-zinc-500">' + (d.size || '') + '</p></div></label>'
  }).join('');
  lucide.createIcons();
}

async function uploadAndAttach(event) {
  const file = event.target.files && event.target.files[0];
  if (!file) return;
  try {
    const formData = new FormData();
    formData.append('file', file);
    const resp = await fetch(BASE + '/applicant/profile/documents', { method: 'POST', credentials: 'same-origin', body: formData });
    const data = await resp.json().catch(() => ({}));
    if (!resp.ok) throw { message: data.error || 'Upload failed' };
    if (data.document || data.id) profileDocs.push(data.document || data);
    else { const pdata = await applicantApi.getProfile(); profileDocs = (pdata.profile || {}).documents || []; }
    renderApplyDocs();
    showToast('Document uploaded!');
  } catch (e) { showToast(e.message || 'Upload failed', 'error'); }
  event.target.value = '';
}

function closeApply() { document.getElementById('applyModal').classList.remove('show'); }

async function submitApply() {
  const errDiv = document.getElementById('applyError');
  errDiv.classList.add('hidden');
  const cl = document.getElementById('coverLetter').value.trim();
  if (!cl) { errDiv.textContent = 'Cover letter is required.'; errDiv.classList.remove('hidden'); return; }

  const selectedDocIds = [...document.querySelectorAll('.applyDocCb:checked')].map(cb => cb.value);

  try {
    await applicantApi.applyForJob(jobId, cl, selectedDocIds, applicationPeriodState.period?.id);
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
