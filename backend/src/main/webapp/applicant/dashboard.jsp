<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/auth-check.jsp" %>
<jsp:include page="/common/layout-top.jsp"/>
<jsp:include page="/common/application-period-banner.jsp"/>

<div class="space-y-8" id="page">
  <label class="block">Application cycle<select id="dashboardPeriod" class="input w-auto" onchange="loadDashboard()"></select></label>
  <div>
    <h1 class="text-3xl font-bold tracking-tight text-zinc-900" id="greeting">Welcome back!</h1>
    <p class="text-zinc-500 mt-2 text-lg">Here's what's happening with your TA applications.</p>
  </div>

  <div class="grid grid-cols-1 md:grid-cols-2 gap-6" id="statsGrid">
    <div class="card fade-in">
      <div class="p-6 flex items-center gap-4">
        <div class="stat-icon"><i data-lucide="clock" class="h-6 w-6"></i></div>
        <div>
          <p class="text-sm font-medium text-zinc-500">Active Applications</p>
          <p class="text-2xl font-bold text-zinc-900" id="statActive">-</p>
        </div>
      </div>
    </div>
    <div class="card fade-in">
      <div class="p-6 flex items-center gap-4">
        <div class="stat-icon"><i data-lucide="check-circle-2" class="h-6 w-6"></i></div>
        <div>
          <p class="text-sm font-medium text-zinc-500">Profile Completion</p>
          <p class="text-2xl font-bold text-zinc-900" id="statProfile">-</p>
        </div>
      </div>
    </div>
  </div>

  <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
    <div class="lg:col-span-2 space-y-6">
      <h2 class="text-xl font-semibold text-zinc-900">Weekly Schedule</h2>
      <div class="card">
        <div class="p-4" id="weeklySchedule">
          <div class="text-center text-zinc-400 text-sm py-8">Loading...</div>
        </div>
      </div>

      <div id="offersSection" class="hidden space-y-4">
        <h2 class="text-xl font-semibold text-zinc-900 flex items-center gap-2"><i data-lucide="trophy" class="h-5 w-5 text-emerald-500"></i> Accepted Section Assignments</h2>
        <div class="space-y-4" id="offersList"></div>
      </div>

      <div class="flex items-center justify-between">
        <h2 class="text-xl font-semibold text-zinc-900">Recent Applications</h2>
        <a href="<%= request.getContextPath() %>/applicant/applications.jsp" class="btn btn-ghost text-zinc-500 hover:text-zinc-900">View all</a>
      </div>
      <div class="card">
        <div class="divide-y divide-zinc-100" id="recentApps">
          <div class="p-6 text-center text-zinc-400">Loading...</div>
        </div>
      </div>
    </div>

    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <h2 class="text-xl font-semibold text-zinc-900">Notifications</h2>
        <span id="unreadBadge" class="hidden px-2 py-0.5 text-xs font-bold bg-red-500 text-white rounded-full"></span>
      </div>
      <div class="card">
        <div class="divide-y divide-zinc-100 max-h-64 overflow-y-auto" id="notificationsList">
          <div class="p-4 text-center text-zinc-400 text-sm">Loading...</div>
        </div>
      </div>

      <h2 class="text-xl font-semibold text-zinc-900">Your Workload Tracker</h2>
      <div class="card bg-gradient-to-br from-indigo-50 to-purple-50 border-indigo-100">
        <div class="p-6" id="workloadCard">
          <div class="text-center text-zinc-400 text-sm py-2">Loading...</div>
        </div>
      </div>

      <h2 class="text-xl font-semibold text-zinc-900 mt-4">Upcoming Deadlines</h2>
      <div class="card">
        <div class="p-6 space-y-6" id="deadlines">
          <div class="text-center text-zinc-400 text-sm">Loading...</div>
        </div>
      </div>
    </div>
  </div>
</div>

<jsp:include page="/common/layout-bottom.jsp"/>
<script>
const ctx = document.querySelector('meta[name="ctx"]').content;
let currentWeekOffset = 0;
let cachedSchedule = [];

function padDate(dt) {
  var y = dt.getFullYear();
  var m = String(dt.getMonth() + 1).padStart(2, '0');
  var d = String(dt.getDate()).padStart(2, '0');
  return y + '-' + m + '-' + d;
}

function shiftWeek(delta) {
  currentWeekOffset += delta;
  renderSchedule(cachedSchedule);
  lucide.createIcons();
}

