<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/auth-check.jsp" %>
<jsp:include page="/common/layout-top.jsp"/>

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
        <div class="space-y-2"><label class="text-sm font-medium text-zinc-500">Full Name</label><p class="font-medium text-zinc-900" id="pName">-</p></div>
        <div class="space-y-2"><label class="text-sm font-medium text-zinc-500">Email</label><p class="font-medium text-zinc-900" id="pEmail">-</p></div>
        <div class="space-y-2"><label class="text-sm font-medium text-zinc-500">Phone</label><p class="font-medium text-zinc-900" id="pPhone">-</p></div>
        <div class="space-y-2"><label class="text-sm font-medium text-zinc-500">Student ID</label><p class="font-medium text-zinc-900" id="pStudentId">-</p></div>
      </div>
      <div class="p-6 hidden" id="personalEdit">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Phone</label><input id="editPhone" class="input" placeholder="+1 (555) 123-4567"></div>
          <div><label class="block text-sm font-medium text-zinc-700 mb-1.5">Skills (comma separated)</label><input id="editSkills" class="input" placeholder="Java, Python, C++"></div>
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
      </div>
      <div class="p-6 space-y-6">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="space-y-2"><label class="text-sm font-medium text-zinc-500">Degree</label><p class="font-medium text-zinc-900" id="pDegree">-</p></div>
          <div class="space-y-2"><label class="text-sm font-medium text-zinc-500">Year</label><p class="font-medium text-zinc-900" id="pYear">-</p></div>
          <div class="space-y-2"><label class="text-sm font-medium text-zinc-500">GPA</label><p class="font-medium text-zinc-900" id="pGpa">-</p></div>
        </div>
        <div>
          <label class="text-sm font-medium text-zinc-500 mb-2 block">Skills</label>
          <div class="flex flex-wrap gap-2" id="pSkills"></div>
        </div>
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
        <div class="border-2 border-dashed border-zinc-200 rounded-xl p-6 flex flex-col items-center justify-center text-center cursor-pointer hover:bg-zinc-50 transition-colors"
             onclick="uploadDoc()">
          <i data-lucide="upload-cloud" class="h-8 w-8 text-zinc-400 mb-2"></i>
          <p class="font-medium text-zinc-900">Upload Document</p>
          <p class="text-sm text-zinc-500">PDF up to 5MB</p>
        </div>
      </div>
    </div>
  </div>
</div>

<jsp:include page="/common/layout-bottom.jsp"/>
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
    console.error('加载资料失败', e);
  }
}

function render(p) {
  // User info comes from user object, profile info from profile object
  document.getElementById('pName').textContent = userData.name || '-';
  document.getElementById('pEmail').textContent = userData.email || '-';
  document.getElementById('pPhone').textContent = userData.phone || '-';
  document.getElementById('pStudentId').textContent = p.studentId || '-';
  document.getElementById('pDegree').textContent = p.degree || '-';
  document.getElementById('pYear').textContent = p.year || '-';
  document.getElementById('pGpa').textContent = p.gpa ? p.gpa + ' / ' + (p.maxGpa || '4.0') : '-';
  document.getElementById('editPhone').value = userData.phone || '';

  // Skills are not stored in profile model, show coursework as skills substitute
  const coursework = p.coursework || [];
  const skills = coursework.map(c => c.code).filter(Boolean);
  document.getElementById('editSkills').value = skills.join(', ');
  document.getElementById('pSkills').innerHTML = skills.length ?
    skills.map(s => '<span class="px-3 py-1 bg-zinc-100 text-zinc-700 rounded-full text-sm border border-zinc-200">' + s + '</span>').join('') :
    '<span class="text-zinc-400 text-sm">No skills added</span>';

  // Documents - API uses 'name' not 'fileName', 'size' not 'fileSize'
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

  // Use profileCompletion from API if available, otherwise calculate
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
      phone: document.getElementById('editPhone').value.trim(),
      skills: document.getElementById('editSkills').value.split(',').map(s => s.trim()).filter(Boolean),
    };
    await applicantApi.updateProfile(update);
    showToast('Profile updated successfully!');
    toggleEdit('personal');
    loadProfile();
  } catch (e) {
    showToast(e.message || 'Update failed', 'error');
  }
}

async function uploadDoc() {
  const name = prompt('Document name (e.g. CV.pdf):');
  if (!name) return;
  try {
    await applicantApi.uploadDocument(name, 'pdf', '2.4 MB');
    showToast('Document uploaded!');
    loadProfile();
  } catch (e) {
    showToast(e.message || 'Upload failed', 'error');
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

loadProfile();
</script>
