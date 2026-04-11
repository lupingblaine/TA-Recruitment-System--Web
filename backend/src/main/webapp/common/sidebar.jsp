<%@ page pageEncoding="UTF-8" %>
<%
  String currentPath = request.getRequestURI();
  String ctx = request.getContextPath();
  String role = (String) session.getAttribute("role");
  String userName = (String) session.getAttribute("userName");
  if (userName == null) userName = "User";

  String[][] navItems;
  String sectionRoot;
  if ("applicant".equals(role)) {
    sectionRoot = ctx + "/applicant";
    navItems = new String[][] {
      {"Dashboard", sectionRoot + "/dashboard.jsp", "layout-dashboard"},
      {"My Applications", sectionRoot + "/applications.jsp", "briefcase"},
      {"Find Jobs", sectionRoot + "/jobs.jsp", "users"},
      {"Profile", sectionRoot + "/profile.jsp", "user"},
    };
  } else if ("organiser".equals(role)) {
    sectionRoot = ctx + "/organiser";
    navItems = new String[][] {
      {"Overview", sectionRoot + "/dashboard.jsp", "layout-dashboard"},
      {"TA Positions", sectionRoot + "/jobs.jsp", "briefcase"},
      {"Pipeline", sectionRoot + "/pipeline.jsp", "users"},
      {"Interviews", sectionRoot + "/interviews.jsp", "calendar"},
    };
  } else {
    sectionRoot = ctx + "/admin";
    navItems = new String[][] {
      {"Analytics", sectionRoot + "/dashboard.jsp", "layout-dashboard"},
      {"Workload Intel", sectionRoot + "/workload.jsp", "trending-up"},
      {"Global Modules", sectionRoot + "/modules.jsp", "book-open"},
      {"Properties", sectionRoot + "/properties.jsp", "sliders"},
      {"Users", sectionRoot + "/users.jsp", "users"},
      {"System Logs", sectionRoot + "/logs.jsp", "settings"},
    };
  }
%>
<aside class="hidden md:flex w-64 flex-col border-r border-zinc-200/50 bg-white/50 backdrop-blur-xl flex-shrink-0">
  <div class="flex h-14 items-center px-6 pt-2">
    <div class="flex items-center gap-2 font-semibold">
      <div class="h-6 w-6 rounded-md bg-zinc-900 flex items-center justify-center">
        <span class="text-white text-xs font-bold">TA</span>
      </div>
      <span>RecruitPro</span>
    </div>
  </div>

  <div class="flex-1 overflow-auto py-6">
    <nav class="space-y-1 px-4">
      <% for (String[] item : navItems) {
        boolean isActive = currentPath.equals(item[1]) || currentPath.equals(item[1].replace(ctx, ""));
        String activeClass = isActive ? "text-zinc-900 bg-zinc-100" : "text-zinc-500 hover:text-zinc-900 hover:bg-zinc-100/50";
      %>
      <a href="<%= item[1] %>"
         class="group flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-all duration-200 <%= activeClass %>">
        <i data-lucide="<%= item[2] %>" class="h-4 w-4"></i>
        <span><%= item[0] %></span>
      </a>
      <% } %>
    </nav>
  </div>

  <div class="p-4 mt-auto border-t border-zinc-200/50">
    <div class="flex items-center gap-3 px-3 py-2 mb-2">
      <div class="h-8 w-8 rounded-full bg-zinc-200 border border-zinc-300 flex items-center justify-center text-xs font-bold text-zinc-600">
        <%= userName.substring(0,1).toUpperCase() %>
      </div>
      <div class="flex-1 min-w-0">
        <p class="text-sm font-medium text-zinc-900 truncate"><%= userName %></p>
        <p class="text-xs text-zinc-500 capitalize"><%= role %></p>
      </div>
    </div>
    <button onclick="doLogout()" class="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-zinc-500 hover:bg-zinc-100/50 hover:text-zinc-900 transition-colors">
      <i data-lucide="log-out" class="h-4 w-4"></i>
      <span>Sign out</span>
    </button>
  </div>
</aside>
