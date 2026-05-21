package com.tamo.filter;

import com.tamo.util.JsonUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            JsonUtil.writeError(resp, 401, "Not logged in");
            return;
        }

        String role = (String) session.getAttribute("role");
        String path = req.getRequestURI();

        if (path.contains("/api/applicant") && !"applicant".equals(role)) {
            JsonUtil.writeError(resp, 403, "Access denied");
            return;
        }
        if (path.contains("/api/organiser") && !"organiser".equals(role)) {
            JsonUtil.writeError(resp, 403, "Access denied");
            return;
        }
        if (path.contains("/api/admin") && !"admin".equals(role)) {
            JsonUtil.writeError(resp, 403, "Access denied");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}
}
