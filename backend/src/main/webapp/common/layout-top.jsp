<%@ page pageEncoding="UTF-8" %>
<%
  String userRole = (String) session.getAttribute("role");
  String uri = request.getRequestURI();
  String[] parts = uri.split("/");
  String currentSection = parts.length > 2 ? parts[2] : "";
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <title>TA RecruitPro</title>
  <meta name="ctx" content="<%= request.getContextPath() %>">
  <jsp:include page="/common/head.jsp"/>
</head>
<body class="bg-[#FAFAFA] text-zinc-900 font-sans selection:bg-zinc-200">
<div class="flex h-screen w-full">
  <jsp:include page="/common/sidebar.jsp"/>

  <main class="flex-1 flex flex-col min-w-0 overflow-hidden">
    <header class="h-14 flex items-center justify-between px-6 border-b border-zinc-200/50 bg-white/30 backdrop-blur-md sticky top-0 z-10">
      <div class="flex items-center gap-2 text-sm text-zinc-500">
        <span>Platform</span>
        <i data-lucide="chevron-right" class="h-4 w-4"></i>
        <span class="text-zinc-900 font-medium capitalize"><%= currentSection %></span>
      </div>
      <div class="flex items-center gap-4">
        <button class="relative p-2 text-zinc-400 hover:text-zinc-900 transition-colors rounded-full hover:bg-zinc-100">
          <i data-lucide="bell" class="h-5 w-5"></i>
          <span class="absolute top-1.5 right-1.5 h-2 w-2 rounded-full bg-blue-500"></span>
        </button>
        <div class="h-8 w-8 rounded-full bg-zinc-200 border border-zinc-300 overflow-hidden flex items-center justify-center text-xs font-bold text-zinc-600">
          <%
            String un = (String) session.getAttribute("userName");
            out.print(un != null ? un.substring(0,1).toUpperCase() : "U");
          %>
        </div>
      </div>
    </header>

    <div class="flex-1 overflow-auto p-6 md:p-8">
      <div class="max-w-6xl mx-auto w-full">
