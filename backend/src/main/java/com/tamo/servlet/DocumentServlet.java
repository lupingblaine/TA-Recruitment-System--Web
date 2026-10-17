package com.tamo.servlet;
import com.tamo.service.*;
import com.tamo.util.JsonUtil;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
public class DocumentServlet extends HttpServlet{
 @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException{
  if(req.getSession(false)==null||req.getSession(false).getAttribute("userId")==null){JsonUtil.writeError(resp,401,"Not logged in.");return;}
  try{var doc=PrivateDocuments.authorized(JsonDataService.getInstance().getData(),(String)req.getSession().getAttribute("userId"),JsonUtil.getPathInfo(req),req.getParameter("applicationId"));Path file=PrivateDocuments.path(doc);if(!Files.isRegularFile(file)){JsonUtil.writeError(resp,404,"Document file not available.");return;}
   String ext=doc.getName().substring(doc.getName().lastIndexOf('.')+1).toLowerCase(Locale.ROOT);String type=switch(ext){case "pdf"->"application/pdf";case "png"->"image/png";case "jpg","jpeg"->"image/jpeg";default->"application/octet-stream";};
   resp.setContentType(type);resp.setHeader("X-Content-Type-Options","nosniff");resp.setHeader("Cache-Control","private, no-store");resp.setHeader("Content-Disposition",("1".equals(req.getParameter("inline"))&&!type.equals("application/octet-stream")?"inline":"attachment")+"; filename*=UTF-8''"+java.net.URLEncoder.encode(doc.getName(),java.nio.charset.StandardCharsets.UTF_8).replace("+","%20"));resp.setContentLengthLong(Files.size(file));Files.copy(file,resp.getOutputStream());
  }catch(QualificationService.Problem e){JsonUtil.writeError(resp,e.status,e.getMessage());}
 }
}
