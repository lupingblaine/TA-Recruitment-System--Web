<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/auth-check.jsp" %>
<jsp:include page="/common/layout-top.jsp"/>
<jsp:include page="/common/application-period-banner.jsp"/>

<div class="space-y-8 max-w-5xl mx-auto">
  <div>
    <h1 class="text-3xl font-bold tracking-tight text-zinc-900">Application Tracker</h1>
    <p class="text-zinc-500 mt-2">Track the progress of your submitted TA applications.</p>
  </div>

  <div><label for="applicationPeriodFilter" class="text-sm font-medium mr-3">Application period</label><select id="applicationPeriodFilter" class="input w-auto" onchange="loadApplications()"><option value="">All periods</option></select></div>
  <div class="space-y-6" id="appList">
    <div class="text-center text-zinc-400 py-12">Loading...</div>
  </div>
</div>

<jsp:include page="/common/layout-bottom.jsp"/>
<script src="<%= request.getContextPath() %>/static/js/availability-view.js"></script>
<script src="<%= request.getContextPath() %>/static/js/qualification-view.js"></script>
<script>
const ctx = document.querySelector('meta[name="ctx"]').content;
const STEPS = ['applied', 'under_review', 'shortlisted', 'hired'];
const STEP_LABELS = ['Applied', 'Under Review', 'Shortlisted', 'Offer / Rejected'];
const WITHDRAWABLE_STATUSES = ['applied', 'review', 'under_review', 'shortlisted'];

/** Maps API status values (review, offer) to timeline step indices. */
function getStepIndex(status) {
  const map = {
    applied: 0,
    review: 1,
    under_review: 1,
    shortlisted: 2,
    interview: 1,
    offer: 3,
    hired: 3
  };
  const s = (status || '').toLowerCase();
  if (s === 'rejected') return -1;
  return map[s] !== undefined ? map[s] : 0;
}

function escapePeriodName(value) { const span = document.createElement('span'); span.textContent = value; return span.innerHTML; }

function viewSubmittedAvailability(index) {
  const apps = window.submittedApplications || [];
  const container = document.getElementById('submittedAvailability' + index);
  container.replaceChildren();
  QualificationView.render(container,apps[index]?.qualificationSnapshot,apps[index]?.id);
  const availability=document.createElement("section");
  container.appendChild(availability);
  AvailabilityView.render(availability, apps[index]?.availabilitySnapshot);
}