async function loadDashboard() {
  try {
    const data = await applicantApi.getDashboard(document.getElementById('dashboardPeriod').value);
    const periodSelect=document.getElementById('dashboardPeriod'); if(!periodSelect.options.length)(data.periods||[]).forEach(p=>periodSelect.add(new Option(p.name,p.id)));periodSelect.value=data.periodId;
    const apps = escapeDisplay(data.recentApplications || []);
    const jobs = escapeDisplay(data.upcomingDeadlines || []);

    document.getElementById('statActive').textContent = data.activeApplications ?? 0;
    document.getElementById('statProfile').textContent = (data.profileCompletion != null ? data.profileCompletion : 0) + '%';
    document.getElementById('greeting').textContent = 'Welcome back, ' + (data.profile?.name || <%= com.tamo.util.JsonUtil.getGson().toJson(session.getAttribute("userName")) %>) + '!';

    const container = document.getElementById('recentApps');
    if (apps.length === 0) {
      container.innerHTML = '<div class="p-6 text-center text-zinc-400">No applications yet. <a href="' + ctx + '/applicant/jobs.jsp" class="text-zinc-900 font-medium hover:underline">Browse jobs</a></div>';
    } else {
      container.innerHTML = apps.slice(0, 5).map(app => {
        const status = app.status === 'interview' ? 'review' : app.status;
        const statusText = status === 'review' || status === 'under_review' ? 'Under Review' : status;
        const badgeClass = status === 'rejected' ? 'badge-danger' : status === 'hired' ? 'badge-success' : 'badge-neutral';
        return '<a href="' + ctx + '/applicant/applications.jsp" class="p-6 flex items-center justify-between hover:bg-zinc-50/50 transition-colors cursor-pointer group block">' +
          '<div class="space-y-1"><h3 class="font-semibold text-zinc-900 group-hover:text-blue-600 transition-colors">' + (app.role || app.jobId) + '</h3>' +
          '<p class="text-sm text-zinc-500">' + (app.module || '') + ' • Applied ' + (app.appliedAt || '').split('T')[0] + '</p></div>' +
          '<div class="flex items-center gap-4"><span class="badge ' + badgeClass + '">' + statusText + '</span>' +
          '<i data-lucide="chevron-right" class="h-5 w-5 text-zinc-300 group-hover:text-zinc-500 transition-colors"></i></div></a>';
      }).join('');
    }

    renderWorkload(data.workload);
    renderSchedule(data.schedule || [], data.offers || []);
    renderNotifications(data.notifications || [], data.unreadNotifications || 0);
    renderOffers(data.offers || []);

    const dl = document.getElementById('deadlines');
    if (jobs.length === 0) {
      dl.innerHTML = '<div class="text-center text-zinc-400 text-sm">No upcoming deadlines</div>';
    } else {
      dl.innerHTML = jobs.slice(0, 3).map(j =>
        '<div class="flex gap-4"><div class="mt-0.5 h-2 w-2 rounded-full bg-blue-500 shrink-0"></div><div>' +
        '<p class="font-medium text-zinc-900 text-sm">' + (j.moduleCode || '') + ' ' + j.title + '</p>' +
        '<p class="text-sm text-zinc-500 mt-1">Closes ' + (j.deadline || 'TBD') + '</p></div></div>'
      ).join('') + '<a href="' + ctx + '/applicant/jobs.jsp" class="btn btn-outline w-full mt-4">Browse more jobs</a>';
    }

    lucide.createIcons();
  } catch (e) {
    console.error('Failed to load dashboard', e);
    document.getElementById('weeklySchedule').textContent='Dashboard could not be loaded. '+e.message;
  }
}

function renderWorkload(wl) {
  const el = document.getElementById('workloadCard');
  if (!wl) { el.innerHTML = '<p class="text-sm text-zinc-400 text-center">No workload data</p>'; return; }

  const pct = wl.max > 0 ? Math.min(Math.round(wl.current / wl.max * 100), 100) : 0;
  const barColor = wl.status === 'Overloaded' ? 'bg-red-500' : wl.status === 'Near Capacity' ? 'bg-amber-500' : 'bg-indigo-500';
  const badgeBg = wl.status === 'Overloaded' ? 'bg-red-100 text-red-700 border-red-200' : wl.status === 'Near Capacity' ? 'bg-amber-100 text-amber-700 border-amber-200' : 'bg-white/50 text-indigo-700 border-indigo-200';

  const termLabel = notificationText(document.getElementById('dashboardPeriod').selectedOptions[0]?.textContent||'Selected period');

  let html = '<div class="flex items-center justify-between mb-4">' +
    '<div class="flex items-center gap-2"><i data-lucide="trending-up" class="h-5 w-5 text-indigo-600"></i>' +
    '<span class="font-semibold text-indigo-900">' + termLabel + '</span></div>' +
    '<span class="badge ' + badgeBg + '">' + wl.status + '</span></div>';

  html += '<div class="space-y-2 mb-4">' +
    '<div class="flex justify-between text-sm"><span class="text-indigo-800/70">Accepted commitment</span>' +
    '<span class="font-bold text-indigo-900">' + wl.current + ' / ' + wl.max + ' hrs</span></div>' +
    '<div class="h-2 w-full bg-indigo-200/50 rounded-full overflow-hidden">' +
    '<div class="h-full ' + barColor + ' rounded-full transition-all duration-1000" style="width:' + pct + '%"></div></div></div>';

  if (wl.assignments && wl.assignments.length > 0) {
    html += '<div class="space-y-2 mb-4">';
    wl.assignments.forEach(function(a) {
      const aPct = wl.max > 0 ? Math.round(a.hoursPerWeek / wl.max * 100) : 0;
      html += '<div class="flex items-center justify-between text-sm">' +
        '<span class="text-indigo-900 font-medium">' + notificationText(a.moduleCode) + '</span>' +
        '<span class="text-indigo-800/70">' + a.hoursPerWeek + ' hrs/wk</span></div>' +
        '<div class="h-1.5 w-full bg-indigo-100 rounded-full overflow-hidden mb-1">' +
        '<div class="h-full bg-indigo-400 rounded-full" style="width:' + aPct + '%"></div></div>';
    });
    html += '</div>';
  }

  html += '<p class="text-sm">Published reserved hours: '+wl.reservedHours+'. Review actual Section requirements before accepting further work.</p>';

  el.innerHTML = html;
}

