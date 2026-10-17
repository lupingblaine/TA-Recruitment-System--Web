(() => {
  const el = id => document.getElementById(id);
  const days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
  let editable = false, loading = 0;
  function status(message, error = false) { el('availabilityStatus').textContent = message; el('availabilityStatus').className = 'text-sm ' + (error ? 'text-red-700' : 'text-zinc-600'); }
  function addSlot(kind, value = {}) {
    const row = document.createElement('div'); row.className = 'grid grid-cols-1 sm:grid-cols-2 gap-3 rounded-lg border border-zinc-200 p-3'; row.setAttribute('role', 'group'); row.setAttribute('aria-label', kind === 'availableSlots' ? 'Available time slot' : 'Known conflict');
    const select = document.createElement('select'); select.className = 'input'; select.dataset.field = 'dayOfWeek';
    days.forEach((day, index) => select.add(new Option(day, index + 1))); select.value = value.dayOfWeek || 1;
    function field(label, input) { const wrap = document.createElement('label'); wrap.className = 'text-sm font-medium'; wrap.append(document.createTextNode(label), input); row.appendChild(wrap); }
    field('Day', select);
    for (const [name, label, placeholder] of [['startTime', 'Start time (24h)', '09:00'], ['endTime', 'End time (24h)', '17:00']]) {
      const input = document.createElement('input'); input.className = 'input'; input.type = 'text'; input.placeholder = placeholder; input.required = true; input.maxLength = 5; input.dataset.field = name; input.value = value[name] || '';
      input.pattern = name === 'endTime' ? '([01][0-9]|2[0-3]):[0-5][0-9]|24:00' : '([01][0-9]|2[0-3]):[0-5][0-9]';
      input.title = 'Use 24-hour HH:mm format, such as 09:00 or 17:30.'; field(label, input);
    }
    if (kind === 'conflictSlots') { const input = document.createElement('input'); input.className = 'input'; input.maxLength = 200; input.dataset.field = 'reason'; input.value = value.reason || ''; field('Reason (optional)', input); }
    const remove = document.createElement('button'); remove.type = 'button'; remove.className = 'btn btn-outline justify-self-start'; remove.textContent = 'Remove slot'; remove.addEventListener('click', () => row.remove()); row.appendChild(remove);
    el(kind).appendChild(row);
  }
  function slots(kind) { return [...el(kind).children].map(row => { const result = {}; row.querySelectorAll('[data-field]').forEach(input => result[input.dataset.field] = input.dataset.field === 'dayOfWeek' ? Number(input.value) : input.value.trim()); return result; }); }
  function fill(schedule) {
    el('availableSlots').replaceChildren(); el('conflictSlots').replaceChildren();
    (schedule?.availableSlots || []).forEach(slot => addSlot('availableSlots', slot)); (schedule?.conflictSlots || []).forEach(slot => addSlot('conflictSlots', slot));
    el('noAvailability').checked = !!schedule?.noAvailability; el('availabilityNotes').value = schedule?.notes || ''; el('addAvailable').disabled = !!schedule?.noAvailability;
  }
  async function loadPeriod() {
    const sequence = ++loading; editable = false; el('availabilityFields').disabled = true;
    const id = el('availabilityPeriod').value;
    if (!id) { el('availabilityForm').classList.add('hidden'); status('No published application period is available. An administrator must create and open one first.'); return; }
    status('Loading availability…');
    try {
      const result = await applicantApi.getAvailability(id);
      if (sequence !== loading) return;
      fill(result.availability); editable = result.editable; el('availabilityFields').disabled = !editable; el('availabilityForm').classList.remove('hidden');
      status(editable ? result.availability ? 'Saved schedule loaded. Changes apply to future submissions only.' : 'No schedule saved for this period yet.' : 'This period is not accepting updates. Your saved schedule is read-only.');
    } catch (error) { if (sequence === loading) { el('availabilityForm').classList.add('hidden'); status(error.message || 'Could not load availability.', true); } }
  }
  el('addAvailable').addEventListener('click', () => addSlot('availableSlots'));
  el('addConflict').addEventListener('click', () => addSlot('conflictSlots'));
  el('noAvailability').addEventListener('change', event => {
    if (event.target.checked && el('availableSlots').children.length) { event.target.checked = false; status('Remove available slots first, then confirm that you have no availability.', true); return; }
    el('addAvailable').disabled = event.target.checked;
  });
  el('availabilityPeriod').addEventListener('change', loadPeriod);
  el('availabilityForm').addEventListener('submit', async event => {
    event.preventDefault(); if (!editable) return;
    const id = el('availabilityPeriod').value;
    el('availabilityFields').disabled = true; el('availabilityPeriod').disabled = true;
    try {
      const saved = await applicantApi.saveAvailability(id, {timeZone:'America/Los_Angeles', availableSlots:slots('availableSlots'), conflictSlots:slots('conflictSlots'), noAvailability:el('noAvailability').checked, notes:el('availabilityNotes').value.trim()});
      fill(saved); status('Availability saved. Existing submitted applications keep their original snapshot.');
    } catch (error) { status(error.message || 'Could not save availability.', true); }
    finally { el('availabilityFields').disabled = !editable; el('availabilityPeriod').disabled = false; }
  });
  applicantApi.getAvailabilityPeriods().then(async periods => {
    el('availabilityPeriod').replaceChildren(new Option('Choose an application period', ''));
    periods.forEach(p => el('availabilityPeriod').add(new Option(p.name + ' · ' + p.effectiveStatus, p.id)));
    const requested = new URLSearchParams(location.search).get('periodId');
    el('availabilityPeriod').value = periods.some(p => p.id === requested) ? requested : (periods.find(p => p.editable) || periods[periods.length - 1])?.id || '';
    el('availabilityPeriod').disabled = false; await loadPeriod();
  }).catch(error => status(error.message || 'Could not load periods.', true));
})();
