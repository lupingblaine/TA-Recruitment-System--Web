<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/auth-check.jsp" %>
<jsp:include page="/common/layout-top.jsp"/>

<div class="space-y-8 max-w-5xl mx-auto">
  <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
    <div>
      <h1 class="text-3xl font-bold tracking-tight text-zinc-900">Interviews</h1>
      <p class="text-zinc-500 mt-2">Manage your upcoming candidate interviews.</p>
    </div>
    <button onclick="openSchedule()" class="btn btn-primary gap-2 shrink-0">
      <i data-lucide="calendar" class="h-4 w-4"></i> Schedule Interview
    </button>
  </div>

  <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
    <div class="md:col-span-2 space-y-6">
      <h2 class="text-xl font-semibold text-zinc-900">Upcoming Sessions</h2>
      <div class="space-y-4" id="interviewsList">
        <div class="text-center text-zinc-400 py-12">Loading...</div>
      </div>
    </div>

    <div class="space-y-6">
      <h2 class="text-xl font-semibold text-zinc-900">Quick Actions</h2>
      <div class="card bg-amber-50/50 border-amber-100">
        <div class="p-6 space-y-4">
          <div class="space-y-2">
            <span class="badge badge-warning">Pending</span>
            <p class="text-sm text-zinc-600">Schedule interviews for shortlisted candidates.</p>
          </div>
          <a href="<%= request.getContextPath() %>/organiser/pipeline.jsp" class="btn btn-outline w-full">Go to Pipeline</a>
        </div>
      </div>
    </div>
  </div>
</div>

<!-- Schedule Modal -->
<div id="scheduleModal" data-modal class="fixed inset-0 z-50 items-center justify-center bg-black/50 backdrop-blur-sm">
  <div class="bg-white rounded-2xl shadow-xl max-w-lg w-full mx-4">
    <div class="bg-zinc-50 border-b border-zinc-200/60 p-6">
      <h2 class="text-xl font-bold text-zinc-900">Schedule Interview</h2>
    </div>
    <div class="p-6 space-y-4">
      <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Application ID</label><input id="iAppId" class="input" placeholder="app001"></div>
      <div class="grid grid-cols-2 gap-4">
        <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Date</label><input id="iDate" type="date" class="input"></div>
        <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Time</label><input id="iTime" type="time" class="input"></div>
      </div>
      <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Location</label><input id="iLocation" class="input" placeholder="Room 402 / Zoom link"></div>
      <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Type</label>
        <select id="iType" class="input"><option value="video">Video Call</option><option value="in_person">In Person</option></select>
      </div>
      <div id="schedError" class="hidden text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-4 py-3"></div>
    </div>
    <div class="p-6 border-t border-zinc-200 flex justify-end gap-3">
      <button onclick="closeSchedule()" class="btn btn-secondary">Cancel</button>
      <button onclick="saveInterview()" class="btn btn-primary">Schedule</button>
    </div>
  </div>
</div>

<jsp:include page="/common/layout-bottom.jsp"/>
<script>
const ctx = document.querySelector('meta[name="ctx"]').content;

async function loadInterviews() {
  try {
    const data = await organiserApi.getInterviews();
    const interviews = data.interviews || data || [];
    const container = document.getElementById('interviewsList');

    if (interviews.length === 0) {
      container.innerHTML = '<div class="text-center text-zinc-400 py-12">No interviews scheduled yet.</div>';
      return;
    }

    container.innerHTML = interviews.map(iv => {
      const isToday = iv.date && new Date(iv.date).toDateString() === new Date().toDateString();
      return '<div class="card fade-in border-l-4 ' + (isToday ? 'border-l-blue-500' : 'border-l-zinc-300') + '">' +
        '<div class="p-6"><div class="flex flex-col sm:flex-row justify-between gap-4">' +
          '<div class="space-y-3">' +
            '<div class="flex items-center gap-3">' +
              '<div class="h-10 w-10 rounded-full bg-zinc-100 flex items-center justify-center font-bold text-zinc-700">' + ((iv.applicantName || 'I').charAt(0)) + '</div>' +
              '<div><h3 class="font-bold text-zinc-900">' + (iv.applicantName || 'Candidate') + '</h3>' +
              '<p class="text-sm text-zinc-500">' + (iv.moduleCode || iv.applicationId || '') + '</p></div>' +
            '</div>' +
            '<div class="flex flex-wrap items-center gap-4 text-sm text-zinc-600">' +
              '<span class="flex items-center gap-1.5 font-medium text-zinc-900"><i data-lucide="calendar" class="h-4 w-4 text-zinc-400"></i> ' + (iv.date || 'TBD') + '</span>' +
              '<span class="flex items-center gap-1.5"><i data-lucide="clock" class="h-4 w-4 text-zinc-400"></i> ' + (iv.time || '') + '</span>' +
              '<span class="flex items-center gap-1.5"><i data-lucide="' + (iv.type === 'video' ? 'video' : 'user') + '" class="h-4 w-4 text-zinc-400"></i> ' + (iv.location || iv.type || '') + '</span>' +
            '</div>' +
          '</div>' +
          '<div class="flex flex-row sm:flex-col justify-end gap-2 shrink-0">' +
            '<span class="badge ' + (iv.status === 'scheduled' ? 'badge-success' : iv.status === 'completed' ? 'badge-neutral' : 'badge-warning') + ' capitalize">' + (iv.status || 'scheduled') + '</span>' +
          '</div>' +
        '</div></div></div>';
    }).join('');
    lucide.createIcons();
  } catch (e) {
    console.error('加载面试列表失败', e);
    document.getElementById('interviewsList').innerHTML = '<div class="text-center text-red-500 py-12">Failed to load interviews</div>';
  }
}

function openSchedule() { document.getElementById('scheduleModal').classList.add('show'); }
function closeSchedule() { document.getElementById('scheduleModal').classList.remove('show'); }

async function saveInterview() {
  const errDiv = document.getElementById('schedError');
  errDiv.classList.add('hidden');
  const payload = {
    applicationId: document.getElementById('iAppId').value.trim(),
    date: document.getElementById('iDate').value,
    time: document.getElementById('iTime').value,
    location: document.getElementById('iLocation').value.trim(),
    type: document.getElementById('iType').value,
  };
  try {
    await organiserApi.createInterview(payload);
    closeSchedule();
    showToast('Interview scheduled!');
    loadInterviews();
  } catch (e) {
    errDiv.textContent = e.message || 'Schedule failed';
    errDiv.classList.remove('hidden');
  }
}

loadInterviews();
</script>