let cachedOffers = [];

function renderSchedule(schedule, offers) {
  cachedSchedule = schedule;
  if (offers) cachedOffers = offers;
  const el = document.getElementById('weeklySchedule');
  const safeSchedule=escapeDisplay(schedule);
  const isCurrentWeek=currentWeekOffset===0;
  const duties = safeSchedule.filter(function(s) { return s.type === 'duty'; });

  const today = new Date();
  const dayOfWeek = today.getDay();
  const monday = new Date(today);
  monday.setDate(today.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1) + (currentWeekOffset * 7));

  const weekDays = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
  const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

  const weekDates = [];
  for (let i = 0; i < 7; i++) {
    const d = new Date(monday);
    d.setDate(monday.getDate() + i);
    weekDates.push(d);
  }

  const eventsByDay = {};
  for (let i = 0; i < 7; i++) eventsByDay[i] = [];

  duties.forEach(function(d) {
    (d.meetingTimes||[]).forEach(function(m){eventsByDay[m.dayOfWeek-1].push({type:'work',data:{moduleCode:d.moduleCode+' · '+d.title+' '+m.startTime+'–'+m.endTime}});});
  });
  let html = '<div class="flex items-center justify-between mb-4">' +
    '<div class="flex items-center gap-1">' +
    '<button aria-label="Previous week" onclick="shiftWeek(-1)" class="p-1.5 rounded-lg hover:bg-zinc-100 transition-colors"><i data-lucide="chevron-left" class="h-5 w-5 text-zinc-500"></i></button>' +
    '<button aria-label="Next week" onclick="shiftWeek(1)" class="p-1.5 rounded-lg hover:bg-zinc-100 transition-colors"><i data-lucide="chevron-right" class="h-5 w-5 text-zinc-500"></i></button>' +
    '</div>' +
    '<span class="font-semibold text-zinc-800">' + monthNames[monday.getMonth()] + ' ' + monday.getFullYear() + '</span>' +
    '<div class="flex gap-2">' +
    (isCurrentWeek ? '' : '<button onclick="currentWeekOffset=0;renderSchedule(cachedSchedule);lucide.createIcons();" class="text-xs px-2 py-1 rounded-md bg-indigo-100 text-indigo-700 hover:bg-indigo-200 font-medium">Today</button>') +
    '</div></div>';

  html += '<div class="grid grid-cols-7 gap-1 text-center text-xs font-medium text-zinc-500 mb-2">';
  weekDays.forEach(function(day) { html += '<div>' + day + '</div>'; });
  html += '</div>';

  html += '<div class="grid grid-cols-7 gap-1">';
  weekDates.forEach(function(date, idx) {
    const isToday = date.toDateString() === today.toDateString();
    const events = eventsByDay[idx];
    const hasWork = events.some(function(e) { return e.type === 'work'; });

    html += '<div class="relative group" tabindex="0" aria-label="Weekly schedule '+padDate(date)+'">';
    html += '<div class="aspect-square flex flex-col items-center justify-center rounded-lg border transition-all cursor-pointer ' +
      (isToday ? 'bg-indigo-600 text-white border-indigo-600 shadow-md' : hasWork ? 'bg-emerald-50 border-emerald-200 hover:border-emerald-400' : 'bg-white border-zinc-200 hover:border-indigo-300 hover:bg-indigo-50/50') + '">';
    html += '<span class="text-sm font-semibold">' + date.getDate() + '</span>';
    
    if (hasWork) {
      html += '<div class="flex gap-0.5 mt-1">';
      if (hasWork) html += '<span class="w-1.5 h-1.5 rounded-full ' + (isToday ? 'bg-emerald-300' : 'bg-emerald-500') + '"></span>';
      html += '</div>';
    }
    html += '</div>';

    if (events.length > 0) {
      html += '<div class="absolute left-1/2 -translate-x-1/2 top-full mt-2 z-20 hidden group-hover:block group-focus-within:block w-48">' +
        '<div class="bg-white rounded-lg shadow-lg border border-zinc-200 p-3 text-left">';
      events.forEach(function(ev) {
        if (ev.type === 'duty') {
          html += '<div class="flex items-center gap-2 text-xs mb-2 last:mb-0">' +
            '<span class="w-2 h-2 rounded-full bg-indigo-400 shrink-0"></span>' +
            '<span class="text-zinc-700"><strong>' + ev.data.moduleCode + '</strong> TA Duty</span></div>';
        } else if (ev.type === 'work') {
          html += '<div class="flex items-center gap-2 text-xs mb-2 last:mb-0">' +
            '<span class="w-2 h-2 rounded-full bg-emerald-500 shrink-0"></span>' +
            '<span class="text-zinc-700"><strong>' + (ev.data.moduleCode || '') + '</strong> Work Day</span></div>';

        }
      });
      html += '</div></div>';
    }
    html += '</div>';
  });
  html += '</div>';

  html += '<div class="flex items-center gap-4 mt-4 pt-4 border-t border-zinc-100 text-xs text-zinc-500">' +
    '<div class="flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-full bg-emerald-500"></span>Work Day</div></div>';

  el.innerHTML = html;
}

