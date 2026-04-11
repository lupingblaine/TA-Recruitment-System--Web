<%@ page pageEncoding="UTF-8" %>
      </div>
    </div>
  </main>
</div>

<script src="${pageContext.request.contextPath}/static/js/api.js?v=20260411"></script>
<script>
  lucide.createIcons();

  async function doLogout() {
    try {
      await authApi.logout();
    } catch(e) {}
    window.location.href = '${pageContext.request.contextPath}/auth/login.jsp';
  }

  function toggleMobileMenu() {
    const sidebar = document.getElementById('mobileSidebar');
    const overlay = document.getElementById('mobileMenuOverlay');
    if (sidebar && overlay) {
      sidebar.classList.toggle('-translate-x-full');
      overlay.classList.toggle('hidden');
    }
    lucide.createIcons();
  }

  function toggleNotificationPanel() {
    const panel = document.getElementById('notificationPanel');
    if (panel) {
      panel.classList.toggle('hidden');
    }
  }

  document.addEventListener('click', function(e) {
    const panel = document.getElementById('notificationPanel');
    const btn = document.getElementById('notificationBtn');
    if (panel && btn && !panel.contains(e.target) && !btn.contains(e.target)) {
      panel.classList.add('hidden');
    }
  });

  // Load notifications for header dropdown
  async function loadHeaderNotifications() {
    try {
      const notifications = await applicantApi.getNotifications();
      renderHeaderNotifications(notifications);
    } catch (e) {
      console.log('Could not load notifications');
    }
  }

  function renderHeaderNotifications(notifications) {
    const list = document.getElementById('headerNotifList');
    const badge = document.getElementById('headerUnreadBadge');
    if (!list) return;

    const unread = notifications.filter(n => !n.read).length;
    if (badge) {
      badge.classList.toggle('hidden', unread === 0);
    }

    if (notifications.length === 0) {
      list.innerHTML = '<div class="p-4 text-center text-zinc-400 text-sm">No notifications</div>';
      return;
    }

    const iconMap = { status_change: 'refresh-cw', interview_scheduled: 'calendar-check', offer: 'trophy', rejection: 'x-circle' };
    const colorMap = { status_change: 'bg-blue-100 text-blue-600', interview_scheduled: 'bg-amber-100 text-amber-600', offer: 'bg-emerald-100 text-emerald-600', rejection: 'bg-red-100 text-red-600' };

    list.innerHTML = notifications.slice(0, 5).map(function(n) {
      const icon = iconMap[n.type] || 'bell';
      const color = colorMap[n.type] || 'bg-zinc-100 text-zinc-600';
      return '<a href="' + getNotificationLink(n) + '" onclick="markRead(\'' + n.id + '\')" class="block p-3 hover:bg-zinc-50 transition-colors ' + (n.read ? 'opacity-60' : '') + '">' +
        '<div class="flex gap-3">' +
        '<div class="h-8 w-8 rounded-full ' + color + ' flex items-center justify-center shrink-0">' +
        '<i data-lucide="' + icon + '" class="h-4 w-4"></i></div>' +
        '<div class="flex-1 min-w-0">' +
        '<p class="text-sm font-medium text-zinc-900 truncate">' + n.title + '</p>' +
        '<p class="text-xs text-zinc-500 truncate">' + n.message.substring(0, 50) + '...</p>' +
        '</div>' +
        (n.read ? '' : '<span class="w-2 h-2 rounded-full bg-blue-500 shrink-0 mt-2"></span>') +
        '</div></a>';
    }).join('');
    lucide.createIcons();
  }

  function getNotificationLink(n) {
    const ctx = document.querySelector('meta[name="ctx"]')?.content || '';
    if (n.type === 'interview_scheduled') return ctx + '/applicant/dashboard.jsp';
    return ctx + '/applicant/applications.jsp';
  }

  async function markRead(id) {
    try { await applicantApi.markNotificationRead(id); } catch(e) {}
  }

  async function markAllRead() {
    try {
      await applicantApi.markAllNotificationsRead();
      loadHeaderNotifications();
    } catch(e) {}
  }

  // Load notifications on page load (for applicants)
  if (document.getElementById('headerNotifList')) {
    loadHeaderNotifications();
  }
</script>
