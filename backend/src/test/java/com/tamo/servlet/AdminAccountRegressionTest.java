package com.tamo.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import java.io.PrintWriter;
import java.io.StringWriter;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminAccountRegressionTest {
    @Test void rawAccountWritesReturnForbiddenBeforeReadingBodyOrCallingServices() throws Exception {
        for (String method : new String[]{"POST", "PUT", "DELETE"}) {
            HttpServletRequest request=mock(HttpServletRequest.class);
            HttpServletResponse response=mock(HttpServletResponse.class);
            when(request.getPathInfo()).thenReturn(method.equals("POST") ? "/data-editor/users" : "/data-editor/users/u010");
            StringWriter output=new StringWriter(); when(response.getWriter()).thenReturn(new PrintWriter(output));
            AdminServlet servlet=new AdminServlet();
            switch(method) { case "POST" -> servlet.doPost(request,response); case "PUT" -> servlet.doPut(request,response); case "DELETE" -> servlet.doDelete(request,response); }
            verify(response).setStatus(403);
            verify(request,never()).getReader();
            verify(request,never()).getSession(anyBoolean());
            assertTrue(output.toString().contains("Users"));
        }
    }
}
