<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/auth-check.jsp" %>
<jsp:include page="/common/layout-top.jsp" />
<jsp:include page="/common/assignments-content.jsp" />
<jsp:include page="/common/layout-bottom.jsp" />
<link rel="stylesheet" href="<%= request.getContextPath() %>/static/vendor/flatpickr/flatpickr.min.css">
<script src="<%= request.getContextPath() %>/static/vendor/flatpickr/flatpickr.min.js"></script>
<script src="<%= request.getContextPath() %>/static/js/assignments.js"></script>
