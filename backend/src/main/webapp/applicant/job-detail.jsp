<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/auth-check.jsp" %>
<jsp:include page="/common/layout-top.jsp"/>

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
<script>
const ctx = document.querySelector('meta[name="ctx"]').content;
const params = new URLSearchParams(window.location.search);
const jobId = params.get('id');
let jobData = null;
let myApplicationStatus = null; // null | 'applied' | 'interview' | 'offer' | 'hired' | 'rejected'

async function loadJobDetail() {
  if (!jobId) { document.getElementById('loading').textContent = 'No job ID provided.'; return; }
  try {
    const [jobResp, appsResp] = await Promise.all([
      applicantApi.getJobById(jobId),
      applicantApi.getApplications().catch(() => []),
      loadExistingSchedule()
    ]);
    jobData = jobResp.job || jobResp;
    const apps = Array.isArray(appsResp) ? appsResp : (appsResp.applications || []);
    const myApp = apps.find(a => a.jobId === jobId);
    myApplicationStatus = myApp ? myApp.status : null;
    renderDetail(jobData);
  } catch (e) {
    document.getElementById('loading').textContent = 'Failed to load job details: ' + (e.message || '');
  }
}

var scheduleWeekOffset = 0;
var scheduleWorkDates = [];
var existingSchedule = [];
var existingOffers = [];

function padDate(dt) {
  var y = dt.getFullYear();
  var m = String(dt.getMonth() + 1).padStart(2, '0');
  var d = String(dt.getDate()).padStart(2, '0');
  return y + '-' + m + '-' + d;
}

function expandWorkDates(rawDates) {
  if (!rawDates || rawDates.length === 0) return [];
  var sorted = rawDates.slice().sort();
  if (sorted.length > 2) return sorted;

  var start = new Date(sorted[0] + 'T00:00:00');
  var end = new Date(sorted[sorted.length - 1] + 'T00:00:00');

  var targetDays = {};
  sorted.forEach(function(d) {
    var dt = new Date(d + 'T00:00:00');
    targetDays[dt.getDay()] = true;
  });

  var allDates = [];
  var cursor = new Date(start);
  while (cursor <= end) {
    if (targetDays[cursor.getDay()]) {
      allDates.push(padDate(cursor));
    }
    cursor.setDate(cursor.getDate() + 1);
  }
  return allDates;
}

function buildWorkScheduleSummaryHtml(job) {
  if (!job.workDates || job.workDates.length === 0) return '';

  var sorted = job.workDates.slice().sort();
  var startDate = sorted[0];
  var endDate = sorted[sorted.length - 1];
  var expanded = expandWorkDates(job.workDates);

  var dayNames = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];
  var dayFreq = {};
  sorted.forEach(function(d) {
    var dt = new Date(d + 'T00:00:00');
    var dayName = dayNames[dt.getDay()];
    dayFreq[dayName] = (dayFreq[dayName] || 0) + 1;
  });
  var workDaysList = Object.keys(dayFreq).sort(function(a, b) {
    return dayNames.indexOf(a) - dayNames.indexOf(b);
  });

  return '<div class="bg-emerald-50/50 rounded-xl p-6 border border-emerald-100">' +
    '<h4 class="font-semibold text-zinc-900 mb-4 flex items-center gap-2">' +
    '<i data-lucide="calendar-days" class="h-4 w-4 text-emerald-600"></i> Work Schedule</h4>' +
    '<div class="space-y-3">' +
    '<div class="flex items-center gap-2 text-sm">' +
    '<i data-lucide="calendar-range" class="h-4 w-4 text-zinc-400"></i>' +
    '<span class="text-zinc-700"><strong>' + startDate + '</strong> to <strong>' + endDate + '</strong></span></div>' +
    '<div class="flex items-center gap-2 text-sm">' +
    '<i data-lucide="repeat" class="h-4 w-4 text-zinc-400"></i>' +
    '<span class="text-zinc-700">Every <strong>' + workDaysList.join(', ') + '</strong></span></div>' +
    '<div class="flex items-center gap-2 text-sm">' +
    '<i data-lucide="hash" class="h-4 w-4 text-zinc-400"></i>' +
    '<span class="text-zinc-700"><strong>' + expanded.length + '</strong> sessions total</span></div>' +
    '</div></div>';
}