function renderNotifications(notifications, unreadCount) {
  const el = document.getElementById('notificationsList');
  const badge = document.getElementById('unreadBadge');

  if (unreadCount > 0) {
    badge.textContent = unreadCount;
    badge.classList.remove('hidden');
  } else {
    badge.classList.add('hidden');
  }

  if (notifications.length === 0) {
    el.innerHTML = '<div class="p-4 text-center text-zinc-400 text-sm">No notifications yet</div>';
    return;
  }

  el.innerHTML = notifications.map(function(n) {
    const iconMap = {
      'status_change': 'refresh-cw',
      'offer': 'trophy',
      'rejection': 'x-circle'
    };
    const colorMap = {
      'status_change': 'bg-blue-100 text-blue-600',
      'offer': 'bg-emerald-100 text-emerald-600',
      'rejection': 'bg-red-100 text-red-600'
    };
    const icon = iconMap[n.type] || 'bell';
    const color = colorMap[n.type] || 'bg-zinc-100 text-zinc-600';
    const timeAgo = formatTimeAgo(n.createdAt);

    return '<div class="p-4 hover:bg-zinc-50 transition-colors ' + (n.read ? 'opacity-60' : '') + '">' +
      '<div class="flex gap-3">' +
      '<div class="h-9 w-9 rounded-full ' + color + ' flex items-center justify-center shrink-0">' +
      '<i data-lucide="' + icon + '" class="h-4 w-4"></i></div>' +
      '<div class="flex-1 min-w-0">' +
      '<p class="text-sm font-medium text-zinc-900">' + notificationText(n.title) + '</p>' +
      '<p class="text-xs text-zinc-500 mt-0.5 line-clamp-2">' + notificationText(n.message) + '</p>' +
      '<p class="text-xs text-zinc-400 mt-1">' + timeAgo + '</p>' +
      ((n.type || '').startsWith('assignment_') ? '<a class="text-sm text-blue-600 underline" href="' + getNotificationLink(n) + '">Review assignment</a>' : '') +
      '</div>' +
      (n.read ? '' : '<span class="w-2 h-2 rounded-full bg-blue-500 shrink-0 mt-1"></span>') +
      '</div></div>';
  }).join('');
}

function formatTimeAgo(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diffMs = now - date;
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 1) return 'Just now';
  if (diffMins < 60) return diffMins + ' min ago';
  if (diffHours < 24) return diffHours + ' hour' + (diffHours > 1 ? 's' : '') + ' ago';
  if (diffDays < 7) return diffDays + ' day' + (diffDays > 1 ? 's' : '') + ' ago';
  return date.toLocaleDateString();
}

function renderOffers(offers) {
  const el=document.getElementById('offersSection');if(!el)return;el.classList.toggle('hidden',!offers.length);
  const list=document.getElementById('offersList');list.replaceChildren();
  offers.forEach(o=>{const card=document.createElement('div');card.className='card p-4';card.textContent=o.moduleCode+' · '+o.title+' · '+o.hoursPerWeek+' hours/week · '+(o.meetingTimes||[]).map(m=>['','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday'][m.dayOfWeek]+' '+m.startTime+'–'+m.endTime).join(' · ');list.appendChild(card);});
}

loadDashboard();
</script>
