(() => {
  const form = document.getElementById('qualificationForm');
  const fields = document.getElementById('qualificationFields');
  const message = document.getElementById('qualificationMessage');
  const retry = document.getElementById('qualificationRetry');
  const save = document.getElementById('qualificationSave');
  const errors = document.getElementById('qualificationErrors');
  const errorList = document.getElementById('qualificationErrorList');
  const input = name => form.elements.namedItem(name);
  const view = document.getElementById('qualificationView');
  const edit = document.getElementById('qualificationEdit');
  const cancel = document.getElementById('qualificationCancel');
  let source = {}, busy = false, loaded = false, editing = false;
  const names = {
    firstName: 'First name', lastName: 'Last name', uciEmail: 'UCI email',
    uciStudentId: 'UCI student ID', studentLevel: 'Student level', program: 'Academic program',
    graduateStartYear: 'Graduate program start year', facultyPI: 'Faculty / PI',
    taQuarters: 'Previous TA quarters at UCI', advancedToCandidacy: 'Candidacy status',
    trainingStatus: 'TA Training status', sexualHarassmentTrainingCompleted: 'Sexual Harassment Training status',
    additionalEmployment: 'Additional on-campus employment', documentedAccommodation: 'DSC accommodation status',
    ackSchedule: 'Schedule acknowledgement', ackEnrollment: 'Enrollment and fees acknowledgement',
    acknowledged: 'Information accuracy acknowledgement'
  };

  function status(text, state = 'ready') {
    message.textContent = text;
    document.getElementById('qualificationSaveHint').textContent = text;
    message.parentElement.dataset.state = state;
  }
  function setBusy(value) {
    busy = value;
    fields.disabled = value || !loaded || !editing;
    edit.disabled = value;
    cancel.disabled = value;
    retry.disabled = value;
    form.setAttribute('aria-busy', String(value));
    save.textContent = value && loaded ? 'Saving profile…' : 'Save application profile';
  }
  function focus(target) {
    target.focus();
    target.scrollIntoView({block: 'center'});
  }
  document.querySelectorAll('.profile-nav a').forEach(link => {
    link.dataset.section = link.hash.slice(1);
    link.addEventListener('click', event => {
      const target = document.getElementById(link.hash.slice(1));
      if (target) { event.preventDefault(); focus(target); }
    });
  });
  function updateGraduateYear() {
    const required = input('studentLevel').value !== 'undergraduate';
    input('graduateStartYear').required = required;
    document.getElementById('graduateYearRequirement').textContent = required ? 'Required' : 'Optional';
    document.getElementById('graduateYearHint').textContent = required
      ? 'Required for your student level. Enter the year you started your graduate program.'
      : 'Optional for undergraduate students. Leave blank if it does not apply.';
  }
  function updateEmailValidity() {
    const email = input('uciEmail');
    email.setCustomValidity(email.value && !/^[^\s@]+@(?:[a-z0-9-]+\.)*uci\.edu$/i.test(email.value)
      ? 'Enter your UCI email address, ending in @uci.edu or a UCI subdomain.' : '');
  }
  function validationMessage(control) {
    if (control.name === 'uciStudentId' && control.value) return 'Enter exactly 8 digits.';
    if (control.validity.valueMissing) return control.type === 'checkbox' ? 'Please confirm this acknowledgement.' : 'Complete this required field.';
    return control.validationMessage;
  }
  function validate(focusErrors = true) {
    updateEmailValidity();
    errorList.replaceChildren();
    for (const control of form.elements) {
      if (!control.willValidate) continue;
      const invalid = !control.validity.valid;
      control.toggleAttribute('aria-invalid', false);
      if (!invalid) continue;
      control.setAttribute('aria-invalid', 'true');
      const item = document.createElement('li');
      const link = document.createElement('a');
      link.href = '#' + control.id;
      link.textContent = (names[control.name] || control.name) + ': ' + validationMessage(control);
      link.addEventListener('click', event => { event.preventDefault(); focus(control); });
      item.appendChild(link);
      errorList.appendChild(item);
    }
    errors.hidden = !errorList.children.length;
    if (!errors.hidden && focusErrors) focus(errors);
    return errors.hidden;
  }
  function choices(id, items, selected, name, emptyMessage) {
    const container = document.getElementById(id);
    container.replaceChildren();
    if (!items.length) {
      const note = document.createElement('p');
      note.textContent = emptyMessage;
      container.appendChild(note);
    }
    for (const [value, label] of items) {
      const wrapper = document.createElement('label');
      const checkbox = document.createElement('input');
      checkbox.type = 'checkbox'; checkbox.name = name; checkbox.value = value;
      checkbox.checked = selected.includes(value);
      const text = document.createElement('span'); text.textContent = label;
      wrapper.append(checkbox, text); container.appendChild(wrapper);
    }
  }
  function hydrate(q) {
    form.reset();
    for (const [key, value] of Object.entries(q)) {
      const control = input(key);
      if (!control || !control.tagName) continue;
      if (control.type === 'checkbox') control.checked = value === true;
      else control.value = value == null ? '' : String(value);
    }
    for (const name of ['skills', 'interests']) {
      form.querySelectorAll('input[name="' + name + '"]').forEach(control => {
        control.checked = (q[name] || []).includes(control.value);
      });
    }
    for (const control of form.elements) control.removeAttribute('aria-invalid');
    errors.hidden = true;
    errorList.replaceChildren();
    updateGraduateYear(); updateEmailValidity();
  }
  function renderView() {
    view.replaceChildren();
    form.querySelectorAll('.profile-section').forEach(section => {
      const card = document.createElement('section'); card.className = 'profile-section';
      const heading = document.createElement('h2');
      heading.id = 'view-' + section.querySelector('h2').id;
      heading.tabIndex = -1; heading.textContent = section.querySelector('h2').textContent.replace('Confirm and save', 'Acknowledgements');
      card.setAttribute('aria-labelledby', heading.id);
      card.appendChild(heading);
      const list = document.createElement('dl'); list.className = 'profile-details';
      function detail(label, value) {
        const item = document.createElement('div');
        const title = document.createElement('dt'); title.textContent = label;
        const text = document.createElement('dd'); text.textContent = value || 'Not provided';
        item.append(title, text); list.appendChild(item);
      }
      section.querySelectorAll('label').forEach(label => {
        const control = label.querySelector('input, select, textarea');
        if (!control || ['skills', 'interests'].includes(control.name)) return;
        const title = label.cloneNode(true);
        title.querySelectorAll('input, select, textarea, small, .profile-required, .profile-optional').forEach(node => node.remove());
        const supplied = source[control.name] !== undefined && source[control.name] !== null && source[control.name] !== '';
        const value = !supplied ? '' : control.type === 'checkbox' ? (control.checked ? 'Confirmed' : 'Not confirmed')
          : control.tagName === 'SELECT' ? (control.value ? control.selectedOptions[0].textContent : '') : control.value;
        detail(title.textContent.trim(), value);
      });
      section.querySelectorAll('.profile-choices').forEach(group => {
        const title = group.querySelector('legend').cloneNode(true);
        title.querySelectorAll('span').forEach(node => node.remove());
        const selected = [...group.querySelectorAll('input:checked')].map(control => control.value);
        detail(title.textContent.trim(), selected.length ? selected.join(', ') : 'None selected');
      });
      card.appendChild(list); view.appendChild(card);
    });
  }
  function setMode(value) {
    editing = value;
    form.hidden = !value;
    view.hidden = value || !loaded;
    edit.hidden = value || !loaded;
    edit.setAttribute('aria-expanded', String(value));
    cancel.hidden = !Object.keys(source).length;
    document.querySelectorAll('.profile-nav a').forEach(link => {
      link.href = '#' + (value ? '' : 'view-') + link.dataset.section;
      if (link.dataset.section === 'profile-confirmation') link.textContent = value ? '5. Confirm & save' : '5. Acknowledgements';
    });
    setBusy(busy);
  }
  edit.addEventListener('click', () => {
    if (busy || !loaded) return;
    hydrate(source); setMode(true);
    status('Editing your application profile. Save your changes or cancel to keep your saved details.');
    focus(input('firstName'));
  });
  cancel.addEventListener('click', () => {
    if (busy) return;
    hydrate(source); renderView(); setMode(false);
    status('Changes discarded. Your saved application profile is unchanged.');
    focus(edit);
  });
  async function load() {
    if (busy) return;
    setBusy(true);
    retry.hidden = true;
    status('Loading your application profile…', 'loading');
    try {
      const [q, options] = await Promise.all([
        request('/applicant/qualifications'), request('/applicant/qualification-options')
      ]);
      source = q;
      hydrate(q);
      choices('qualificationSkills', options.skills.map(s => [s, s]), q.skills || [], 'skills',
        'No skills are available yet. You can save your profile and return when course staff add options.');
      const courses = new Map(options.courses.map(c => [c.code, c.code + ' · ' + c.name]));
      (q.interests || []).forEach(code => { if (!courses.has(code)) courses.set(code, code + ' (previously selected)'); });
      choices('qualificationInterests', [...courses], q.interests || [], 'interests',
        'No courses are available yet. You can save your profile and return when courses are listed.');
      updateGraduateYear(); updateEmailValidity();
      loaded = true;
      renderView(); setMode(!Object.keys(source).length);
      status(editing ? 'Complete your application profile and save your details.' : 'Your saved application profile. Select Edit to make changes.');
    } catch (error) {
      status('Could not load your profile. ' + (error.message || 'Please try again.'), 'error');
      retry.hidden = false;
    } finally { setBusy(false); }
  }
  form.noValidate = true;
  form.addEventListener('input', () => {
    if (!loaded || busy) return;
    updateGraduateYear(); updateEmailValidity();
    if (!errors.hidden) validate(false);
    status('Unsaved changes. Save your application profile before leaving this page.', 'dirty');
  });
  // Selects and checkboxes also emit change for keyboard and assistive technology use.
  form.addEventListener('change', () => {
    if (!loaded || busy) return;
    updateGraduateYear(); updateEmailValidity();
    if (!errors.hidden) validate(false);
    status('Unsaved changes. Save your application profile before leaving this page.', 'dirty');
  });
  form.addEventListener('submit', async event => {
    event.preventDefault();
    if (busy || !loaded || !editing) return;
    if (!validate()) {
      status('Your profile has not been saved. Review the highlighted fields.', 'error');
      return;
    }
    // Collect before disabling the fieldset: disabled controls are omitted by FormData.
    const data = new FormData(form), q = Object.fromEntries(data);
    q.skills = data.getAll('skills'); q.interests = data.getAll('interests');
    ['ackSchedule', 'ackEnrollment', 'acknowledged'].forEach(key => { q[key] = input(key).checked; });
    ['documentedAccommodation', 'sexualHarassmentTrainingCompleted'].forEach(key => { q[key] = q[key] === 'true'; });
    q.taQuarters = Number(q.taQuarters);
    if (q.graduateStartYear) q.graduateStartYear = Number(q.graduateStartYear);
    if (source.testingScenario) q.testingScenario = source.testingScenario;
    setBusy(true); status('Saving your application profile…', 'saving');
    try {
      const saved = await request('/applicant/qualifications', {method: 'PUT', body: JSON.stringify(q)});
      source = saved && typeof saved === 'object' && !Array.isArray(saved) ? saved : q;
      hydrate(source); renderView(); setMode(false);
      status('Application profile saved. Previously submitted applications retain their original data.', 'saved');
    } catch (error) {
      status((error.message || 'Could not save your profile.') + ' Your edits are still here. Please try saving again.', 'error');
    } finally {
      setBusy(false);
      if (!editing) focus(edit);
    }
  });
  retry.addEventListener('click', load);
  load();
})();