function buildSchedulePreviewSection(job) {
  if (!job.workDates || job.workDates.length === 0) return '';
  var isHired = (myApplicationStatus === 'offer' || myApplicationStatus === 'hired');
  var title = isHired ? 'Your Work Schedule' : 'Schedule Preview After Offer';
  var desc = isHired
    ? 'Your confirmed work schedule for this position, combined with your other commitments.'
    : 'This shows what your weekly calendar would look like if you receive this position, combined with your existing commitments.';
  return '<section class="mt-8">' +
    '<div class="card border border-zinc-200 rounded-2xl overflow-hidden">' +
    '<div class="bg-gradient-to-r from-emerald-50 to-indigo-50 px-6 py-4 border-b border-zinc-200">' +
    '<h3 class="text-lg font-bold text-zinc-900 flex items-center gap-2">' +
    '<i data-lucide="calendar-check" class="h-5 w-5 text-emerald-600"></i> ' +
    title + '</h3>' +
    '<p class="text-sm text-zinc-500 mt-1">' + desc + '</p>' +
    '</div>' +
    '<div class="p-6" id="schedulePreviewCalendar"></div>' +
    '</div></section>';
}

async function loadExistingSchedule() {
  try {
    var data = await applicantApi.getDashboard();
    existingSchedule = data.schedule || [];
    existingOffers = data.offers || [];
  } catch (e) {
    existingSchedule = [];
    existingOffers = [];
  }
}

