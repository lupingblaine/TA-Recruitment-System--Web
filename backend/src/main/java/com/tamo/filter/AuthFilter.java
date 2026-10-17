package com.tamo.filter;

import com.tamo.util.JsonUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Implementation of AuthFilter.
 * Provides necessary operations and logic for the system.
 */
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
        try {
            var ds=com.tamo.service.JsonDataService.getInstance().getData();
            if(ds!=null&&ds.getUsers()!=null){var user=ds.getUsers().stream().filter(u->java.util.Objects.equals(session.getAttribute("userId"),u.getId())).findFirst().orElse(null);
                if(user==null||!"active".equals(user.getStatus())){JsonUtil.writeError(resp,403,"Account access is no longer active.");return;}role=user.getRole();session.setAttribute("role",role);
            }
        }catch(IllegalStateException unavailable){/* DatabaseTransactionFilter rejects unavailable database requests first. */}
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

        if(path.contains("/api/messages")&&java.util.Set.of("reviewer","decision_maker","viewer").contains(role)){JsonUtil.writeError(resp,403,"Messaging is not included in this review role.");return;}
        chain.doFilter(request, response);
    }

    @Override
    /**
     * Executes the init operation.
     * @param filterConfig the filterConfig argument
     */
    /**
     * Executes the init operation.
     * @param filterConfig the filterConfig argument
     */
    public void init(FilterConfig filterConfig) {}

    @Override
    /**
     * Executes the destroy operation.
     */
    /**
     * Executes the destroy operation.
     */
    public void destroy() {}
}
