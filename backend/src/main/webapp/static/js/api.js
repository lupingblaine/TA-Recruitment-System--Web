const CTX = document.querySelector('meta[name="ctx"]')?.content || '';
const BASE = CTX + '/api';

async function request(url, options = {}) {
  const resp = await fetch(BASE + url, {
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  });
  const data = await resp.json().catch(() => ({}));
  if (!resp.ok) throw { status: resp.status, message: data.error || 'HTTP ' + resp.status };
  return data;
}

const authApi = {
  login: (email, password, role) =>
    request('/auth/login', { method: 'POST', body: JSON.stringify({ email, password, role }) }),
  signup: (name, email, password, role) =>
    request('/auth/signup', { method: 'POST', body: JSON.stringify({ name, email, password, role: role || 'applicant' }) }),
  logout: () => request('/auth/logout', { method: 'POST' }),
  me: () => request('/auth/me'),
};

const applicantApi = {
  getDashboard: () => request('/applicant/dashboard'),
  getJobs: (params) => {
    const q = params ? '?' + new URLSearchParams(params).toString() : '';
    return request('/applicant/jobs' + q);
  },
  getJobById: (id) => request('/applicant/jobs/' + id),
  applyForJob: (jobId, coverLetter, documentIds) =>
    request('/applicant/jobs/' + jobId + '/apply', { method: 'POST', body: JSON.stringify({ coverLetter, documentIds }) }),
  getApplications: () => request('/applicant/applications'),
  getApplicationById: (id) => request('/applicant/applications/' + id),
  getProfile: () => request('/applicant/profile'),
  updateProfile: (data) => request('/applicant/profile', { method: 'PUT', body: JSON.stringify(data) }),
  uploadDocument: (fileName, fileType, fileSize) =>
    request('/applicant/profile/documents', { method: 'POST', body: JSON.stringify({ fileName, fileType, fileSize }) }),
  deleteDocument: (docId) => request('/applicant/profile/documents/' + docId, { method: 'DELETE' }),
};

const organiserApi = {
  getDashboard: () => request('/organiser/dashboard'),
  getJobs: () => request('/organiser/jobs'),
  createJob: (data) => request('/organiser/jobs', { method: 'POST', body: JSON.stringify(data) }),
  updateJob: (id, data) => request('/organiser/jobs/' + id, { method: 'PUT', body: JSON.stringify(data) }),
  deleteJob: (id) => request('/organiser/jobs/' + id, { method: 'DELETE' }),
  getPipeline: (module) => {
    const q = module && module !== 'All' ? '?module=' + module : '';
    return request('/organiser/pipeline' + q);
  },
  updateCandidateStatus: (appId, status) =>
    request('/organiser/pipeline/' + appId + '/status', { method: 'PUT', body: JSON.stringify({ status }) }),
  getApplicationDetails: (id) => request('/organiser/applications/' + id),
  applicationAction: (id, action) =>
    request('/organiser/applications/' + id + '/action', { method: 'PUT', body: JSON.stringify({ action }) }),
  getInterviews: () => request('/organiser/interviews'),
  createInterview: (data) => request('/organiser/interviews', { method: 'POST', body: JSON.stringify(data) }),
  updateInterview: (id, data) => request('/organiser/interviews/' + id, { method: 'PUT', body: JSON.stringify(data) }),
};

const adminApi = {
  getDashboard: () => request('/admin/dashboard'),
  getUsers: (params) => {
    const q = params ? '?' + new URLSearchParams(params).toString() : '';
    return request('/admin/users' + q);
  },
  createUser: (data) => request('/admin/users', { method: 'POST', body: JSON.stringify(data) }),
  updateUser: (id, data) => request('/admin/users/' + id, { method: 'PUT', body: JSON.stringify(data) }),
  deleteUser: (id) => request('/admin/users/' + id, { method: 'DELETE' }),
  getModules: (keyword) => request('/admin/modules' + (keyword ? '?keyword=' + keyword : '')),
  createModule: (data) => request('/admin/modules', { method: 'POST', body: JSON.stringify(data) }),
  updateModule: (id, data) => request('/admin/modules/' + id, { method: 'PUT', body: JSON.stringify(data) }),
  deleteModule: (id) => request('/admin/modules/' + id, { method: 'DELETE' }),
  getTerms: () => request('/admin/properties/terms'),
  createTerm: (data) => request('/admin/properties/terms', { method: 'POST', body: JSON.stringify(data) }),
  getDepartments: () => request('/admin/properties/departments'),
  createDepartment: (data) => request('/admin/properties/departments', { method: 'POST', body: JSON.stringify(data) }),
  getLogs: (params) => {
    const q = params ? '?' + new URLSearchParams(params).toString() : '';
    return request('/admin/logs' + q);
  },
  getWorkload: (params) => {
    const q = params ? '?' + new URLSearchParams(params).toString() : '';
    return request('/admin/workload' + q);
  },
  reassignWorkload: (taId, data) =>
    request('/admin/workload/' + taId + '/reassign', { method: 'PUT', body: JSON.stringify(data) }),
};

function showToast(msg, type) {
  const t = document.createElement('div');
  t.className = 'fixed top-4 right-4 z-50 px-4 py-3 rounded-lg shadow-lg text-sm font-medium fade-in ' +
    (type === 'error' ? 'bg-red-50 text-red-800 border border-red-200' : 'bg-emerald-50 text-emerald-800 border border-emerald-200');
  t.textContent = msg;
  document.body.appendChild(t);
  setTimeout(() => t.remove(), 3000);
}
