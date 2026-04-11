<%@ page pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <title>TA RecruitPro</title>
  <meta name="ctx" content="<%= request.getContextPath() %>">
  <jsp:include page="/common/head.jsp"/>
</head>
<body class="min-h-screen bg-white text-zinc-950 selection:bg-zinc-200 selection:text-zinc-900">
  <%
    String ctx = request.getContextPath();
  %>
  <nav class="fixed inset-x-0 top-0 z-50 flex h-16 items-center justify-between px-6 border-b border-zinc-200/50 bg-white/70 backdrop-blur-md">
    <div class="flex items-center gap-2 font-semibold text-lg">
      <div class="h-8 w-8 rounded-lg bg-zinc-900 flex items-center justify-center shadow-sm">
        <span class="text-white text-sm font-bold">TA</span>
      </div>
      <span>RecruitPro</span>
    </div>
    <div class="flex items-center gap-4">
      <a href="<%= ctx %>/auth/login.jsp" class="text-sm font-medium text-zinc-600 hover:text-zinc-900 transition-colors">Log in</a>
      <a href="<%= ctx %>/auth/signup.jsp" class="rounded-full bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-800 transition-colors shadow-sm">Get Started</a>
    </div>
  </nav>

  <main class="pt-32 pb-16 px-6 sm:px-12 lg:px-24 max-w-7xl mx-auto flex flex-col items-center text-center">
    <div class="inline-flex items-center gap-2 rounded-full border border-zinc-200 bg-zinc-50 px-3 py-1 text-sm font-medium text-zinc-600 mb-8 shadow-sm fade-in">
      <i data-lucide="zap" class="h-4 w-4 text-amber-500"></i>
      <span>The new standard for TA recruitment</span>
    </div>

    <h1 class="text-5xl md:text-7xl font-bold tracking-tight text-zinc-950 max-w-4xl fade-in">
      Streamline your teaching assistant hiring
    </h1>

    <p class="mt-6 text-xl text-zinc-500 max-w-2xl fade-in">
      A transparent, efficient, and beautiful platform for universities to manage TA applications, interviews, and offers in one place.
    </p>

    <div class="mt-10 flex flex-col sm:flex-row items-center gap-4 w-full justify-center fade-in">
      <a href="<%= ctx %>/auth/signup.jsp" class="flex items-center justify-center gap-2 rounded-full bg-zinc-900 px-8 py-4 text-base font-semibold text-white hover:bg-zinc-800 transition-all hover:scale-105 active:scale-95 shadow-lg w-full sm:w-auto">
        Start hiring now
        <i data-lucide="chevron-right" class="h-5 w-5"></i>
      </a>
      <a href="<%= ctx %>/auth/login.jsp" class="flex items-center justify-center gap-2 rounded-full border border-zinc-200 bg-white px-8 py-4 text-base font-semibold text-zinc-900 hover:bg-zinc-50 transition-all w-full sm:w-auto shadow-sm">
        View as Applicant
      </a>
    </div>

    <div class="mt-20 w-full max-w-5xl rounded-2xl border border-zinc-200/60 bg-white/50 p-2 shadow-2xl backdrop-blur-xl fade-in">
      <div class="rounded-xl overflow-hidden border border-zinc-100 bg-zinc-50 aspect-video flex items-center justify-center text-zinc-400">
        <img src="https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=2000&q=80"
             alt="Dashboard Preview"
             class="w-full h-full object-cover opacity-90 mix-blend-multiply filter grayscale-[30%] contrast-125">
      </div>
    </div>

    <div class="mt-32 grid grid-cols-1 md:grid-cols-3 gap-8 text-left w-full max-w-5xl">
      <div class="p-6 rounded-2xl border border-zinc-200/50 bg-zinc-50/50 hover:bg-zinc-50 transition-colors shadow-sm fade-in">
        <div class="h-12 w-12 rounded-xl bg-white border border-zinc-200 flex items-center justify-center mb-6 shadow-sm">
          <i data-lucide="users" class="h-6 w-6 text-zinc-900"></i>
        </div>
        <h3 class="text-lg font-semibold text-zinc-900 mb-2">Unified Applicant Profiles</h3>
        <p class="text-zinc-500 leading-relaxed">Students create one profile and apply to multiple modules seamlessly.</p>
      </div>
      <div class="p-6 rounded-2xl border border-zinc-200/50 bg-zinc-50/50 hover:bg-zinc-50 transition-colors shadow-sm fade-in">
        <div class="h-12 w-12 rounded-xl bg-white border border-zinc-200 flex items-center justify-center mb-6 shadow-sm">
          <i data-lucide="briefcase" class="h-6 w-6 text-zinc-900"></i>
        </div>
        <h3 class="text-lg font-semibold text-zinc-900 mb-2">Transparent Tracking</h3>
        <p class="text-zinc-500 leading-relaxed">Real-time application status for both applicants and organisers.</p>
      </div>
      <div class="p-6 rounded-2xl border border-zinc-200/50 bg-zinc-50/50 hover:bg-zinc-50 transition-colors shadow-sm fade-in">
        <div class="h-12 w-12 rounded-xl bg-white border border-zinc-200 flex items-center justify-center mb-6 shadow-sm">
          <i data-lucide="check-circle-2" class="h-6 w-6 text-zinc-900"></i>
        </div>
        <h3 class="text-lg font-semibold text-zinc-900 mb-2">Automated Interviews</h3>
        <p class="text-zinc-500 leading-relaxed">Schedule and manage interview slots without the back-and-forth emails.</p>
      </div>
    </div>
  </main>

  <script>lucide.createIcons();</script>
</body>
</html>
