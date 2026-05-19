<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/auth-check.jsp" %>
<jsp:include page="/common/layout-top.jsp"/>
<meta name="uid" content="<%= session.getAttribute("userId") %>">
<jsp:include page="/common/messages-content.jsp"/>
<jsp:include page="/common/layout-bottom.jsp"/>
