/* Formal selection creates an assignment draft; publication remains a separate action. */
window.TeacherSelection = (() => {
  const add = (parent, tag, text) => {
    const node = document.createElement(tag);
    if (text != null) node.textContent = text;
    parent.appendChild(node);
    return node;
  };
  function render(container, app, options = {}) {
    container.replaceChildren();
    if (!app || (!app.canSelectTa && !app.selectionAssignmentId)) return;
    container.className = 'card p-5 space-y-3';
    const draft = (id, existing = false) => {
      container.replaceChildren();
      add(container, 'p', existing ? 'TA assignment recorded; view current publication and reply status.' : 'Selected as TA. Assignment saved as a draft; review and publish it to request the applicant’s response.');
      const link = add(container, 'a', existing ? 'View TA assignment' : 'Review assignment draft');
      link.className = 'btn btn-primary';
      link.href = (options.assignmentsPath || window.TeacherSelectionAssignmentsPath || '/organiser/assignments.jsp') + '?assignmentId=' + encodeURIComponent(id);
    };
    if (app.selectionAssignmentId) { draft(app.selectionAssignmentId, true); return; }
    add(container, 'h3', 'Select as TA').className = 'font-bold';
    add(container, 'p', 'Record a formal approval and create a section assignment draft. Matching advice supports your decision.');
    const form = add(container, 'form');
    form.className = 'space-y-3';
    const field = (name, tag, title) => {
      const label = add(form, 'label', title);
      label.className = 'block';
      const input = add(label, tag);
      input.name = name;
      input.className = 'input';
      return input;
    };
    const section = field('sectionId', 'select', 'Section (required)');
    section.required = true;
    add(section, 'option', 'Choose a section').value = '';
    const sections = ((app.scheduleMatching || app.sectionMatching || {}).sections || []);
    for (const item of sections) {
      const option = add(section, 'option', item.name);
      option.value = item.sectionId || item.id;
      option.disabled = item.status === 'conflict';
    }
    const note = field('note', 'textarea', 'Selection rationale (10–4000 characters)');
    note.required = true; note.minLength = 10; note.maxLength = 4000;
    const confirmation = add(form, 'div');
    const confirmLabel = add(confirmation, 'label');
    const confirm = add(confirmLabel, 'input');
    confirm.name = 'confirmPending'; confirm.type = 'checkbox';
    add(confirmLabel, 'span', ' I explicitly confirm the applicant’s availability for this section.');
    const manualLabel = add(confirmation, 'label', 'Manual availability confirmation (10–1000 characters)');
    const manual = add(manualLabel, 'textarea');
    manual.name = 'manualConfirmationNote'; manual.className = 'input'; manual.minLength = 10; manual.maxLength = 1000;
    const selected = () => sections.find(s => String(s.sectionId || s.id) === section.value);
    const needsConfirmation = () => { const item = selected(); return item && item.status !== 'available' && item.status !== 'conflict'; };
    section.onchange = () => {
      const pending = !!needsConfirmation();
      confirmation.hidden = !pending; confirm.required = pending; manual.required = pending;
      if (!pending) { confirm.checked = false; manual.value = ''; }
    };
    section.onchange();
    const button = add(form, 'button', 'Select as TA');
    button.type = 'submit'; button.className = 'btn btn-primary';
    const message = add(form, 'p'); message.setAttribute('role', 'status'); message.setAttribute('aria-live', 'polite');
    let busy = false;
    form.onsubmit = async event => {
      event.preventDefault();
      if (busy) return;
      const item = selected();
      if (!item || item.status === 'conflict') { message.textContent = 'Choose an eligible section.'; return; }
      const rationale = note.value.trim(), manualNote = manual.value.trim();
      if (rationale.length < 10 || rationale.length > 4000) { message.textContent = 'Selection rationale must contain 10–4000 characters.'; return; }
      const pending = !!needsConfirmation();
      if (pending && (!confirm.checked || manualNote.length < 10 || manualNote.length > 1000)) {
        message.textContent = 'Explicitly confirm availability and provide a confirmation note of 10–1000 characters.'; return;
      }
      busy = true; button.disabled = true; section.disabled = true; message.textContent = 'Creating assignment draft…';
      try {
        const result = await request('/review/' + encodeURIComponent(app.id) + '/select-ta', {
          method: 'POST', body: JSON.stringify({sectionId: section.value, note: rationale, confirmPending: pending && confirm.checked, manualConfirmationNote: pending ? manualNote : ''})
        });
        app.canSelectTa = false; app.selectionAssignmentId = result.assignmentId;
        draft(result.assignmentId);
        if (options.onSelected) options.onSelected(result);
      } catch (error) {
        message.textContent = error.message || 'Selection failed. Please try again.';
        busy = false; button.disabled = false; section.disabled = false;
      }
    };
  }
  return {render};
})();
