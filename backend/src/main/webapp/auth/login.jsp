<%@ page pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <title>Sign In - TA RecruitPro</title>
  <meta name="ctx" content="<%= request.getContextPath() %>">
  <jsp:include page="/common/head.jsp"/>
</head>
<body class="flex min-h-screen bg-zinc-50 items-center justify-center p-6 text-zinc-900">
  <%
    String ctx = request.getContextPath();
  %>
  <div class="absolute top-6 left-6 flex items-center gap-2 font-semibold text-lg">
    <a href="<%= ctx %>/index.jsp" class="flex items-center gap-2">
      <div class="h-8 w-8 rounded-lg bg-zinc-900 flex items-center justify-center shadow-sm">
        <span class="text-white text-sm font-bold">TA</span>
      </div>
      <span>RecruitPro</span>
    </a>
  </div>

  <div class="w-full max-w-md rounded-2xl border border-zinc-200/60 bg-white p-8 shadow-xl fade-in">
    <div class="mb-8">
      <h1 class="text-2xl font-bold tracking-tight mb-2">Welcome back</h1>
      <p class="text-zinc-500 text-sm">Sign in to your account to continue</p>
    </div>

    <form id="loginForm" class="space-y-6" onsubmit="return handleLogin(event)">
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-zinc-700 mb-1.5">Email address</label>
          <input id="email" type="email" placeholder="name@university.edu" required
                 class="input">
        </div>
        <div>
          <label class="block text-sm font-medium text-zinc-700 mb-1.5">Password</label>
          <input id="password" type="password" placeholder="••••••••" required
                 class="input">
        </div>
      </div>

      <div class="space-y-3">
        <label class="text-sm font-medium text-zinc-700">Sign in as</label>
        <div class="grid grid-cols-3 gap-3" id="roleSelector">
          <button type="button" data-role="applicant" onclick="selectRole('applicant')"
                  class="role-btn flex flex-col items-center justify-center gap-2 rounded-xl border p-4 transition-all border-zinc-900 bg-zinc-50 text-zinc-900 ring-1 ring-zinc-900">
            <i data-lucide="user" class="h-5 w-5"></i>
            <span class="text-sm font-medium">Applicant</span>
          </button>
          <button type="button" data-role="organiser" onclick="selectRole('organiser')"
                  class="role-btn flex flex-col items-center justify-center gap-2 rounded-xl border p-4 transition-all border-zinc-200 bg-white text-zinc-500 hover:bg-zinc-50">
            <i data-lucide="briefcase" class="h-5 w-5"></i>
            <span class="text-sm font-medium">Organiser</span>
          </button>
          <button type="button" data-role="admin" onclick="selectRole('admin')"
                  class="role-btn flex flex-col items-center justify-center gap-2 rounded-xl border p-4 transition-all border-zinc-200 bg-white text-zinc-500 hover:bg-zinc-50">
            <i data-lucide="layout-dashboard" class="h-5 w-5"></i>
            <span class="text-sm font-medium">Admin</span>
          </button>
        </div>
      </div>

      <div id="errorMsg" class="hidden text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-4 py-3"></div>

      <button type="submit" id="submitBtn"
              class="btn btn-primary w-full h-11 text-base">
        Sign In
      </button>

      <p class="text-center text-sm text-zinc-500">
        Don't have an account? <a href="<%= ctx %>/auth/signup.jsp" class="text-zinc-900 font-medium hover:underline">Sign up</a>
      </p>
    </form>
  </div>

  <script src="<%= ctx %>/static/js/api.js"></script>
  <script>
    lucide.createIcons();
    let selectedRole = 'applicant';

    function selectRole(r) {
      selectedRole = r;
      document.querySelectorAll('.role-btn').forEach(btn => {
        if (btn.dataset.role === r) {
          btn.className = 'role-btn flex flex-col items-center justify-center gap-2 rounded-xl border p-4 transition-all border-zinc-900 bg-zinc-50 text-zinc-900 ring-1 ring-zinc-900';
        } else {
          btn.className = 'role-btn flex flex-col items-center justify-center gap-2 rounded-xl border p-4 transition-all border-zinc-200 bg-white text-zinc-500 hover:bg-zinc-50';
        }
      });
    }

    async function handleLogin(e) {
      e.preventDefault();
      const errDiv = document.getElementById('errorMsg');
      errDiv.classList.add('hidden');
      const email = document.getElementById('email').value;
      const pwd = document.getElementById('password').value;
      const ctx = document.querySelector('meta[name="ctx"]').content;

      try {
        const data = await authApi.login(email, pwd, selectedRole);
        const role = data.user?.role || selectedRole;
        window.location.href = ctx + '/' + role + '/dashboard.jsp';
      } catch (err) {
        errDiv.textContent = err.message || '登录失败';
        errDiv.classList.remove('hidden');
      }
      return false;
    }
  </script>
</body>
</html>
