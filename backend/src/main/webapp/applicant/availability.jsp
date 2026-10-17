<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/auth-check.jsp" %>
<jsp:include page="/common/layout-top.jsp"/>
<div class="max-w-5xl mx-auto space-y-6">
  <div><h1 class="text-3xl font-bold">Weekly Availability</h1><p class="text-zinc-500 mt-2">Record when you can work during this application period.</p></div>
  <p class="text-sm text-zinc-600">All times use America/Los_Angeles (UCI local time), in 24-hour HH:mm format. Add separate slots for each day. End times may use 24:00 for midnight; split overnight slots across two days.</p>
  <div><label for="availabilityPeriod" class="block text-sm font-medium mb-2">Application period</label><select id="availabilityPeriod" class="input" disabled><option value="">Loading periods…</option></select></div>
  <p id="availabilityStatus" role="status" aria-live="polite" class="text-sm text-zinc-600"></p>
  <form id="availabilityForm" class="card p-6 hidden">
    <fieldset id="availabilityFields" class="space-y-6" disabled>
      <legend class="font-semibold text-lg mb-4">Weekly working times</legend>
      <label class="flex gap-2 items-center text-sm"><input id="noAvailability" type="checkbox"> I have no available working times this period</label>
      <section aria-labelledby="availableHeading"><h2 id="availableHeading" class="font-semibold">Available working times</h2><div id="availableSlots" class="space-y-3 my-3"></div><button id="addAvailable" type="button" class="btn btn-outline">Add available time</button></section>
      <section aria-labelledby="conflictsHeading"><h2 id="conflictsHeading" class="font-semibold">Known conflicts</h2><p class="text-sm text-zinc-500">For example, classes or other commitments. Conflicts must not overlap the available times above.</p><div id="conflictSlots" class="space-y-3 my-3"></div><button id="addConflict" type="button" class="btn btn-outline">Add conflict</button></section>
      <div><label for="availabilityNotes" class="block text-sm font-medium mb-2">Other restrictions or notes</label><textarea id="availabilityNotes" class="input" rows="3" maxlength="2000" placeholder="Other scheduling constraints or comments"></textarea></div>
      <p class="text-sm text-zinc-500">Saving updates your schedule for future submissions. Each submitted application keeps its own copy; it will not change when you edit this schedule.</p>
      <button id="saveAvailability" type="submit" class="btn btn-primary">Save availability</button>
    </fieldset>
  </form>
</div>
<jsp:include page="/common/layout-bottom.jsp"/>
<script src="<%= request.getContextPath() %>/static/js/availability.js"></script>
