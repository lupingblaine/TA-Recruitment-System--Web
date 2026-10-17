<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/auth-check.jsp" %>
<jsp:include page="/common/layout-top.jsp"/>
<link rel="stylesheet" href="<%= request.getContextPath() %>/static/vendor/flatpickr/flatpickr.min.css">
<div class="max-w-6xl mx-auto space-y-6">
  <div class="flex flex-wrap items-start justify-between gap-4">
    <div><h1 class="text-3xl font-bold text-zinc-900">Application Periods</h1>
      <p class="text-zinc-500 mt-2">Manage intake windows and review applications by cycle.</p></div>
    <button id="newPeriod" class="btn btn-primary">Create period</button>
  </div>
  <p class="text-sm text-zinc-600">One period can be open or scheduled at a time. It applies to all active TA positions. Closing intake keeps existing applications available for review.</p>
  <p id="periodMessage" role="status" aria-live="polite" class="text-sm"></p>
  <section id="periodFormSection" class="card p-6 hidden" aria-labelledby="periodFormTitle">
    <h2 id="periodFormTitle" class="text-xl font-semibold mb-4">Create application period</h2>
    <form id="periodForm" class="grid grid-cols-1 sm:grid-cols-2 gap-4">
      <div><label for="periodName" class="block text-sm font-medium mb-1">Name</label><input id="periodName" class="input" required maxlength="120" placeholder="Fall 2026 TA Applications"></div>
      <div><label for="periodTerm" class="block text-sm font-medium mb-1">Academic term</label><select id="periodTerm" class="input" required></select></div>
      <div><label for="periodOpens" class="block text-sm font-medium mb-1">Opening date and time</label><input id="periodOpens" type="text" class="input" placeholder="Select date and time" autocomplete="off" aria-describedby="periodTimezone" required></div>
      <div><label for="periodCloses" class="block text-sm font-medium mb-1">Deadline</label><input id="periodCloses" type="text" class="input" placeholder="Select date and time" autocomplete="off" aria-describedby="periodTimezone" required></div>
      <p id="periodTimezone" class="text-xs text-zinc-500 sm:col-span-2"></p>
      <div class="flex gap-3 sm:col-span-2"><button id="savePeriod" type="submit" class="btn btn-primary">Save draft</button><button id="cancelPeriod" type="button" class="btn btn-outline">Cancel</button></div>
    </form>
  </section>
  <section class="card overflow-x-auto" aria-label="Application periods">
    <table class="w-full text-sm text-left"><thead class="bg-zinc-50 border-b border-zinc-200"><tr>
      <th class="p-4">Period / term</th><th class="p-4">Window</th><th class="p-4">Status</th><th class="p-4">Applications</th><th class="p-4">Actions</th>
    </tr></thead><tbody id="periodRows"><tr><td colspan="5" class="p-6">Loading…</td></tr></tbody></table>
  </section>
  <section id="periodHistory" class="card p-6 hidden" aria-labelledby="periodHistoryTitle">
    <h2 id="periodHistoryTitle" class="text-xl font-semibold mb-4" tabindex="-1">Applications</h2>
    <div class="overflow-x-auto"><table class="w-full text-sm text-left"><thead><tr><th class="p-3">Applicant</th><th class="p-3">Position</th><th class="p-3">Status</th><th class="p-3">Submitted</th><th class="p-3">Availability</th></tr></thead><tbody id="historyRows"></tbody></table></div>
  </section>
</div>
<jsp:include page="/common/layout-bottom.jsp"/>
<script src="<%= request.getContextPath() %>/static/js/availability-view.js"></script>
<script src="<%= request.getContextPath() %>/static/vendor/flatpickr/flatpickr.min.js"></script>
<script src="<%= request.getContextPath() %>/static/js/application-periods.js?v=weekly-availability-1"></script>
