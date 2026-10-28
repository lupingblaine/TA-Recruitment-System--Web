package com.tamo.servlet;

import com.tamo.service.JsonDataService;
import com.tamo.service.MessageService;
import com.tamo.service.TestDataFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import java.io.PrintWriter;
import java.io.StringWriter;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageUserInfoAccessTest {
    @Test void forbiddenUserInfoReturns403WithoutAcademicData() throws Exception {
        String response = request("u001", "u002", 403);
        assertFalse(response.contains("gpa"));
        assertFalse(response.contains("BSc EE"));
    }
    @Test void unrelatedTeacherReturns403() throws Exception {
        request("u006", "u002", 403);
    }
    @Test void missingTargetReturns404() throws Exception {
        request("u001", "missing", 404);
    }
    private String request(String requester, String target, int expectedStatus) throws Exception {
        MessageServlet servlet = new MessageServlet();
        var serviceField = MessageServlet.class.getDeclaredField("service");
        serviceField.setAccessible(true);
        serviceField.set(servlet, new MessageService(JsonDataService.detached(TestDataFactory.createTestDataStore())));
        var req = mock(HttpServletRequest.class);
        var resp = mock(HttpServletResponse.class);
        var session = mock(HttpSession.class);
        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(requester);
        when(req.getPathInfo()).thenReturn("/user-info/" + target);
        StringWriter output = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(output));
        servlet.doGet(req, resp);
        verify(resp).setStatus(expectedStatus);
        return output.toString();
    }
}
