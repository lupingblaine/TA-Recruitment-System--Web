(() => {
  let periods = [];
  let editing = null;
  const el = id => document.getElementById(id);
  const escape = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
  const displayTime = value => new Date(value).toLocaleString('en-US', {dateStyle:'medium', timeStyle:'short', hour12:false});
  // Always use the bundled English picker, including on mobile browsers.
  const dateOptions = {
    locale: 'en', enableTime: true, time_24hr: true, disableMobile: true,
    dateFormat: 'M j, Y H:i', ariaDateFormat: 'F j, Y', minuteIncrement: 1,
    allowInput: false
  };
  const openingPicker = flatpickr(el('periodOpens'), {...dateOptions});
  const deadlinePicker = flatpickr(el('periodCloses'), {...dateOptions});
  function message(text, error = false) {
    el('periodMessage').textContent = text;
    el('periodMessage').className = 'text-sm ' + (error ? 'text-red-700' : 'text-emerald-700');
  }
  async function load() {
    periods = await adminApi.getApplicationPeriods();
    const rows = el('periodRows');
    rows.innerHTML = '';
    if (!periods.length) rows.innerHTML = '<tr><td colspan="5" class="p-6 text-zinc-500">No periods yet. Create a draft to get started.</td></tr>';
    for (const p of periods) {
      const row = document.createElement('tr');
      row.className = 'border-t border-zinc-100';
      row.innerHTML = '<td class="p-4 font-medium">' + escape(p.name) + '<div class="font-normal text-zinc-500">' + escape(p.termName || 'Historical records') + '</div></td>' +
        '<td class="p-4">' + (p.legacy ? 'Before period management' : escape(displayTime(p.opensAt)) + '<br>to ' + escape(displayTime(p.closesAt))) + '</td>' +
        '<td class="p-4"><span class="badge ' + (p.accepting ? 'badge-success' : 'badge-neutral') + '">' + escape(p.effectiveStatus) + '</span></td>' +
        '<td class="p-4">' + p.applicationCount + '</td><td class="p-4"><div class="flex flex-wrap gap-2"></div></td>';
      const buttons = row.lastElementChild.firstElementChild;
      function button(label, callback) {
        const btn = document.createElement('button');
        btn.className = 'btn btn-outline text-xs'; btn.textContent = label;
        btn.addEventListener('click', callback); buttons.appendChild(btn);
      }
      button('View applications', () => history(p));
      if (!p.legacy) {
        button('Edit', () => form(p));
        const action = p.status === 'open' ? 'close' : 'open';
        button(action === 'close' ? 'Close intake' : p.status === 'closed' ? 'Reopen' : 'Open intake', async event => {
          event.currentTarget.disabled = true;
          try {
            await adminApi.changeApplicationPeriodStatus(p.id, action);
            await load(); message(action === 'close' ? 'Intake closed. Existing applications remain available.' : 'Period enabled. Submissions are accepted only within its dates.');
          } catch (error) { message(error.message, true); event.currentTarget.disabled = false; }
        });
      }
      rows.appendChild(row);
    }
  }
  function form(period) {
    editing = period ? period.id : null;
    el('periodForm').reset();
    openingPicker.clear(); deadlinePicker.clear();
    el('periodFormTitle').textContent = editing ? 'Edit application period' : 'Create application period';
    el('savePeriod').textContent = editing ? 'Save changes' : 'Save draft';
    if (period) {
      el('periodName').value = period.name; el('periodTerm').value = period.termId;
      openingPicker.setDate(new Date(period.opensAt)); deadlinePicker.setDate(new Date(period.closesAt));
    }
    el('periodFormSection').classList.remove('hidden'); el('periodName').focus();
  }
  async function history(period) {
    el('periodHistory').classList.remove('hidden');
    el('periodHistoryTitle').textContent = 'Applications · ' + period.name;
    el('periodHistoryTitle').focus();
    el('historyRows').innerHTML = '<tr><td colspan="5" class="p-3">Loading…</td></tr>';
    try {
      const apps = await adminApi.getPeriodApplications(period.id);
      el('historyRows').innerHTML = apps.length ? apps.map((a, index) => '<tr class="border-t border-zinc-100"><td class="p-3">' + escape(a.applicantName) + '<br>' + escape(a.email) + '</td><td class="p-3">' + escape(a.moduleCode) + ' · ' + escape(a.jobTitle) + '</td><td class="p-3">' + escape(a.status) + '</td><td class="p-3">' + escape(a.appliedAt) + '</td><td class="p-3"><details><summary class="cursor-pointer">Submitted schedule</summary><div data-snapshot-index="' + index + '" class="mt-2 min-w-64"></div></details></td></tr>').join('') : '<tr><td colspan="5" class="p-3 text-zinc-500">No applications in this period.</td></tr>';
      el('historyRows').querySelectorAll('[data-snapshot-index]').forEach(container => AvailabilityView.render(container, apps[Number(container.dataset.snapshotIndex)].availabilitySnapshot));
    } catch (error) { el('historyRows').textContent = 'Could not load applications.'; message(error.message, true); }
  }
  el('newPeriod').addEventListener('click', () => form(null));
  el('cancelPeriod').addEventListener('click', () => el('periodFormSection').classList.add('hidden'));
  el('periodForm').addEventListener('submit', async event => {
    event.preventDefault();
    el('savePeriod').disabled = true;
    try {
      const opensAt = openingPicker.selectedDates[0];
      const closesAt = deadlinePicker.selectedDates[0];
      if (!opensAt || !closesAt) throw new Error('Select both the opening date and the deadline.');
      if (opensAt >= closesAt) throw new Error('Opening time must be before the deadline.');
      const data = {name:el('periodName').value.trim(), termId:el('periodTerm').value,
        opensAt:opensAt.toISOString(), closesAt:closesAt.toISOString()};
      if (editing) await adminApi.updateApplicationPeriod(editing, data);
      else await adminApi.createApplicationPeriod(data);
      el('periodFormSection').classList.add('hidden');
      await load(); message(editing ? 'Period updated.' : 'Draft saved. Use Open intake when ready.');
    } catch (error) { message(error.message || 'Could not save period.', true); }
    finally { el('savePeriod').disabled = false; }
  });
  el('periodTimezone').textContent = 'Dates are entered and displayed in ' + Intl.DateTimeFormat().resolvedOptions().timeZone + '.';
  Promise.all([adminApi.getTerms(), load()]).then(([terms]) => {
    el('periodTerm').innerHTML = '<option value="">Choose an academic term</option>' + terms.map(t => '<option value="' + escape(t.id) + '">' + escape(t.name) + '</option>').join('');
  }).catch(error => { message(error.message || 'Could not load periods.', true); el('periodRows').textContent = 'Could not load periods. Please refresh.'; });
})();
