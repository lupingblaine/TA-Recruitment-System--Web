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
            const statusColors = {
              'scheduled': 'bg-blue-50 text-blue-700 border-blue-200',
              'completed': 'bg-emerald-50 text-emerald-700 border-emerald-200',
              'cancelled': 'bg-zinc-100 text-zinc-600 border-zinc-200'
            };
            const sColor = statusColors[iv.status] || 'bg-amber-50 text-amber-700 border-amber-200';
            
            return '<div class="card fade-in hover:shadow-lg transition-all duration-300 border border-zinc-200/60 overflow-hidden relative">' +
              (isToday ? '<div class="absolute left-0 top-0 bottom-0 w-1.5 bg-blue-500"></div>' : '<div class="absolute left-0 top-0 bottom-0 w-1.5 bg-zinc-200"></div>') +
              '<div class="p-5 sm:p-6 pl-6 sm:pl-8">' +
                '<div class="flex flex-col sm:flex-row sm:items-center justify-between gap-6">' +
                  '<div class="flex items-start gap-4">' +
                    '<div class="h-12 w-12 rounded-full bg-gradient-to-br from-zinc-100 to-zinc-200 border border-zinc-300/50 flex items-center justify-center text-lg font-bold text-zinc-700 shadow-sm shrink-0">' + ((iv.candidateName || iv.applicationId || 'I').charAt(0).toUpperCase()) + '</div>' +
                    '<div class="space-y-1.5">' +
                      '<div class="flex items-center gap-3">' +
                        '<h3 class="text-lg font-bold text-zinc-900 tracking-tight">' + (iv.candidateName || iv.applicationId || 'Candidate') + '</h3>' +
                        '<span class="badge border ' + sColor + ' px-2.5 py-0.5 rounded-full text-xs font-medium capitalize">' + (iv.status || 'scheduled') + '</span>' +
                      '</div>' +
                      '<p class="text-sm font-medium text-zinc-500 mb-2">' + (iv.moduleCode || iv.applicationId || 'Unknown Module') + '</p>' +
                      '<div class="flex flex-wrap items-center gap-x-5 gap-y-2 text-sm text-zinc-600 mt-2">' +
                        '<span class="flex items-center gap-1.5 bg-zinc-50 px-2.5 py-1 rounded-md border border-zinc-100"><i data-lucide="calendar" class="h-4 w-4 text-blue-500"></i> <span class="font-medium text-zinc-800">' + (iv.date || 'TBD') + '</span></span>' +
                        '<span class="flex items-center gap-1.5 bg-zinc-50 px-2.5 py-1 rounded-md border border-zinc-100"><i data-lucide="clock" class="h-4 w-4 text-amber-500"></i> <span class="font-medium text-zinc-800">' + (iv.startTime || 'TBD') + '</span></span>' +
                        '<span class="flex items-center gap-1.5 bg-zinc-50 px-2.5 py-1 rounded-md border border-zinc-100"><i data-lucide="' + (iv.type === 'video' ? 'video' : 'map-pin') + '" class="h-4 w-4 text-emerald-500"></i> <span class="font-medium text-zinc-800 max-w-[150px] truncate">' + (iv.location || iv.type || 'N/A') + '</span></span>' +
                      '</div>' +
                    '</div>' +
                  '</div>' +
                  '<div class="flex flex-row sm:flex-col items-center sm:items-end justify-start sm:justify-center gap-2 shrink-0 border-t sm:border-t-0 border-zinc-100 pt-4 sm:pt-0">' +
                    '<button onclick="editInterview(\'' + iv.id + '\')" class="btn btn-outline bg-white hover:bg-zinc-50 text-zinc-700 border-zinc-200 w-full sm:w-24 justify-center shadow-sm text-sm font-medium py-1.5">Edit</button>' +
                    '<button onclick="deleteInterview(\'' + iv.id + '\')" class="btn hover:bg-red-50 text-zinc-600 hover:text-red-600 hover:border-red-200 border border-transparent w-full sm:w-24 justify-center transition-colors text-sm font-medium py-1.5">Delete</button>' +
                  '</div>' +
                '</div>' +
              '</div>' +
            '</div>';
          }).join('');
          lucide.createIcons();
        } catch (e) {
          console.error('Failed to load interviews', e);
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