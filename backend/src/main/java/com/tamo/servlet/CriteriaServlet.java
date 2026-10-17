package com.tamo.servlet;
import com.google.gson.*;
import com.tamo.service.*;
import com.tamo.model.*;
import com.tamo.util.JsonUtil;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
public class CriteriaServlet extends HttpServlet{
 private JsonDataService data;@Override public void init(){data=JsonDataService.getInstance();}
 private boolean allowed(HttpServletRequest req,Job j){String role=(String)req.getSession().getAttribute("role"),user=(String)req.getSession().getAttribute("userId");return "admin".equals(role)||("organiser".equals(role)&&Objects.equals(user,j.getOrganiserId())&&data.getData().getModules().stream().anyMatch(m->Objects.equals(j.getModuleId(),m.getId())&&Objects.equals(user,m.getOrganiserId())));}
 @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException{JsonArray rows=new JsonArray();for(Job j:data.getData().getJobs())if(allowed(req,j))rows.add(JsonUtil.getGson().toJsonTree(j));JsonUtil.writeJson(resp,rows);}
 @Override protected void doPut(HttpServletRequest req,HttpServletResponse resp)throws IOException{
  String id=JsonUtil.getPathInfo(req);Job j=data.getData().getJobs().stream().filter(x->id.equals(x.getId())).findFirst().orElse(null);if(j==null){JsonUtil.writeError(resp,404,"Position not found.");return;}if(!allowed(req,j)){JsonUtil.writeError(resp,403,"Only the current course organiser or administrator may update criteria.");return;}
  try{JsonObject b=JsonUtil.readRequestBodyAsJson(req);String period=QualificationService.text(b,"periodId",100,true);if(data.getData().getApplicationPeriods().stream().noneMatch(p->period.equals(p.getId())))throw new QualificationService.Problem(400,"Choose an existing application cycle.");JsonObject criteria=CriteriaMatchingService.validate(b);JsonObject all=j.getPeriodCriteria()==null?new JsonObject():j.getPeriodCriteria().deepCopy();all.add(period,criteria);j.setPeriodCriteria(all);data.save();data.addLog("Matching Criteria Updated","info",(String)req.getSession().getAttribute("userName"),(String)req.getSession().getAttribute("userId"),"Updated criteria for position "+id,req.getRemoteAddr());JsonUtil.writeJson(resp,criteria);}catch(QualificationService.Problem e){JsonUtil.writeError(resp,e.status,e.getMessage());}catch(JsonParseException e){JsonUtil.writeError(resp,400,"Invalid JSON.");}
 }
}
