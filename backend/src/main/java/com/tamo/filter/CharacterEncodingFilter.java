package com.tamo.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CharacterEncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest req) {
            req.setCharacterEncoding("UTF-8");
        }
        if (response instanceof HttpServletResponse resp) {
            resp.setCharacterEncoding("UTF-8");
        }
        chain.doFilter(request, response);
    }
}
