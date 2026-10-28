<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script src="https://cdn.tailwindcss.com"></script>
<script src="https://unpkg.com/lucide@latest"></script>
<script src="https://unpkg.com/mammoth@1.8.0/mammoth.browser.min.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/custom.css?v=<%= System.currentTimeMillis() %>">
<script>
tailwind.config = {
  theme: {
    extend: {
      fontFamily: { sans: ['Inter', 'system-ui', 'sans-serif'] }
    }
  }
}
</script>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

<script>window.roleDisplayLabels = <%= com.tamo.util.JsonUtil.getGson().toJson(java.util.Map.of("applicant",com.tamo.util.RoleLabels.display("applicant"),"organiser",com.tamo.util.RoleLabels.display("organiser"),"admin",com.tamo.util.RoleLabels.display("admin"),"reviewer",com.tamo.util.RoleLabels.display("reviewer"),"decision_maker",com.tamo.util.RoleLabels.display("decision_maker"),"viewer",com.tamo.util.RoleLabels.display("viewer"))) %>; window.displayRole = role => window.roleDisplayLabels[role] || "Unknown role";</script>
