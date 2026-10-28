package com.tamo.servlet;
import com.tamo.service.*;
import com.tamo.model.*;
import jakarta.servlet.http.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class OrganiserPermissionServletTest {
    private OrganiserServlet servlet(JsonDataService data)throws Exception{
        OrganiserServlet servlet=new OrganiserServlet();
        for(String name:List.of("service","dataService")){var field=OrganiserServlet.class.getDeclaredField(name);field.setAccessible(true);field.set(servlet,name.equals("service")?new OrganiserService(data):data);}
        return servlet;
    }
    private HttpServletRequest request(String path,String body)throws Exception{
        var req=mock(HttpServletRequest.class);var session=mock(HttpSession.class);when(req.getSession()).thenReturn(session);when(session.getAttribute("userId")).thenReturn("u005");when(req.getPathInfo()).thenReturn(path);when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));return req;
    }
    @ParameterizedTest @ValueSource(strings={"/pipeline","/dashboard","/applications/a001"})
    void revokedReviewIsHttp403(String path)throws Exception{
        var ds=TestDataFactory.createTestDataStore();ds.getUsers().stream().filter(u->"u005".equals(u.getId())).forEach(u->u.setPermissions(List.of()));
        var resp=mock(HttpServletResponse.class);when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        servlet(JsonDataService.detached(ds)).doGet(request(path,"{}"),resp);verify(resp).setStatus(403);
    }
    @Test void rejectionReasonReachesFormalReviewService()throws Exception{
        var ds=TestDataFactory.createTestDataStore();ds.getUsers().stream().filter(u->"u005".equals(u.getId())).forEach(u->u.setPermissions(List.of("review.read","review.approve")));
        var resp=mock(HttpServletResponse.class);var output=new StringWriter();when(resp.getWriter()).thenReturn(new PrintWriter(output));
        servlet(JsonDataService.detached(ds)).doPut(request("/applications/a001/action","{\"action\":\"reject\",\"note\":\"Applicant cannot cover this section.\"}"),resp);
        assertEquals("rejected",ds.getApplications().get(0).getStatus());assertEquals("not_selected",ds.getReviewRecords().get(ds.getReviewRecords().size()-1).getAction());assertFalse(com.google.gson.JsonParser.parseString(output.toString()).getAsJsonObject().has("error"), output.toString());
    }
}
