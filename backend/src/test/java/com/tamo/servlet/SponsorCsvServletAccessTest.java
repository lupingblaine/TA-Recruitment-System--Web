package com.tamo.servlet;
import com.tamo.service.*;
import com.tamo.model.User;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;
import java.io.*;
import static org.mockito.Mockito.*;
class SponsorCsvServletAccessTest {
 @Test void everyCsvEndpointDeniesNonAdminBeforeParsing()throws Exception{for(String role:new String[]{"organiser","reviewer","decision_maker","viewer","applicant"})for(String path:new String[]{"options","export","preview","import"}){var ds=TestDataFactory.createTestDataStore();User user=new User();user.setId("actor");user.setRole(role);user.setStatus("active");ds.getUsers().add(user);SponsorCsvServlet servlet=new SponsorCsvServlet();var field=SponsorCsvServlet.class.getDeclaredField("data");field.setAccessible(true);field.set(servlet,JsonDataService.detached(ds));var request=mock(HttpServletRequest.class);var response=mock(HttpServletResponse.class);var session=mock(HttpSession.class);when(request.getSession(false)).thenReturn(session);when(session.getAttribute("userId")).thenReturn("actor");lenient().when(request.getPathInfo()).thenReturn("/"+path);when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));if(path.equals("options")||path.equals("export"))servlet.doGet(request,response);else servlet.doPost(request,response);verify(response).setStatus(403);}}
}
