package com.tamo.servlet;
import com.google.gson.*;
import com.tamo.model.*;
import com.tamo.service.*;
import com.tamo.util.JsonUtil;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
public class SponsorCsvServlet extends HttpServlet {
 private JsonDataService data;public void init(){data=JsonDataService.getInstance();}
 private User user(HttpServletRequest r){HttpSession s=r.getSession(false);if(s==null||s.getAttribute("userId")==null)throw new QualificationService.Problem(401,"Not logged in.");User user=ReviewAccess.user(data.getData(),(String)s.getAttribute("userId"));if(!"admin".equals(user.getRole()))throw new QualificationService.Problem(403,"CSV import and export are available to active admin accounts only.");return user;}
 protected void doGet(HttpServletRequest r,HttpServletResponse response)throws IOException{try{User u=user(r);String path=JsonUtil.getPathInfo(r);if(path.equals("options")){JsonObject result=new JsonObject();result.addProperty("role",u.getRole());result.add("periods",JsonUtil.getGson().toJsonTree(data.getData().getApplicationPeriods().stream().filter(p->!p.getLegacy()).toList()));JsonArray owners=new JsonArray();if("admin".equals(u.getRole()))for(User owner:data.getData().getUsers())if("organiser".equals(owner.getRole())&&"active".equals(owner.getStatus())){JsonObject v=new JsonObject();v.addProperty("id",owner.getId());v.addProperty("name",owner.getName());owners.add(v);}result.add("organisers",owners);JsonUtil.writeJson(response,result);return;}if(!path.equals("export")){JsonUtil.writeError(response,404,"Endpoint not found.");return;}
  String kind=r.getParameter("dataset");String csv=new SponsorCsvService(data).export(u.getId(),kind,r.getParameter("periodId"),"true".equals(r.getParameter("template")));response.setContentType("text/csv; charset=UTF-8");response.setHeader("Cache-Control","private, no-store");response.setHeader("X-Content-Type-Options","nosniff");response.setHeader("Content-Disposition","attachment; filename=\""+kind+".csv\"");response.getWriter().write(csv);
 }catch(QualificationService.Problem e){JsonUtil.writeError(response,e.status,e.getMessage());}}
 protected void doPost(HttpServletRequest r,HttpServletResponse response)throws IOException{try{User u=user(r);String path=JsonUtil.getPathInfo(r);if(!Set.of("preview","import").contains(path)){JsonUtil.writeError(response,404,"Endpoint not found.");return;}if(r.getContentLengthLong()>2_100_000){JsonUtil.writeError(response,413,"CSV request exceeds 2 MB.");return;}JsonUtil.writeJson(response,new SponsorCsvService(data).run(u.getId(),JsonUtil.readRequestBodyAsJson(r),path.equals("import")));}catch(QualificationService.Problem e){JsonUtil.writeError(response,e.status,e.getMessage());}catch(JsonParseException|IllegalStateException e){JsonUtil.writeError(response,400,"Invalid CSV import request.");}}
}
