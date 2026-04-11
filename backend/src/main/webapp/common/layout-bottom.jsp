<%@ page pageEncoding="UTF-8" %>
      </div>
    </div>
  </main>
</div>

<script src="${pageContext.request.contextPath}/static/js/api.js?v=20260411"></script>
<script>
  lucide.createIcons();

  async function doLogout() {
    try {
      await authApi.logout();
    } catch(e) {}
    window.location.href = '${pageContext.request.contextPath}/auth/login.jsp';
  }
</script>
