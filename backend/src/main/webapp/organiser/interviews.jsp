<%@ page pageEncoding="UTF-8" %>
  <%@ include file="/common/auth-check.jsp" %>
    <jsp:include page="/common/layout-top.jsp" />

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
              <a href="<%= request.getContextPath() %>/organiser/pipeline.jsp" class="btn btn-outline w-full">Go to
                Pipeline</a>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Schedule Modal -->
    <div id="scheduleModal" data-modal
      class="fixed inset-0 z-50 items-center justify-center bg-black/50 backdrop-blur-sm">
      <div class="bg-white rounded-2xl shadow-xl max-w-lg w-full mx-4">
        <div class="bg-zinc-50 border-b border-zinc-200/60 p-6">
          <h2 class="text-xl font-bold text-zinc-900">Schedule Interview</h2>
        </div>
        <div class="p-6 space-y-4">
          <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Application ID</label><input id="iAppId"
              class="input" placeholder="app001"></div>
          <div class="grid grid-cols-2 gap-4">
            <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Date</label><input id="iDate" type="date"
                class="input"></div>
            <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Start Time</label><input id="iTime"
                type="time" class="input"></div>
          </div>
          <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Location</label><input id="iLocation"
              class="input" placeholder="Location not needed for video call" disabled></div>
          <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Type</label>
            <select id="iType" class="input" onchange="toggleLocation()">
              <option value="video">Video Call</option>
              <option value="in_person">In Person</option>
            </select>
          </div>
          <div id="schedError" class="hidden text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-4 py-3">
          </div>
        </div>
        <div class="p-6 border-t border-zinc-200 flex justify-end gap-3">
          <button onclick="closeSchedule()" class="btn btn-secondary">Cancel</button>
          <button id="saveInterviewButton" onclick="saveInterview()" class="btn btn-primary">Schedule</button>
        </div>
      </div>
    </div>

    <jsp:include page="/common/layout-bottom.jsp" />
    <script>
      const ctx = document.querySelector('meta[name="ctx"]').content;

      function getQueryParam(name) {
        const params = new URLSearchParams(window.location.search);
        return params.get(name);
      }

      async function loadInterviews() {
        try {
          const applicationId = getQueryParam('applicationId');
          if (applicationId) {
            document.getElementById('iAppId').value = applicationId;
            openSchedule();
          }
          const data = await organiserApi.getInterviews();
          const interviews = data.interviews || data || [];
          interviewsCache = interviews;
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
              '<div class="h-10 w-10 rounded-full bg-zinc-100 flex items-center justify-center font-bold text-zinc-700">' + ((iv.candidateName || iv.applicationId || 'I').charAt(0)) + '</div>' +
              '<div><h3 class="font-bold text-zinc-900">' + (iv.candidateName || iv.applicationId || 'Candidate') + '</h3>' +
              '<p class="text-sm text-zinc-500">' + (iv.moduleCode || iv.applicationId || '') + '</p></div>' +
              '</div>' +
              '<div class="flex flex-wrap items-center gap-4 text-sm text-zinc-600">' +
              '<span class="flex items-center gap-1.5 font-medium text-zinc-900"><i data-lucide="calendar" class="h-4 w-4 text-zinc-400"></i> ' + (iv.date || 'TBD') + '</span>' +
              '<span class="flex items-center gap-1.5"><i data-lucide="clock" class="h-4 w-4 text-zinc-400"></i> ' + (iv.startTime || '') + '</span>' +
              '<span class="flex items-center gap-1.5"><i data-lucide="' + (iv.type === 'video' ? 'video' : 'user') + '" class="h-4 w-4 text-zinc-400"></i> ' + (iv.location || iv.type || '') + '</span>' +
              '</div>' +
              '<div class="flex flex-row sm:flex-col justify-end gap-2 shrink-0">' +
              '<span class="badge ' + (iv.status === 'scheduled' ? 'badge-success' : iv.status === 'completed' ? 'badge-neutral' : 'badge-warning') + ' capitalize">' + (iv.status || 'scheduled') + '</span>' +
              '<button onclick="editInterview(\'' + iv.id + '\')" class="btn btn-secondary btn-sm mt-2">Edit</button>' +
              '<button onclick="deleteInterview(\'' + iv.id + '\')" class="btn btn-outline btn-sm mt-2">Delete</button>' +
              '</div>' +
              '</div></div></div>';
          }).join('');
          lucide.createIcons();
        } catch (e) {
          console.error('加载面试列表失败', e);
          document.getElementById('interviewsList').innerHTML = '<div class="text-center text-red-500 py-12">Failed to load interviews</div>';
        }
      }

      let interviewsCache = [];
      let currentInterviewId = null;

      function openSchedule(interviewId) {
        currentInterviewId = interviewId || null;
        const saveButton = document.getElementById('saveInterviewButton');
        const applicationIdInput = document.getElementById('iAppId');
        const dateInput = document.getElementById('iDate');
        const timeInput = document.getElementById('iTime');
        const locationInput = document.getElementById('iLocation');
        const typeInput = document.getElementById('iType');

        if (interviewId) {
          const interview = interviewsCache.find(iv => iv.id === interviewId);
          if (interview) {
            applicationIdInput.value = interview.applicationId || '';
            dateInput.value = interview.date || '';
            timeInput.value = interview.startTime || '';
            locationInput.value = interview.location || '';
            typeInput.value = interview.type || 'video';
            saveButton.textContent = 'Update Interview';
          }
        } else {
          currentInterviewId = null;
          applicationIdInput.value = '';
          dateInput.value = new Date().toISOString().split('T')[0];
          timeInput.value = '10:00';
          locationInput.value = '';
          typeInput.value = 'video';
          saveButton.textContent = 'Schedule Interview';
        }

        toggleLocation();
        document.getElementById('scheduleModal').classList.add('show');
      }
      function closeSchedule() {
        document.getElementById('scheduleModal').classList.remove('show');
        currentInterviewId = null;
        document.getElementById('saveInterviewButton').textContent = 'Schedule Interview';
      }

      function editInterview(interviewId) {
        openSchedule(interviewId);
      }

      function toggleLocation() {
        const type = document.getElementById('iType').value;
        const locationInput = document.getElementById('iLocation');
        if (type === 'video') {
          locationInput.disabled = true;
          locationInput.placeholder = 'Location not needed for video call';
          locationInput.value = '';
        } else {
          locationInput.disabled = false;
          locationInput.placeholder = 'Room 402 / Zoom link';
        }
      }

      async function deleteInterview(interviewId) {
        if (!confirm('Delete this interview?')) return;
        try {
          await organiserApi.deleteInterview(interviewId);
          showToast('Interview deleted');
          loadInterviews();
        } catch (e) {
          showToast(e.message || 'Delete failed', 'error');
        }
      }

      async function saveInterview() {
        const errDiv = document.getElementById('schedError');
        errDiv.classList.add('hidden');
        const payload = {
          applicationId: document.getElementById('iAppId').value.trim(),
          date: document.getElementById('iDate').value,
          startTime: document.getElementById('iTime').value,
          location: document.getElementById('iLocation').value.trim(),
          type: document.getElementById('iType').value,
        };

        if (!payload.applicationId) {
          errDiv.textContent = 'Please enter Application ID.';
          errDiv.classList.remove('hidden');
          return;
        }
        if (!payload.date) {
          errDiv.textContent = 'Please select a date.';
          errDiv.classList.remove('hidden');
          return;
        }
        if (!payload.startTime) {
          errDiv.textContent = 'Please select a start time.';
          errDiv.classList.remove('hidden');
          return;
        }

        try {
          if (currentInterviewId) {
            await organiserApi.updateInterview(currentInterviewId, payload);
            showToast('Interview updated!');
          } else {
            await organiserApi.createInterview(payload);
            // ensure application status is updated to interview when scheduling from interviews page
            if (payload.applicationId) {
              try { await organiserApi.updateCandidateStatus(payload.applicationId, 'interview'); } catch (e) { }
            }
            showToast('Interview scheduled!');
          }
          closeSchedule();
          loadInterviews();
        } catch (e) {
          errDiv.textContent = e.message || 'Schedule failed';
          errDiv.classList.remove('hidden');
        }
      }

      loadInterviews();
    </script>