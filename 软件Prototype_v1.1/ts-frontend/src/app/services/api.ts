const BASE_URL = '/api';

async function request<T>(url: string, options: RequestInit = {}): Promise<T> {
  const resp = await fetch(`${BASE_URL}${url}`, {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  });

  if (!resp.ok) {
    const error = await resp.json().catch(() => ({ error: resp.statusText }));
    throw new Error(error.error || `HTTP ${resp.status}`);
  }

  return resp.json();
}

// ==================== Auth API ====================

export const authApi = {
  login: (email: string, password: string, role: string) =>
    request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password, role }),
    }),

  signup: (name: string, email: string, password: string, role = 'applicant') =>
    request('/auth/signup', {
      method: 'POST',
      body: JSON.stringify({ name, email, password, role }),
    }),

  logout: () =>
    request('/auth/logout', { method: 'POST' }),

  me: () =>
    request('/auth/me'),
};

// ==================== Applicant API ====================

export const applicantApi = {
  getDashboard: () =>
    request('/applicant/dashboard'),

  getJobs: (params?: { keyword?: string; department?: string; type?: string }) => {
    const query = new URLSearchParams(params as Record<string, string>).toString();
    return request(`/applicant/jobs${query ? `?${query}` : ''}`);
  },

  getJobById: (id: string) =>
    request(`/applicant/jobs/${id}`),

  applyForJob: (jobId: string, coverLetter: string, documentIds: string[]) =>
    request(`/applicant/jobs/${jobId}/apply`, {
      method: 'POST',
      body: JSON.stringify({ coverLetter, documentIds }),
    }),

  getApplications: () =>
    request('/applicant/applications'),

  getApplicationById: (id: string) =>
    request(`/applicant/applications/${id}`),

  getProfile: () =>
    request('/applicant/profile'),

  updateProfile: (data: Record<string, unknown>) =>
    request('/applicant/profile', {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  uploadDocument: (fileName: string, fileType: string, fileSize: string) =>
    request('/applicant/profile/documents', {
      method: 'POST',
      body: JSON.stringify({ fileName, fileType, fileSize }),
    }),

  deleteDocument: (docId: string) =>
    request(`/applicant/profile/documents/${docId}`, { method: 'DELETE' }),
};

// ==================== Organiser API ====================

export const organiserApi = {
  getDashboard: () =>
    request('/organiser/dashboard'),

  getJobs: () =>
    request('/organiser/jobs'),

  createJob: (data: Record<string, unknown>) =>
    request('/organiser/jobs', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateJob: (id: string, data: Record<string, unknown>) =>
    request(`/organiser/jobs/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  deleteJob: (id: string) =>
    request(`/organiser/jobs/${id}`, { method: 'DELETE' }),

  getPipeline: (module?: string) => {
    const query = module && module !== 'All' ? `?module=${module}` : '';
    return request(`/organiser/pipeline${query}`);
  },

  updateCandidateStatus: (appId: string, status: string) =>
    request(`/organiser/pipeline/${appId}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    }),

  getApplicationDetails: (id: string) =>
    request(`/organiser/applications/${id}`),

  applicationAction: (id: string, action: string) =>
    request(`/organiser/applications/${id}/action`, {
      method: 'PUT',
      body: JSON.stringify({ action }),
    }),

  getInterviews: () =>
    request('/organiser/interviews'),

  createInterview: (data: Record<string, unknown>) =>
    request('/organiser/interviews', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateInterview: (id: string, data: Record<string, unknown>) =>
    request(`/organiser/interviews/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),
};

// ==================== Admin API ====================

export const adminApi = {
  getDashboard: () =>
    request('/admin/dashboard'),

  getUsers: (params?: { keyword?: string; role?: string; status?: string }) => {
    const query = new URLSearchParams(params as Record<string, string>).toString();
    return request(`/admin/users${query ? `?${query}` : ''}`);
  },

  createUser: (data: Record<string, unknown>) =>
    request('/admin/users', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateUser: (id: string, data: Record<string, unknown>) =>
    request(`/admin/users/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  deleteUser: (id: string) =>
    request(`/admin/users/${id}`, { method: 'DELETE' }),

  getModules: (keyword?: string) => {
    const query = keyword ? `?keyword=${keyword}` : '';
    return request(`/admin/modules${query}`);
  },

  createModule: (data: Record<string, unknown>) =>
    request('/admin/modules', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateModule: (id: string, data: Record<string, unknown>) =>
    request(`/admin/modules/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  deleteModule: (id: string) =>
    request(`/admin/modules/${id}`, { method: 'DELETE' }),

  getTerms: () =>
    request('/admin/properties/terms'),

  createTerm: (data: Record<string, unknown>) =>
    request('/admin/properties/terms', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  getDepartments: () =>
    request('/admin/properties/departments'),

  createDepartment: (data: Record<string, unknown>) =>
    request('/admin/properties/departments', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  getLogs: (params?: { keyword?: string; type?: string }) => {
    const query = new URLSearchParams(params as Record<string, string>).toString();
    return request(`/admin/logs${query ? `?${query}` : ''}`);
  },

  getWorkload: (params?: { keyword?: string; status?: string }) => {
    const query = new URLSearchParams(params as Record<string, string>).toString();
    return request(`/admin/workload${query ? `?${query}` : ''}`);
  },

  reassignWorkload: (taId: string, data: Record<string, unknown>) =>
    request(`/admin/workload/${taId}/reassign`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),
};
