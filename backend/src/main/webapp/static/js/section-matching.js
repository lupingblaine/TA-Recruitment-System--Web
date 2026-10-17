/* Shared, escaped rendering for server-calculated scheduling advice. */
window.SectionMatching = (() => {
  const labels = {available: 'Available', conflict: 'Time conflict', pending: 'Needs confirmation'};
  const days = ['', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
  const escape = value => String(value == null ? '' : value).replace(/[&<>"']/g, c => ({'&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;'}[c]));
  function selected(app, sectionId) {
    const match = app.sectionMatching || {status: 'pending', sections: []};
    return sectionId ? (match.sections || []).find(s => s.sectionId === sectionId) : match;
  }
  function badge(match) {
    const status = match && labels[match.status] ? match.status : 'pending';
    const color = {available:'text-emerald-700 bg-emerald-50', conflict:'text-red-700 bg-red-50', pending:'text-amber-700 bg-amber-50'}[status];
    return '<span class="inline-block rounded px-2 py-1 text-xs font-medium ' + color + '">' + labels[status] + '</span>';
  }
  function render(container, match) {
    container.className = 'card p-6 space-y-4';
    container.innerHTML = '<h2 class="text-lg font-bold">Section time matching</h2><p class="text-sm text-zinc-500">' + escape(match && match.basis || 'Submitted snapshot compared with current active sections.') + '</p>' +
      '<p class="text-sm">Time zone: America/Los_Angeles. Available means at least one section fits; review each section below. Sections with zero TA positions are shown for schedule reference.</p>' +
      '<p>' + badge(match) + ' ' + escape(match && match.message) + '</p>' +
      ((match && match.sections) || []).map(s => '<article class="border rounded-lg p-4 space-y-2"><h3 class="font-semibold">' + escape(s.name) + ' · ' + escape(s.type) + ' ' + badge(s) + '</h3>' +
        '<p class="text-sm">TA positions: ' + escape(s.taPositions) + '</p><p class="text-sm">' +
        (s.meetingTimes || []).map(m => escape(days[m.dayOfWeek]) + ' ' + escape(m.startTime) + '–' + escape(m.endTime)).join(' · ') + '</p><p class="text-sm">' + escape(s.message) + '</p>' + (s.qualificationMatching?'<p class="text-sm">Section qualifications: '+escape(s.qualificationMatching.status.replaceAll('_',' '))+'</p><ul class="text-sm list-disc pl-5">'+[...(s.qualificationMatching.missingRequired||[]),...(s.qualificationMatching.unmatchedPreferred||[])].map(x=>'<li>'+escape(x)+'</li>').join('')+'</ul>':'') +
        ((s.issues || []).length ? '<ul class="text-sm text-red-700 list-disc pl-5">' + s.issues.map(i => '<li>' + escape(days[i.dayOfWeek]) + ' ' + escape(i.startTime) + '–' + escape(i.endTime) + ': ' + escape(i.reason) + '</li>').join('') + '</ul>' : '') + '</article>').join('');
  }
  return {selected, badge, render};
})();
