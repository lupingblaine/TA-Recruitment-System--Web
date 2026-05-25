<%@ page pageEncoding="UTF-8" %>
  <%@ include file="/common/auth-check.jsp" %>
    <jsp:include page="/common/layout-top.jsp" />

    <div class="space-y-8 max-w-4xl mx-auto">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold tracking-tight text-zinc-900">Profile</h1>
          <p class="text-zinc-500 mt-2">Manage your reusable applicant profile.</p>
        </div>
        <div class="text-right">
          <span class="text-sm font-medium text-emerald-600" id="completionPct">-</span>
          <div class="mt-2 h-2 w-32 bg-zinc-100 rounded-full overflow-hidden">
            <div class="h-full bg-emerald-500 transition-all" id="completionBar" style="width:0%"></div>
          </div>
        </div>
      </div>

      <div class="grid gap-8" id="profileContent">
        <!-- Personal Information -->
        <div class="card fade-in">
          <div class="p-6 border-b border-zinc-100 flex justify-between items-center">
            <h2 class="text-xl font-semibold flex items-center gap-2 text-zinc-900">
              <i data-lucide="user" class="h-5 w-5 text-zinc-500"></i> Personal Information
            </h2>
            <button onclick="toggleEdit('personal')" class="btn btn-ghost text-sm gap-2">
              <i data-lucide="edit-2" class="h-4 w-4"></i> Edit
            </button>
          </div>
          <div class="p-6 grid grid-cols-1 md:grid-cols-2 gap-6" id="personalView">
            <div class="space-y-2"><label class="text-sm font-medium text-zinc-500">Full Name</label>
              <p class="font-medium text-zinc-900" id="pName">-</p>
            </div>
            <div class="space-y-2"><label class="text-sm font-medium text-zinc-500">Email</label>
              <p class="font-medium text-zinc-900" id="pEmail">-</p>
            </div>
            <div class="space-y-2"><label class="text-sm font-medium text-zinc-500">Phone</label>
              <p class="font-medium text-zinc-900" id="pPhone">-</p>
            </div>
            <div class="space-y-2"><label class="text-sm font-medium text-zinc-500">Department</label>
              <p class="font-medium text-zinc-900" id="pDept">-</p>
            </div>
            <div class="space-y-2"><label class="text-sm font-medium text-zinc-500">Student ID</label>
              <p class="font-medium text-zinc-900" id="pStudentId">-</p>
            </div>
          </div>
          <div class="p-6 hidden" id="personalEdit">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Full Name</label><input id="editName"
                  class="input" placeholder="Your full name"></div>
              <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Phone</label><input id="editPhone"
                  class="input" placeholder="+1 (555) 123-4567"></div>
              <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Department</label><input id="editDept"
                  class="input" placeholder="e.g. Computer Science"></div>
              <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Student ID</label><input id="editStudentId"
                  class="input" placeholder="e.g. 20261234"></div>
            </div>
            <div class="flex justify-end gap-3 mt-4">
              <button onclick="toggleEdit('personal')" class="btn btn-secondary">Cancel</button>
              <button onclick="saveProfile()" class="btn btn-primary">Save Changes</button>
            </div>
          </div>
        </div>

        <!-- Academic Details -->
        <div class="card fade-in">
          <div class="p-6 border-b border-zinc-100 flex justify-between items-center">
            <h2 class="text-xl font-semibold flex items-center gap-2 text-zinc-900">
              <i data-lucide="graduation-cap" class="h-5 w-5 text-zinc-500"></i> Academic Details
            </h2>
            <button onclick="toggleEdit('academic')" class="btn btn-ghost text-sm gap-2">
              <i data-lucide="edit-2" class="h-4 w-4"></i> Edit
            </button>
          </div>
          <div class="p-6 space-y-6" id="academicView">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div class="space-y-2"><label class="text-sm font-medium text-zinc-500">Degree</label>
                <p class="font-medium text-zinc-900" id="pDegree">-</p>
              </div>
              <div class="space-y-2"><label class="text-sm font-medium text-zinc-500">Year</label>
                <p class="font-medium text-zinc-900" id="pYear">-</p>
              </div>
              <div class="space-y-2"><label class="text-sm font-medium text-zinc-500">GPA</label>
                <p class="font-medium text-zinc-900" id="pGpa">-</p>
              </div>
            </div>
            <div>
              <label class="text-sm font-medium text-zinc-500 mb-2 block">Skills</label>
              <div class="flex flex-wrap gap-2" id="pSkills"></div>
            </div>
          </div>
          <div class="p-6 hidden" id="academicEdit">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Degree</label>
                <select id="editDegree" class="input">
                  <option value="">Select...</option>
                  <option value="Bachelor">Bachelor</option>
                  <option value="Master">Master</option>
                  <option value="PhD">PhD</option>
                  <option value="Diploma">Diploma</option>
                </select>
              </div>
              <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Year</label>
                <input id="editYear" type="number" class="input" min="1" max="8" placeholder="e.g. 2"></div>
              <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">GPA</label>
                <input id="editGpa" type="number" step="0.01" class="input" min="0" placeholder="e.g. 3.5"></div>
              <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Max GPA</label>
                <input id="editMaxGpa" type="number" step="0.01" class="input" min="0" placeholder="e.g. 4.0"></div>
              <div class="col-span-2"><label class="block text-sm font-medium text-zinc-700 mb-1.5">Skills (comma separated)</label>
                <input id="editSkills" class="input" placeholder="Java, Python, Machine Learning, ..."></div>
            </div>
            <div class="flex justify-end gap-3 mt-4">
              <button onclick="toggleEdit('academic')" class="btn btn-secondary">Cancel</button>
              <button onclick="saveAcademic()" class="btn btn-primary">Save Changes</button>
            </div>
          </div>
        </div>

        <!-- Experience -->
        <div class="card fade-in">
          <div class="p-6 border-b border-zinc-100 flex justify-between items-center">
            <h2 class="text-xl font-semibold flex items-center gap-2 text-zinc-900">
              <i data-lucide="briefcase" class="h-5 w-5 text-zinc-500"></i> Experience
            </h2>
            <button onclick="addExperience()" class="btn btn-ghost text-sm gap-2">
              <i data-lucide="plus" class="h-4 w-4"></i> Add
            </button>
          </div>
          <div class="p-6" id="expSection">
            <p class="text-zinc-400 text-sm">No experience added</p>
          </div>
        </div>

        <!-- Documents -->
        <div class="card fade-in">
          <div class="p-6 border-b border-zinc-100 flex justify-between items-center">
            <h2 class="text-xl font-semibold flex items-center gap-2 text-zinc-900">
              <i data-lucide="book" class="h-5 w-5 text-zinc-500"></i> Documents
            </h2>
          </div>
          <div class="p-6 space-y-6">
            <div id="docList"></div>
            <div
              class="border-2 border-dashed border-zinc-200 rounded-xl p-6 flex flex-col items-center justify-center text-center cursor-pointer hover:bg-zinc-50 transition-colors"
              onclick="uploadDoc()">
              <i data-lucide="upload-cloud" class="h-8 w-8 text-zinc-400 mb-2"></i>
              <p class="font-medium text-zinc-900">Upload Document</p>
              <p class="text-sm text-zinc-500">PDF up to 5MB</p>
            </div>
            <input id="docFileInput" type="file" class="hidden" accept=".pdf,.doc,.docx,.txt,.rtf"
              onchange="handleFileSelected(event)">
          </div>
        </div>
      </div>
    </div>

    <jsp:include page="/common/layout-bottom.jsp" />
    <script>
      let profileData = null;
      let userData = null;

      async function loadProfile() {
        try {
          const data = await applicantApi.getProfile();
          userData = data.user || {};
          profileData = data.profile || data;
          render(profileData);
        } catch (e) {
          console.error('Failed to load profile', e);
        }
      }

      function render(p) {
        document.getElementById('pName').textContent = userData.name || '-';
        document.getElementById('pEmail').textContent = userData.email || '-';
        document.getElementById('pPhone').textContent = userData.phone || '-';
        document.getElementById('pDept').textContent = userData.department || '-';
        document.getElementById('pStudentId').textContent = p.studentId || '-';
        document.getElementById('pDegree').textContent = p.degree || '-';
        document.getElementById('pYear').textContent = p.year || '-';
        document.getElementById('pGpa').textContent = p.gpa ? p.gpa + ' / ' + (p.maxGpa || '4.0') : '-';

        document.getElementById('editName').value = userData.name || '';
        document.getElementById('editPhone').value = userData.phone || '';
        document.getElementById('editDept').value = userData.department || '';
        document.getElementById('editStudentId').value = p.studentId || '';
        document.getElementById('editDegree').value = p.degree || '';
        document.getElementById('editYear').value = p.year || '';
        document.getElementById('editGpa').value = p.gpa || '';
        document.getElementById('editMaxGpa').value = p.maxGpa || '4.0';

        const skills = p.skills || [];
        document.getElementById('editSkills').value = skills.join(', ');
        document.getElementById('pSkills').innerHTML = skills.length ?
          skills.map(s => '<span class="px-3 py-1 bg-zinc-100 text-zinc-700 rounded-full text-sm border border-zinc-200">' + s + '</span>').join('') :
          '<span class="text-zinc-400 text-sm">No skills added</span>';

        renderExperience(p.experience || []);

        const docs = p.documents || [];
        document.getElementById('docList').innerHTML = docs.length ? docs.map(d =>
          '<div class="border border-zinc-200 rounded-xl p-4 flex justify-between items-center bg-zinc-50 mb-3">' +
          '<div class="flex items-center gap-4">' +
          '<div class="h-10 w-10 bg-red-100 text-red-600 rounded-lg flex items-center justify-center"><i data-lucide="file-text" class="h-5 w-5"></i></div>' +
          '<div><p class="font-medium text-zinc-900">' + (d.name || 'Untitled') + '</p><p class="text-sm text-zinc-500">' + (d.size || '') + '</p></div>' +
          '</div>' +
          '<button onclick="deleteDoc(\'' + d.id + '\')" class="btn btn-ghost text-red-500 hover:text-red-600 text-sm">Delete</button>' +
          '</div>'
        ).join('') : '<p class="text-zinc-400 text-sm">No documents uploaded</p>';

        const completeness = p.profileCompletion || 0;
        document.getElementById('completionPct').textContent = completeness + '% Complete';
        document.getElementById('completionBar').style.width = completeness + '%';
        lucide.createIcons();
      }

      function toggleEdit(section) {
        const view = document.getElementById(section + 'View');
        const edit = document.getElementById(section + 'Edit');
        view.classList.toggle('hidden');
        edit.classList.toggle('hidden');
      }

      async function saveProfile() {
        try {
          const update = {
            name: document.getElementById('editName').value.trim(),
            phone: document.getElementById('editPhone').value.trim(),
            department: document.getElementById('editDept').value.trim(),
            studentId: document.getElementById('editStudentId').value.trim(),
          };
          await applicantApi.updateProfile(update);
          showToast('Profile updated successfully!');
          toggleEdit('personal');
          loadProfile();
        } catch (e) {
          showToast(e.message || 'Update failed', 'error');
        }
      }

      async function saveAcademic() {
        try {
          const gpa = parseFloat(document.getElementById('editGpa').value) || 0;
          const maxGpa = parseFloat(document.getElementById('editMaxGpa').value) || 4.0;
          const update = {
            degree: document.getElementById('editDegree').value,
            year: parseInt(document.getElementById('editYear').value) || 0,
            gpa: gpa,
            maxGpa: maxGpa,
            skills: document.getElementById('editSkills').value.split(',').map(s => s.trim()).filter(Boolean),
          };
          await applicantApi.updateProfile(update);
          showToast('Academic details updated!');
          toggleEdit('academic');
          loadProfile();
        } catch (e) {
          showToast(e.message || 'Update failed', 'error');
        }
      }

      async function uploadDoc() {
        document.getElementById('docFileInput').click();
      }

      function inferFileType(file) {
        const fileName = file.name || '';
        const dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < fileName.length - 1) {
          return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return 'file';
      }

      function formatFileSize(bytes) {
        if (!Number.isFinite(bytes) || bytes < 0) return '0 B';
        if (bytes < 1024) return bytes + ' B';
        const kb = bytes / 1024;
        if (kb < 1024) return kb.toFixed(1).replace(/\.0$/, '') + ' KB';
        const mb = kb / 1024;
        return mb.toFixed(1).replace(/\.0$/, '') + ' MB';
      }

      async function handleFileSelected(event) {
        const input = event.target;
        const file = input.files && input.files[0];
        if (!file) return;

        try {
          // Real file upload via FormData
          const formData = new FormData();
          formData.append('file', file);
          const resp = await fetch(BASE + '/applicant/profile/documents', {
            method: 'POST',
            credentials: 'same-origin',
            body: formData,
          });
          const data = await resp.json().catch(() => ({}));
          if (!resp.ok) throw { status: resp.status, message: data.error || 'HTTP ' + resp.status };
          showToast('Document uploaded!');
          loadProfile();
        } catch (e) {
          showToast(e.message || 'Upload failed', 'error');
        } finally {
          input.value = '';
        }
      }

      async function deleteDoc(id) {
        if (!confirm('Delete this document?')) return;
        try {
          await applicantApi.deleteDocument(id);
          showToast('Document deleted');
          loadProfile();
        } catch (e) {
          showToast(e.message || 'Delete failed', 'error');
        }
      }

      function renderExperience(exps) {
        const container = document.getElementById('expSection');
        if (!exps || exps.length === 0) {
          container.innerHTML = '<p class="text-zinc-400 text-sm">No experience added</p>';
          return;
        }
        container.innerHTML = exps.map((exp, i) =>
          '<div class="border border-zinc-200 rounded-xl p-4 bg-zinc-50 mb-3">' +
            '<div class="flex justify-between items-start">' +
              '<div class="flex-1">' +
                '<p class="font-semibold text-zinc-900">' + (exp.role || 'Untitled') + '</p>' +
                '<p class="text-sm text-zinc-600 mt-0.5">' + (exp.company || '') + (exp.term ? ' &middot; ' + exp.term : '') + '</p>' +
                (exp.course ? '<p class="text-sm text-emerald-600 mt-0.5">Course: ' + exp.course + '</p>' : '') +
                (exp.description ? '<p class="text-sm text-zinc-500 mt-2">' + exp.description + '</p>' : '') +
              '</div>' +
              '<div class="flex gap-2 ml-4 shrink-0">' +
                '<button onclick="editExperience(' + i + ')" class="btn btn-ghost text-sm"><i data-lucide="edit-2" class="h-4 w-4"></i></button>' +
                '<button onclick="removeExperience(' + i + ')" class="btn btn-ghost text-red-500 hover:text-red-600 text-sm"><i data-lucide="trash-2" class="h-4 w-4"></i></button>' +
              '</div>' +
            '</div>' +
          '</div>'
        ).join('');
        lucide.createIcons();
      }

      function addExperience() {
        showExpModal(-1, {role:'', company:'', course:'', term:'', description:''});
      }

      function editExperience(idx) {
        const exps = (profileData && profileData.experience) || [];
        if (idx < 0 || idx >= exps.length) return;
        showExpModal(idx, {...exps[idx]});
      }

      function showExpModal(idx, exp) {
        let modal = document.getElementById('expModal');
        if (!modal) {
          modal = document.createElement('div');
          modal.id = 'expModal';
          modal.className = 'fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50';
          document.body.appendChild(modal);
        }
        modal.classList.remove('hidden');
        modal.innerHTML =
          '<div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg mx-4 p-6">' +
            '<h3 class="text-lg font-semibold text-zinc-900 mb-4">' + (idx < 0 ? 'Add Experience' : 'Edit Experience') + '</h3>' +
            '<div class="space-y-4">' +
              '<div><label class="block text-sm font-medium text-zinc-700 mb-1">Title / Role</label>' +
              '<input id="expRole" class="input w-full" value="' + (exp.role||'').replace(/"/g,'&quot;') + '" placeholder="e.g. Research Assistant"></div>' +
              '<div><label class="block text-sm font-medium text-zinc-700 mb-1">Organization</label>' +
              '<input id="expCompany" class="input w-full" value="' + (exp.company||'').replace(/"/g,'&quot;') + '" placeholder="e.g. NTU School of Computing"></div>' +
              '<div><label class="block text-sm font-medium text-zinc-700 mb-1">Related Course (optional)</label>' +
              '<input id="expCourse" class="input w-full" value="' + (exp.course||'').replace(/"/g,'&quot;') + '" placeholder="e.g. CS3230 Data Structures"></div>' +
              '<div><label class="block text-sm font-medium text-zinc-700 mb-1">Duration / Term</label>' +
              '<input id="expTerm" class="input w-full" value="' + (exp.term||'').replace(/"/g,'&quot;') + '" placeholder="e.g. Jan 2025 - Jun 2025"></div>' +
              '<div><label class="block text-sm font-medium text-zinc-700 mb-1">Description</label>' +
              '<textarea id="expDesc" class="input w-full" rows="3" placeholder="Brief description of your work...">' + (exp.description||'') + '</textarea></div>' +
            '</div>' +
            '<div class="flex justify-end gap-3 mt-6">' +
              '<button onclick="closeExpModal()" class="btn btn-secondary">Cancel</button>' +
              '<button onclick="saveExperience(' + idx + ')" class="btn btn-primary">Save</button>' +
            '</div>' +
          '</div>';
      }

      function closeExpModal() {
        const modal = document.getElementById('expModal');
        if (modal) modal.classList.add('hidden');
      }

      async function saveExperience(idx) {
        const entry = {
          role: document.getElementById('expRole').value.trim(),
          company: document.getElementById('expCompany').value.trim(),
          course: document.getElementById('expCourse').value.trim(),
          term: document.getElementById('expTerm').value.trim(),
          description: document.getElementById('expDesc').value.trim(),
        };
        if (!entry.role) { showToast('Title / Role is required', 'error'); return; }

        const exps = (profileData && profileData.experience) ? [...profileData.experience] : [];
        if (idx < 0) {
          exps.push(entry);
        } else {
          exps[idx] = entry;
        }
        try {
          await applicantApi.updateProfile({ experience: exps });
          showToast('Experience saved!');
          closeExpModal();
          loadProfile();
        } catch (e) {
          showToast(e.message || 'Save failed', 'error');
        }
      }

      async function removeExperience(idx) {
        if (!confirm('Remove this experience?')) return;
        const exps = (profileData && profileData.experience) ? [...profileData.experience] : [];
        exps.splice(idx, 1);
        try {
          await applicantApi.updateProfile({ experience: exps });
          showToast('Experience removed');
          loadProfile();
        } catch (e) {
          showToast(e.message || 'Remove failed', 'error');
        }
      }

      loadProfile();
    </script>