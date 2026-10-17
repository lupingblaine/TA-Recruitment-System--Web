// Render only the supplied saved schedule or submission snapshot, never a live profile fallback.
window.AvailabilityView = {
  render(container, schedule) {
    container.replaceChildren();
    const line = text => { const p = document.createElement('p'); p.textContent = text; p.className = 'text-sm text-zinc-600 whitespace-pre-wrap'; container.appendChild(p); };
    if (!schedule) { line('No weekly availability was recorded for this application.'); return; }
    const days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
    line('Timezone: ' + schedule.timeZone + ' (UCI local time)');
    if(schedule.conflictsOnly)line('Imported reported conflicts only. Full weekly availability has not been confirmed.');
    if (schedule.noAvailability) line('The applicant confirmed no available working times for this period.');
    function slots(title, rows) {
      const heading = document.createElement('h4'); heading.className = 'font-medium text-sm mt-3'; heading.textContent = title; container.appendChild(heading);
      if (!rows?.length) line('None recorded.');
      (rows || []).forEach(slot => line(days[slot.dayOfWeek - 1] + ' · ' + slot.startTime + '–' + slot.endTime + (slot.reason ? ' · ' + slot.reason : '')));
    }
    slots('Available working times', schedule.availableSlots);
    slots('Known conflicts', schedule.conflictSlots);
    if (schedule.notes) { const label = document.createElement('h4'); label.textContent = 'Restrictions / notes'; label.className = 'font-medium text-sm mt-3'; container.appendChild(label); line(schedule.notes); }
  }
};