async function loadApplications() {
  try {
    const data = await applicantApi.getApplications();
    const allApps = data.applications || data || [];
    const filter = document.getElementById('applicationPeriodFilter');
    const selected = filter.value;
    filter.replaceChildren(new Option('All periods', ''));
    const periods = new Map(allApps.filter(a => a.periodId).map(a => [a.periodId, a.periodName || a.periodId]));
    periods.forEach((name, id) => filter.add(new Option(name, id)));
    filter.value = selected;
    const apps = selected ? allApps.filter(a => a.periodId === selected) : allApps;
    window.submittedApplications = apps;
    const container = document.getElementById('appList');

    if (apps.length === 0) {
      container.innerHTML = '<div class="text-center text-zinc-400 py-12">No applications yet. <a href="' + ctx + '/applicant/jobs.jsp" class="text-zinc-900 font-medium hover:underline">Browse jobs</a></div>';
      return;
    }

    container.innerHTML = apps.map((original, appIndex) => {
      const app=escapeDisplay(original);
      if (app.status === 'interview') app.status = 'review';
      const isRejected = app.status === 'rejected';
      const currentIdx = isRejected ? STEPS.length - 1 : getStepIndex(app.status);
      const statusBadge = isRejected ? 'badge-danger' : (app.status === 'offer' ? 'badge-success' : 'badge-neutral');
      const statusText = isRejected ? 'Rejected' : (app.status === 'review' || app.status === 'under_review' ? 'Under Review' : (app.status === 'offer' ? 'Offer Received' : 'In Progress'));

      let timelineHTML = '<div class="relative"><div class="absolute top-4 left-0 w-full h-0.5 bg-zinc-200 hidden md:block"></div>' +
        '<div class="absolute top-4 left-0 h-0.5 bg-zinc-900 transition-all duration-1000 ease-out hidden md:block" style="width:' + (currentIdx / (STEPS.length - 1) * 100) + '%"></div>' +
        '<div class="flex flex-col md:flex-row justify-between relative z-10 gap-8 md:gap-4">';

      const isTerminalSuccess = app.status === 'offer' || app.status === 'hired';
      STEPS.forEach((step, i) => {
        const isCompleted = !isRejected && (isTerminalSuccess ? i <= currentIdx : i < currentIdx);
        const isPending = !isRejected && !isTerminalSuccess && i === currentIdx;
        const isError = isRejected && i === STEPS.length - 1;
        const label = isError ? 'Rejected' : STEP_LABELS[i];

        let dotClass, dotInner;
        if (isError) {
          dotClass = 'border-red-500 bg-red-500 text-white'; dotInner = '<i data-lucide="x" class="h-4 w-4"></i>';
        } else if (isCompleted) {
          dotClass = 'border-zinc-900 bg-zinc-900 text-white'; dotInner = '<i data-lucide="check" class="h-4 w-4"></i>';
        } else if (isPending) {
          dotClass = 'border-zinc-900 text-zinc-900'; dotInner = '<div class="h-2.5 w-2.5 rounded-full bg-zinc-900"></div>';
        } else {
          dotClass = 'border-zinc-200 text-zinc-400'; dotInner = '<div class="h-2 w-2 rounded-full bg-zinc-200"></div>';
        }

        timelineHTML += '<div class="flex md:flex-col items-center gap-4 md:gap-2 w-full md:w-auto">' +
          '<div class="h-8 w-8 rounded-full flex items-center justify-center shrink-0 border-2 z-10 bg-white ' + dotClass + '">' + dotInner + '</div>' +
          '<div class="md:text-center mt-1">' +
            '<p class="text-sm font-semibold ' + ((isPending || isCompleted || isError) ? 'text-zinc-900' : 'text-zinc-500') + '">' + label + '</p>' +
          '</div></div>';
      });
      timelineHTML += '</div></div>';

      const canWithdraw = WITHDRAWABLE_STATUSES.includes((app.status || '').toLowerCase());
      const actionsHTML = canWithdraw
        ? '<button class="btn btn-outline text-red-600 border-red-200 hover:bg-red-50 hover:border-red-300" onclick="withdrawApplication(\'' + app.id + '\')">Withdraw</button>'
        : '';

      return '<div class="card overflow-hidden shadow-sm hover:shadow-md transition-shadow fade-in">' +
        '<div class="p-6 sm:p-8 bg-white border-b border-zinc-200 flex flex-col md:flex-row justify-between items-start gap-6">' +
          '<div class="space-y-2">' +
            '<div class="flex items-center gap-3"><span class="badge ' + statusBadge + '">' + statusText + '</span>' +
              '<span class="text-sm font-medium text-zinc-500">Applied ' + (app.appliedDate || '') + '</span></div>' +
            '<h3 class="text-xl font-bold text-zinc-900 mt-2">' + (app.role || app.jobId) + '</h3>' +
            '<p class="text-zinc-500">' + (app.moduleCode || '') + '</p>' +
            '<p class="text-sm text-zinc-500">' + (app.periodName || '') + '</p>' +
          '</div>' +
          '<details class="text-sm" ontoggle="if(this.open)viewSubmittedAvailability(' + appIndex + ')"><summary class="cursor-pointer">Submitted application snapshot</summary><div id="submittedAvailability' + appIndex + '" class="mt-3"></div></details>' +
          actionsHTML +
        '</div>' +
        '<div class="p-6 sm:p-8 bg-zinc-50/50">' + timelineHTML + '</div></div>';
    }).join('');
    lucide.createIcons();
  } catch (e) {
    console.error('Failed to load applications', e);
    document.getElementById('appList').innerHTML = '<div class="text-center text-red-500 py-12">Failed to load applications</div>';
  }
}

async function withdrawApplication(id) {
  if (!confirm('Are you sure you want to withdraw this application?')) {
    return;
  }

  try {
    if (applicantApi.withdrawApplication) {
      await applicantApi.withdrawApplication(id);
    } else {
      await request('/applicant/applications/' + id, { method: 'DELETE' });
    }
    showToast('Application withdrawn');
    loadApplications();
  } catch (e) {
    showToast(e.message || 'Failed to withdraw application', 'error');
  }
}

loadApplications();
</script>
