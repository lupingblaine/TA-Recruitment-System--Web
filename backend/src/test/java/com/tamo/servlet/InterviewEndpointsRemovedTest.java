package com.tamo.servlet;

import com.tamo.service.*;
import jakarta.servlet.http.*;
import java.io.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.mockito.Mockito.*;

class InterviewEndpointsRemovedTest {
    @ParameterizedTest
    @CsvSource({"GET,/interviews", "POST,/interviews", "PUT,/interviews/i001", "DELETE,/interviews/i001"})
    void oldEndpointsReturn404(String method,String path)throws Exception {
        OrganiserServlet servlet=new OrganiserServlet();
        var data=JsonDataService.detached(TestDataFactory.createTestDataStore());
        for(String name:new String[]{"service","dataService"}) {
            var field=OrganiserServlet.class.getDeclaredField(name);field.setAccessible(true);
            field.set(servlet,name.equals("service")?new OrganiserService(data):data);
        }
        var req=mock(HttpServletRequest.class);var resp=mock(HttpServletResponse.class);var session=mock(HttpSession.class);
        when(req.getSession()).thenReturn(session);when(session.getAttribute("userId")).thenReturn("u005");when(req.getPathInfo()).thenReturn(path);
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{}")));
        when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        switch(method){case "GET"->servlet.doGet(req,resp);case "POST"->servlet.doPost(req,resp);case "PUT"->servlet.doPut(req,resp);default->servlet.doDelete(req,resp);}
        verify(resp).setStatus(404);
    }
}
