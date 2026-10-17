package com.tamo.service;
import com.google.gson.*;
import com.tamo.model.*;
import com.tamo.util.JsonUtil;
import java.util.*;

/** All staffing reports use the same period-scoped assignment ledger. */
public class AssignmentReportingService {
    private final DataStore ds;
    public AssignmentReportingService(DataStore ds){this.ds=ds;}
    public String period(String requested){
        if(requested!=null&&!requested.isBlank())return requested;
        return ds.getApplicationPeriods().stream().filter(p->!p.getLegacy()).reduce((a,b)->b).or(()->ds.getApplicationPeriods().stream().findFirst()).map(ApplicationPeriod::getId).orElse("");
    }
    public JsonObject workload(String user,String requested,boolean staff){
        String period=period(requested);JsonArray accepted=new JsonArray();double hours=0,reserved=0;
        for(SectionAssignment a:ds.getSectionAssignments()){
            if(!Objects.equals(user,a.getApplicantId())||!Objects.equals(period,a.getPeriodId())||!"active".equals(a.getStatus()))continue;
            if(staff||"published".equals(a.getPublicationStatus()))reserved+=a.getSectionSnapshot().getHoursPerWeek();
            if(!"published".equals(a.getPublicationStatus())||!"accepted".equals(a.getResponseStatus()))continue;
            CourseSection s=a.getSectionSnapshot();JsonObject row=new JsonObject();
            row.addProperty("assignmentId",a.getId());row.addProperty("applicationId",a.getApplicationId());row.addProperty("moduleCode",ds.getModules().stream().filter(m->Objects.equals(s.getModuleId(),m.getId())).map(com.tamo.model.Module::getCode).findFirst().orElse(""));
            row.addProperty("jobTitle",s.getName());row.addProperty("title",s.getName());row.addProperty("sectionId",s.getId());row.addProperty("hoursPerWeek",s.getHoursPerWeek());row.addProperty("location",s.getLocation());row.add("meetingTimes",JsonUtil.getGson().toJsonTree(s.getMeetingTimes()));accepted.add(row);hours+=s.getHoursPerWeek();
        }
        int max=ds.getProfiles().stream().filter(p->Objects.equals(user,p.getUserId())).mapToInt(Profile::getMaxWeeklyHours).findFirst().orElse(20);
        JsonObject result=new JsonObject();result.addProperty("periodId",period);result.addProperty("current",hours);result.addProperty("totalHours",hours);result.addProperty("reservedHours",reserved);result.addProperty("max",max);result.addProperty("maxHours",max);result.addProperty("remaining",Math.max(0,max-hours));
        result.addProperty("status",hours>max?"Overloaded":hours==max&&max>0?"At Capacity":max>0&&hours>=max*.8?"Near Capacity":"Healthy Load");result.add("assignments",accepted);return result;
    }
    public List<JsonObject> admin(String keyword,String status,String period){
        List<JsonObject> rows=new ArrayList<>();
        for(User u:ds.getUsers())if("applicant".equals(u.getRole())&&(keyword==null||u.getName().toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT)))){
            JsonObject row=workload(u.getId(),period,true);if(status!=null&&!status.isBlank()&&!row.get("status").getAsString().equalsIgnoreCase(status.replace('-',' ')))continue;
            row.addProperty("taUserId",u.getId());row.addProperty("taName",u.getName());row.addProperty("department",u.getDepartment());rows.add(row);
        }return rows;
    }
}
