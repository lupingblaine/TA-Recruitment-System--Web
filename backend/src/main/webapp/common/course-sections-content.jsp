<%@ page pageEncoding="UTF-8" %>
<div class="max-w-6xl mx-auto space-y-6">
  <div class="flex flex-wrap items-start justify-between gap-4"><div><h1 class="text-3xl font-bold">Course Sections</h1><p class="text-zinc-500 mt-2">Maintain lecture, lab and discussion schedules and TA staffing needs.</p></div><button id="newSection" class="btn btn-primary" disabled>Add section</button></div>
  <p class="text-sm text-zinc-600">Weekly meetings use America/Los_Angeles (UCI local time). TA hours below are per person per week. This page records staffing demand; it does not assign students.</p>
  <div><label for="sectionCourse" class="block text-sm font-medium mb-2">Course</label><select id="sectionCourse" class="input" disabled><option value="">Loading courses…</option></select></div>
  <label class="block">Application cycle<select id="sectionPeriod" class="input"><option value="legacy_unscoped">Historical unscoped sections (copy before reuse)</option></select></label>
  <p id="sectionCourseInfo" class="text-sm text-zinc-600"></p>
  <p id="sectionMessage" role="status" aria-live="polite" class="text-sm"></p>
  <section id="sectionFormPanel" class="card p-6 hidden" aria-labelledby="sectionFormTitle">
    <h2 id="sectionFormTitle" class="text-xl font-semibold mb-4">Add section</h2>
    <form id="sectionForm" class="space-y-5"><fieldset id="sectionFields" class="space-y-5">
      <label id="sectionCopyTargetLabel" class="block hidden">Target cycle for this copy<select id="sectionCopyTarget" class="input"></select></label>
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div><label for="sectionName" class="block text-sm font-medium mb-1">Section name</label><input id="sectionName" class="input" required maxlength="120" placeholder="Lab A"></div>
        <div><label for="sectionType" class="block text-sm font-medium mb-1">Type</label><select id="sectionType" class="input"><option value="lecture">Lecture</option><option value="lab">Lab</option><option value="discussion">Discussion</option></select></div>
        <div><label for="sectionPositions" class="block text-sm font-medium mb-1">TA positions requested</label><input id="sectionPositions" type="number" class="input" required min="0" max="1000" step="1" value="1"></div>
        <div><label for="sectionHours" class="block text-sm font-medium mb-1">Hours per TA per week</label><input id="sectionHours" type="number" class="input" required min="0" max="168" step="any" value="10"></div>
        <div class="sm:col-span-2"><label for="sectionFaculty">Responsible faculty (this cycle)</label><input id="sectionFaculty" class="input" maxlength="200"></div>
        <div class="sm:col-span-2"><label for="sectionLocation" class="block text-sm font-medium mb-1">Location (optional)</label><input id="sectionLocation" class="input" maxlength="200" placeholder="Building / room"></div>
      </div>
      <p class="text-sm text-zinc-500">If no TA is needed, set both positions and hours to 0. This is separate from the existing job posting's position count.</p>
      <section aria-labelledby="sectionMeetingsHeading"><h3 id="sectionMeetingsHeading" class="font-semibold">Weekly meeting times</h3><p class="text-sm text-zinc-500">Use 24-hour HH:mm times. Add multiple meetings for a section that meets on several days. Split overnight meetings across two days; an end time of 24:00 is allowed.</p><div id="sectionMeetings" class="space-y-3 my-3"></div><button id="addSectionMeeting" type="button" class="btn btn-outline">Add meeting time</button></section>
      <div><label for="sectionNotes" class="block text-sm font-medium mb-1">Notes (optional)</label><textarea id="sectionNotes" rows="3" maxlength="2000" class="input"></textarea></div>
      <fieldset class="space-y-3"><legend class="font-semibold">Section skills and training</legend><label class="flex gap-2"><input id="sectionOwnCriteria" type="checkbox">Use Section-specific requirements (otherwise position criteria apply)</label><label class="block">Required skills (one per line)<textarea id="sectionRequiredSkills" class="input"></textarea></label><label class="block">Preferred skills (one per line)<textarea id="sectionPreferredSkills" class="input"></textarea></label><label class="block">Additional skill options for students (one per line)<textarea id="sectionSkillOptions" class="input"></textarea></label><label class="flex gap-2"><input id="sectionTrainingRequired" type="checkbox">TA Training completed required</label><label class="flex gap-2"><input id="sectionSexualTrainingRequired" type="checkbox">Online Sexual Harassment Training completed required</label></fieldset>
      <div class="flex gap-3"><button id="saveSection" type="submit" class="btn btn-primary">Save section</button><button id="cancelSection" type="button" class="btn btn-outline">Cancel</button></div>
    </fieldset></form>
  </section>
  <div class="flex flex-wrap items-center justify-between gap-3"><p id="sectionSummary" class="text-sm font-medium text-zinc-700"></p><label class="text-sm flex gap-2 items-center"><input id="showArchivedSections" type="checkbox">Show archived sections</label></div>
  <section class="card overflow-x-auto" aria-label="Course sections"><table class="w-full text-sm text-left"><thead class="bg-zinc-50 border-b border-zinc-200"><tr><th class="p-4">Section</th><th class="p-4">Weekly meetings</th><th class="p-4">TA demand</th><th class="p-4">Location / notes</th><th class="p-4">Actions</th></tr></thead><tbody id="sectionRows"><tr><td colspan="5" class="p-6">Choose a course.</td></tr></tbody></table></section>
</div>
