<%@ page pageEncoding="UTF-8" %>
  <%@ include file="/common/auth-check.jsp" %>
    <jsp:include page="/common/layout-top.jsp" />

    <div class="space-y-8 max-w-6xl mx-auto">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 class="text-3xl font-bold tracking-tight text-zinc-900">Organiser Overview</h1>
          <p class="text-zinc-500 mt-2">Manage your TA recruitment pipeline across modules.</p>
        </div>
        <a href="<%= request.getContextPath() %>/organiser/jobs.jsp?create=true" class="btn btn-primary gap-2 shrink-0">
          <i data-lucide="plus" class="h-4 w-4"></i> Create Posting
        </a>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6" id="statsGrid">
        <div class="card fade-in">
          <div class="p-6 flex items-center gap-4">
            <div class="stat-icon"><i data-lucide="briefcase" class="h-6 w-6"></i></div>
            <div>
              <p class="text-sm font-medium text-zinc-500">Active Postings</p>
              <p class="text-2xl font-bold text-zinc-900" id="statPostings">-</p>
            </div>
          </div>
        </div>
        <div class="card fade-in">
          <div class="p-6 flex items-center gap-4">
            <div class="stat-icon"><i data-lucide="users" class="h-6 w-6"></i></div>
            <div>
              <p class="text-sm font-medium text-zinc-500">Total Applicants</p>
              <p class="text-2xl font-bold text-zinc-900" id="statApplicants">-</p>
            </div>
          </div>
        </div>
        <div class="card fade-in">
          <div class="p-6 flex items-center gap-4">
            <div class="stat-icon"><i data-lucide="calendar" class="h-6 w-6"></i></div>
            <div>
              <p class="text-sm font-medium text-zinc-500">Interviews to Schedule</p>
              <p class="text-2xl font-bold text-zinc-900" id="statInterviews">-</p>
            </div>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div class="lg:col-span-2 space-y-6">
          <div class="flex items-center justify-between">
            <h2 class="text-xl font-semibold text-zinc-900">Recent Activity</h2>
            <a href="<%= request.getContextPath() %>/organiser/pipeline.jsp"
              class="btn btn-ghost text-zinc-500 hover:text-zinc-900">View pipeline</a>
          </div>
          <div class="card">
            <div class="divide-y divide-zinc-100" id="recentActivity">
              <div class="p-6 text-center text-zinc-400">Loading...</div>
            </div>
            <div class="p-4 border-t border-zinc-100 text-center">
              <a href="<%= request.getContextPath() %>/organiser/pipeline.jsp"
                class="text-sm font-medium text-zinc-600 hover:text-zinc-900">View all activity &rarr;</a>
            </div>
          </div>
        </div>

        <div class="space-y-6">
          <div>
            <h2 class="text-xl font-semibold text-zinc-900">Active Postings</h2>
            <p class="text-sm text-zinc-500">Track your published course and TA postings in one place.</p>
          </div>
          <div class="card">
            <div class="max-h-96 overflow-y-auto">
              <div class="divide-y divide-zinc-100" id="activePostings">
                <div class="p-6 text-center text-zinc-400">Loading...</div>
              </div>
            </div>
          </div>
          <div class="card">
            <div class="p-6 flex items-center justify-between gap-4">
              <div>
                <p class="text-sm font-medium text-zinc-500">Admin Notifications</p>
                <h3 class="text-lg font-semibold text-zinc-900">Receive messages from AD</h3>
              </div>
              <button type="button" onclick="markAllNotificationsRead()"
                class="btn btn-ghost text-sm text-zinc-600 hover:text-zinc-900">
                Mark all read
              </button>
            </div>
            <div class="divide-y divide-zinc-100 max-h-96 overflow-y-auto" id="organiserNotifications">
              <div class="p-6 text-center text-zinc-400">Loading notifications...</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <jsp:include page="/common/layout-bottom.jsp" />
    <script>
      async function loadOrganiserNotifications() {
        try {
          const notifications = await organiserApi.getNotifications();
          const container = document.getElementById('organiserNotifications');
          if (!notifications || notifications.length === 0) {
            container.innerHTML = '<div class="p-6 text-center text-zinc-400">No notifications from AD</div>';
            return;
          }

          container.innerHTML = notifications.map(notif => {
            const readClass = notif.read ? 'opacity-60' : 'bg-zinc-50';
            return '<div class="p-6 hover:bg-zinc-50 transition-colors ' + readClass + '">' +
              '<div class="flex items-start justify-between gap-4">' +
              '<div>' +
              '<p class="text-sm font-semibold text-zinc-900">' + (notif.title || 'Notification') + '</p>' +
              '<p class="text-sm text-zinc-500 mt-1">' + (notif.message || '') + '</p>' +
              '</div>' +
              '<button type="button" onclick="markNotificationRead(\'' + notif.id + '\')" class="text-xs text-blue-600 hover:underline">Mark read</button>' +
              '</div>' +
              '<p class="text-xs text-zinc-400 mt-3">' + (new Date(notif.createdAt).toLocaleString() || '') + '</p>' +
              '</div>';
          }).join('');
        } catch (e) {
          console.error('加载通知失败', e);
          document.getElementById('organiserNotifications').innerHTML = '<div class="p-6 text-center text-red-500">Unable to load notifications</div>';
        }
      }

      async function markNotificationRead(id) {
        try {
          await organiserApi.markNotificationRead(id);
          await loadOrganiserNotifications();
        } catch (e) {
          console.error('标记通知已读失败', e);
        }
      }

      async function markAllNotificationsRead() {
        try {
          await organiserApi.markAllNotificationsRead();
          await loadOrganiserNotifications();
        } catch (e) {
          console.error('标记所有通知已读失败', e);
        }
      }

      async function loadDashboard() {
        try {
          const data = await organiserApi.getDashboard();
          const jobs = data.jobs || [];
          const apps = data.recentApplications || [];

          document.getElementById('statPostings').textContent = jobs.filter(j => j.status === 'active').length;
          document.getElementById('statApplicants').textContent = data.totalApplications || apps.length;
          document.getElementById('statInterviews').textContent = data.interviewsToSchedule || 0;

          const actDiv = document.getElementById('recentActivity');
          if (apps.length === 0) {
            actDiv.innerHTML = '<div class="p-6 text-center text-zinc-400">No recent activity</div>';
          } else {
            actDiv.innerHTML = apps.slice(0, 5).map(app => {
              const badgeClass = app.status === 'shortlisted' ? 'badge-success' : app.status === 'interview' ? 'badge-warning' : 'badge-neutral';
              return '<div class="p-6 flex items-center justify-between hover:bg-zinc-50/50 transition-colors cursor-pointer group">' +
                '<div class="flex items-center gap-4">' +
                '<div class="h-10 w-10 rounded-full bg-zinc-200 flex items-center justify-center font-medium text-zinc-600">' + ((app.applicantName || 'A').charAt(0)) + '</div>' +
                '<div class="space-y-1"><h3 class="font-semibold text-zinc-900 group-hover:text-blue-600 transition-colors">' + (app.applicantName || 'Applicant') + '</h3>' +
                '<p class="text-sm text-zinc-500">Applied for ' + (app.jobTitle || app.moduleCode || '') + '</p></div>' +
                '</div>' +
                '<div class="flex items-center gap-4"><span class="badge ' + badgeClass + '">' + app.status + '</span></div></div>';
            }).join('');
          }

          const posDiv = document.getElementById('activePostings');
          if (jobs.length === 0) {
            posDiv.innerHTML = '<div class="p-6 text-center text-zinc-400">No active postings</div>';
          } else {
            posDiv.innerHTML = jobs.map(j => {
              const pct = Math.min(100, ((j.applicationCount || 0) / 100) * 100);
              return '<div class="p-6">' +
                '<div class="flex justify-between items-start mb-2">' +
                '<h3 class="font-semibold text-zinc-900">' + (j.moduleCode || j.title) + '</h3>' +
                '<span class="badge bg-amber-50 text-amber-600 border-amber-200">' + (j.status || 'active') + '</span>' +
                '</div>' +
                '<div class="flex items-center gap-4 text-sm mt-3">' +
                '<div class="flex flex-col"><span class="text-zinc-500">Applicants</span><span class="font-semibold text-zinc-900">' + (j.applicationCount || 0) + '</span></div>' +
                '<div class="h-8 w-px bg-zinc-200"></div>' +
                '<div class="flex flex-col"><span class="text-zinc-500">Positions</span><span class="font-semibold text-zinc-900">' + (j.positions || '-') + '</span></div>' +
                '</div>' +
                '<div class="mt-4 w-full h-1.5 bg-zinc-100 rounded-full overflow-hidden"><div class="h-full bg-blue-500 rounded-full" style="width:' + pct + '%"></div></div></div>';
            }).join('');
          }

          await loadOrganiserNotifications();
        } catch (e) {
          console.error('加载仪表盘失败', e);
        }
      }
      loadDashboard();
    </script>