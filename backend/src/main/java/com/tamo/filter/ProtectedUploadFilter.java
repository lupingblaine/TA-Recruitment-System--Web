package com.tamo.filter;
import jakarta.servlet.*;import jakarta.servlet.http.*;import java.io.IOException;
/** Legacy public upload URLs are deliberately closed; documents require the authorized API. */
public class ProtectedUploadFilter implements Filter{public void doFilter(ServletRequest req,ServletResponse resp,FilterChain chain)throws IOException{((HttpServletResponse)resp).sendError(404);}}
