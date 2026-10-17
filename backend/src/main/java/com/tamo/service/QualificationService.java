package com.tamo.service;
import com.google.gson.*;
import com.tamo.model.*;
import com.tamo.util.JsonUtil;
import java.util.*;

public class QualificationService {
    private final JsonDataService data;
    public QualificationService(JsonDataService data){this.data=data;}
    public static class Problem extends RuntimeException{public final int status;public Problem(int status,String message){super(message);this.status=status;}}
    private Profile profile(String id){return data.getData().getProfiles().stream().filter(p->id.equals(p.getUserId())).findFirst().orElseThrow(()->new Problem(404,"Complete your profile first."));}
    public JsonObject get(String id){JsonObject q=profile(id).getQualifications();return q==null?new JsonObject():q.deepCopy();}
    public JsonObject save(String id,JsonObject body){JsonObject q=validate(body);SkillCatalogService.validateSelection(data.getData(),q);profile(id).setQualifications(q);data.save();return q.deepCopy();}
    public static String text(JsonObject b,String key,int max,boolean required){
        if(b==null)throw new Problem(400,"Details are required.");JsonElement v=b.get(key);
        if(v==null&&!required)return "";
        if(v==null||!v.isJsonPrimitive()||!v.getAsJsonPrimitive().isString())throw new Problem(400,key+" must be text.");String value=v.getAsString().trim();
        if((required&&value.isEmpty())||value.length()>max)throw new Problem(400,key+" is required and must be at most "+max+" characters.");return value;
    }
    public static List<String> list(JsonObject b,String key){
        if(b==null||!b.has(key)||!b.get(key).isJsonArray())throw new Problem(400,key+" must be a list.");
        if(b.getAsJsonArray(key).size()>100)throw new Problem(400,"Use at most 100 entries for "+key);
        LinkedHashMap<String,String> result=new LinkedHashMap<>();for(JsonElement item:b.getAsJsonArray(key)){
            if(!item.isJsonPrimitive()||!item.getAsJsonPrimitive().isString())throw new Problem(400,"List entries must be text.");String s=item.getAsString().trim();if(s.isEmpty()||s.length()>150)throw new Problem(400,"Entries must be 1–150 characters.");result.putIfAbsent(s.toLowerCase(Locale.ROOT),s);
        }return new ArrayList<>(result.values());
    }
    public static JsonObject validate(JsonObject b){
        JsonObject q=new JsonObject();
        q.addProperty("schemaVersion",2);
        q.addProperty("firstName",text(b,"firstName",100,true));q.addProperty("lastName",text(b,"lastName",100,true));
        q.addProperty("facultyPI",text(b,"facultyPI",200,true));
        q.addProperty("taQuarters",integer(b,"taQuarters",0,100));
        String candidacy=text(b,"advancedToCandidacy",20,true);if(!Set.of("yes","no","not_applicable").contains(candidacy))throw new Problem(400,"Choose a candidacy status.");q.addProperty("advancedToCandidacy",candidacy);
        q.addProperty("sexualHarassmentTrainingCompleted",bool(b,"sexualHarassmentTrainingCompleted"));
        q.addProperty("additionalEmployment",text(b,"additionalEmployment",1000,true));
        q.addProperty("documentedAccommodation",bool(b,"documentedAccommodation"));q.addProperty("accommodationNotes",text(b,"accommodationNotes",2000,false));
        for(String field:List.of("ackSchedule","ackEnrollment")){if(!bool(b,field))throw new Problem(400,"Confirm both scheduling and full-time enrollment / fees acknowledgments.");q.addProperty(field,true);}
        for(String field:List.of("schedulingConflicts","testingScenario"))q.addProperty(field,text(b,field,4000,false));
        String level=text(b,"studentLevel",30,true);
        if(!Set.of("undergraduate","masters","phd").contains(level))throw new Problem(400,"Choose undergraduate, masters or phd.");q.addProperty("studentLevel",level);
        if(!"undergraduate".equals(level))q.addProperty("graduateStartYear",integer(b,"graduateStartYear",1900,java.time.Year.now().getValue()));else if(b.has("graduateStartYear")&&!b.get("graduateStartYear").isJsonNull()&&!b.get("graduateStartYear").getAsString().isBlank())q.addProperty("graduateStartYear",integer(b,"graduateStartYear",1900,java.time.Year.now().getValue()));
        String id=text(b,"uciStudentId",8,true);if(!id.matches("[0-9]{8}"))throw new Problem(400,"UCI student ID must have 8 digits.");q.addProperty("uciStudentId",id);
        String email=text(b,"uciEmail",254,true);if(!email.matches("(?i)[^\\s@]+@(?:[a-z0-9-]+\\.)*uci\\.edu"))throw new Problem(400,"Enter your UCI email address.");q.addProperty("uciEmail",email);
        q.addProperty("program",text(b,"program",200,true));
        for(String field:List.of("interests","skills"))q.add(field,JsonUtil.getGson().toJsonTree(list(b,field)));
        for(String field:List.of("teachingExperience","researchExperience","additionalComments"))q.addProperty(field,text(b,field,4000,false));
        String training=text(b,"trainingStatus",30,true);if(!Set.of("not_started","in_progress","completed","not_applicable").contains(training))throw new Problem(400,"Choose a training status.");q.addProperty("trainingStatus",training);
        if(!b.has("acknowledged")||!b.get("acknowledged").isJsonPrimitive()||!b.get("acknowledged").getAsJsonPrimitive().isBoolean()||!b.get("acknowledged").getAsBoolean())throw new Problem(400,"Confirm the accuracy of the submitted information.");q.addProperty("acknowledged",true);return q;
    }
    public static boolean bool(JsonObject b,String key){JsonElement v=b.get(key);if(v==null||!v.isJsonPrimitive()||!v.getAsJsonPrimitive().isBoolean())throw new Problem(400,key+" must be Yes or No.");return v.getAsBoolean();}
    public static int integer(JsonObject b,String key,int min,int max){try{int n=b.get(key).getAsBigDecimal().intValueExact();if(n<min||n>max)throw new Exception();return n;}catch(Exception e){throw new Problem(400,key+" must be a whole number between "+min+" and "+max+".");}}
    public JsonObject snapshot(String id){
        Profile p=profile(id);if(p.getQualifications()==null)throw new Problem(400,"Complete TA Application Profile before applying.");
        JsonObject q=validate(p.getQualifications());User u=data.getData().getUsers().stream().filter(x->id.equals(x.getId())).findFirst().orElseThrow();
        q.addProperty("fullName",q.get("firstName").getAsString()+" "+q.get("lastName").getAsString());q.addProperty("submittedAt",java.time.Instant.now().toString());
        JsonObject academic=JsonUtil.getGson().toJsonTree(p).getAsJsonObject();academic.remove("qualifications");q.add("academicProfile",academic);return q;
    }
}