function renderSchedulePreview() {
  var el = document.getElementById('schedulePreviewCalendar');
  if (!el || scheduleWorkDates.length === 0) return;

  var today = new Date();
  today.setHours(0,0,0,0);
  var todayStr = padDate(today);
  var dayOfWeek = today.getDay();
  var monday = new Date(today);
  monday.setDate(today.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1) + (scheduleWeekOffset * 7));

  var weekDays = ['Mon','Tue','Wed','Thu','Fri','Sat','Sun'];
  var monthNames = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

  var weekDates = [];
  for (var i = 0; i < 7; i++) {
    var d = new Date(monday);
    d.setDate(monday.getDate() + i);
    weekDates.push(d);
  }

  var newWorkSet = {};
  scheduleWorkDates.forEach(function(d) { newWorkSet[d] = true; });

  var existingWorkSet = {};
  (existingOffers || []).forEach(function(offer) {
    if (!offer.workDates) return;
    if (offer.jobId === jobId) return; // skip current job to avoid duplication
    var expanded = expandWorkDates(offer.workDates);
    expanded.forEach(function(d) { existingWorkSet[d] = (offer.moduleCode || 'Existing'); });
  });

  var existingInterviews = {};
  (existingSchedule || []).filter(function(s) { return s.type === 'interview'; }).forEach(function(iv) {
    if (iv.date) existingInterviews[iv.date] = iv;
  });

  var isCurrentWeek = scheduleWeekOffset === 0;

  var html = '<div class="flex items-center justify-between mb-4">' +
    '<div class="flex items-center gap-1">' +
    '<button onclick="shiftScheduleWeek(-1)" class="p-1.5 rounded-lg hover:bg-zinc-100 transition-colors"><i data-lucide="chevron-left" class="h-5 w-5 text-zinc-500"></i></button>' +
    '<button onclick="shiftScheduleWeek(1)" class="p-1.5 rounded-lg hover:bg-zinc-100 transition-colors"><i data-lucide="chevron-right" class="h-5 w-5 text-zinc-500"></i></button>' +
    '</div>' +
    '<span class="font-semibold text-zinc-800">' + monthNames[monday.getMonth()] + ' ' + monday.getFullYear() + '</span>' +
    '<div class="flex gap-2">' +
    '<button onclick="jumpScheduleToday()" class="text-xs px-2.5 py-1 rounded-md ' + (isCurrentWeek ? 'bg-zinc-100 text-zinc-400' : 'bg-indigo-100 text-indigo-700 hover:bg-indigo-200') + ' font-medium">Today</button>' +
    '<button onclick="jumpScheduleNextWork()" class="text-xs px-2.5 py-1 rounded-md bg-emerald-100 text-emerald-700 hover:bg-emerald-200 font-medium">Next Work</button>' +
    '</div></div>';

  html += '<div class="grid grid-cols-7 gap-1 text-center text-xs font-medium text-zinc-500 mb-2">';
  weekDays.forEach(function(day) { html += '<div>' + day + '</div>'; });
  html += '</div>';

  html += '<div class="grid grid-cols-7 gap-1">';
  weekDates.forEach(function(date, idx) {
    var dateStr = padDate(date);
    var isToday = dateStr === todayStr;
    var isNewWork = newWorkSet[dateStr];
    var existingWork = existingWorkSet[dateStr];
    var hasInterview = existingInterviews[dateStr];

    var cellBg = '';
    if (isToday) {
      cellBg = 'bg-indigo-600 text-white border-indigo-600 shadow-md';
    } else if (isNewWork && existingWork) {
      cellBg = 'bg-gradient-to-br from-emerald-100 to-blue-100 border-emerald-300 hover:border-emerald-500';
    } else if (isNewWork) {
      cellBg = 'bg-emerald-50 border-emerald-300 hover:border-emerald-500';
    } else if (existingWork) {
      cellBg = 'bg-blue-50 border-blue-200 hover:border-blue-400';
    } else {
      cellBg = 'bg-white border-zinc-200 hover:border-zinc-300';
    }

    html += '<div class="relative group">';
    html += '<div class="aspect-square flex flex-col items-center justify-center rounded-lg border transition-all cursor-pointer ' + cellBg + '">';
    html += '<span class="text-sm font-semibold">' + date.getDate() + '</span>';

    var dots = '';
    if (isNewWork) dots += '<span class="w-1.5 h-1.5 rounded-full ' + (isToday ? 'bg-emerald-300' : 'bg-emerald-500') + '"></span>';
    if (existingWork) dots += '<span class="w-1.5 h-1.5 rounded-full ' + (isToday ? 'bg-blue-300' : 'bg-blue-500') + '"></span>';
    if (hasInterview) dots += '<span class="w-1.5 h-1.5 rounded-full ' + (isToday ? 'bg-amber-300' : 'bg-amber-500') + '"></span>';
    if (dots) html += '<div class="flex gap-0.5 mt-1">' + dots + '</div>';
    html += '</div>';

    if (isNewWork || existingWork || hasInterview) {
      html += '<div class="absolute left-1/2 -translate-x-1/2 top-full mt-2 z-20 hidden group-hover:block w-48">' +
        '<div class="bg-white rounded-lg shadow-lg border border-zinc-200 p-3 text-left">';
      if (isNewWork) {
        html += '<div class="flex items-center gap-2 text-xs mb-2 last:mb-0">' +
          '<span class="w-2 h-2 rounded-full bg-emerald-500 shrink-0"></span>' +
          '<span class="text-zinc-700"><strong>' + (jobData.moduleCode || '') + '</strong> (This Job)</span></div>';
      }
      if (existingWork) {
        html += '<div class="flex items-center gap-2 text-xs mb-2 last:mb-0">' +
          '<span class="w-2 h-2 rounded-full bg-blue-500 shrink-0"></span>' +
          '<span class="text-zinc-700"><strong>' + existingWork + '</strong> (Existing)</span></div>';
      }
      if (hasInterview) {
        html += '<div class="flex items-center gap-2 text-xs mb-2 last:mb-0">' +
          '<span class="w-2 h-2 rounded-full bg-amber-500 shrink-0"></span>' +
          '<span class="text-zinc-700"><strong>' + (hasInterview.moduleCode || '') + '</strong> Interview</span></div>';
      }
      html += '</div></div>';
    }
    html += '</div>';
  });
  html += '</div>';

  html += '<div class="flex flex-wrap items-center gap-4 mt-4 pt-4 border-t border-zinc-100 text-xs text-zinc-500">' +
    '<div class="flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-full bg-emerald-500"></span>This Job</div>' +
    '<div class="flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-full bg-blue-500"></span>Existing Work</div>' +
    '<div class="flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-full bg-amber-500"></span>Interview</div></div>';

  el.innerHTML = html;
  lucide.createIcons();
}

function shiftScheduleWeek(delta) {
  scheduleWeekOffset += delta;
  renderSchedulePreview();
}

function jumpScheduleToday() {
  scheduleWeekOffset = 0;
  renderSchedulePreview();
}

function jumpScheduleNextWork() {
  var today = new Date();
  today.setHours(0,0,0,0);
  var todayStr = padDate(today);
  var nextWork = scheduleWorkDates.find(function(d) { return d >= todayStr; });
  if (!nextWork) {
    if (scheduleWorkDates.length > 0) nextWork = scheduleWorkDates[scheduleWorkDates.length - 1];
    else return;
  }
  var target = new Date(nextWork + 'T00:00:00');
  var diffDays = Math.round((target - today) / 86400000);
  var dow = today.getDay();
  var todayMondayOff = dow === 0 ? 6 : dow - 1;
  scheduleWeekOffset = Math.floor((diffDays + todayMondayOff) / 7);
  renderSchedulePreview();
}

function renderDetail(job) {
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
  } else if (job.status === 'active') {
    actionHtml = '<button onclick="openApply()" class="btn btn-primary text-base px-6 py-3 w-full sm:w-auto">Apply Now</button>';
  } else {
    actionHtml = '<span class="badge badge-danger">Closed</span>';
  }

  scheduleWorkDates = expandWorkDates(job.workDates || []);
  // Default: jump to the nearest work day's week
  if (scheduleWorkDates.length > 0) {
    var initToday = new Date();
    initToday.setHours(0,0,0,0);
    var initTodayStr = padDate(initToday);
    var nearest = scheduleWorkDates.find(function(d) { return d >= initTodayStr; }) || scheduleWorkDates[scheduleWorkDates.length - 1];
    var initTarget = new Date(nearest + 'T00:00:00');
    var initDiff = Math.round((initTarget - initToday) / 86400000);
    var initDow = initToday.getDay();
    var initMondayOff = initDow === 0 ? 6 : initDow - 1;
    scheduleWeekOffset = Math.floor((initDiff + initMondayOff) / 7);
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
        buildSchedulePreviewSection(job) +
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
  renderSchedulePreview();
}

let profileDocs = [];

async function openApply() {
  document.getElementById('applyModal').classList.add('show');
  try {
    const data = await applicantApi.getProfile();
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
  }
}

function renderApplyDocs() {
  const el = document.getElementById('applyDocList');
  if (profileDocs.length === 0) {
    el.innerHTML = '<p class="text-sm text-zinc-400">No documents uploaded. Upload one below or on your <a href="profile.jsp" class="text-blue-600 underline">Profile page</a>.</p>';
    return;
  }
  el.innerHTML = profileDocs.map(d =>
    '<label class="flex items-center gap-3 p-3 border border-zinc-200 rounded-lg hover:bg-zinc-50 cursor-pointer transition-colors">' +
    '<input type="checkbox" class="applyDocCb accent-blue-600" value="' + d.id + '" checked>' +
    '<div class="h-8 w-8 bg-red-100 text-red-600 rounded-lg flex items-center justify-center shrink-0"><i data-lucide="file-text" class="h-4 w-4"></i></div>' +
    '<div class="flex-1 min-w-0"><p class="text-sm font-medium text-zinc-900 truncate">' + (d.name || 'Untitled') + '</p>' +
    '<p class="text-xs text-zinc-500">' + (d.size || '') + '</p></div></label>'
  ).join('');
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
    await applicantApi.applyForJob(jobId, cl, selectedDocIds);
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
