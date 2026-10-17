package com.tamo.service;
import com.google.gson.*;
import com.tamo.model.*;
import com.tamo.util.JsonUtil;
import java.util.*;

/** Deterministic decision support. Missing requirements are explained, never an automatic selection. */
public class CriteriaMatchingService{
 public static JsonObject validate(JsonObject b){
  JsonObject c=new JsonObject();for(String key:List.of("requiredSkills","preferredSkills","subjectInterests","studentLevels"))c.add(key,JsonUtil.getGson().toJsonTree(QualificationService.list(b,key)));
  for(JsonElement level:c.getAsJsonArray("studentLevels"))if(!Set.of("undergraduate","masters","phd").contains(level.getAsString()))throw new QualificationService.Problem(400,"Student levels must be undergraduate, masters or phd.");
  for(String key:List.of("trainingRequired","teachingExperiencePreferred")){if(!b.has(key)||!b.get(key).isJsonPrimitive()||!b.get(key).getAsJsonPrimitive().isBoolean())throw new QualificationService.Problem(400,key+" must be boolean.");c.addProperty(key,b.get(key).getAsBoolean());}
  c.add("skillOptions",b.has("skillOptions")?JsonUtil.getGson().toJsonTree(QualificationService.list(b,"skillOptions")):new JsonArray());
  c.addProperty("sexualHarassmentTrainingRequired",b.has("sexualHarassmentTrainingRequired")&&QualificationService.bool(b,"sexualHarassmentTrainingRequired"));
  c.addProperty("updatedAt",java.time.Instant.now().toString());return c;
 }
 private static Set<String> terms(JsonObject b,String key){Set<String> result=new TreeSet<>();if(b!=null&&b.has(key)&&b.get(key).isJsonArray())for(JsonElement v:b.getAsJsonArray(key))if(v.isJsonPrimitive())result.add(SkillCatalogService.canonical(v.getAsString()).toLowerCase(Locale.ROOT));return result;}
 public JsonObject evaluateAcrossSections(DataStore ds,Application app,Job job){
  if(job==null)return evaluate(app,job);List<CourseSection> sections=ds.getCourseSections().stream().filter(s->JobSectionService.includes(job,s)&&Objects.equals(s.getModuleId(),job.getModuleId())&&Objects.equals(s.getPeriodId(),app.getPeriodId())&&"active".equals(s.getStatus())&&s.getMatchingCriteria()!=null).toList();
  if(sections.isEmpty())return evaluate(app,job);JsonObject best=null;for(CourseSection section:sections){JsonObject r=evaluate(app,job,section);r.addProperty("sectionId",section.getId());r.addProperty("sectionName",section.getName());if(best==null||r.getAsJsonArray("missingRequired").size()<best.getAsJsonArray("missingRequired").size()||r.getAsJsonArray("missingRequired").size()==best.getAsJsonArray("missingRequired").size()&&scoreValue(r)>scoreValue(best))best=r;}
  best.addProperty("message","Best Section qualification match: "+best.get("sectionName").getAsString()+". Review each Section's requirements and time conflicts separately. "+best.get("message").getAsString());return best;
 }
 private double scoreValue(JsonObject r){return r.get("score").isJsonNull()?-1:r.get("score").getAsDouble();}
 public JsonObject evaluate(Application app,Job job){return evaluate(app,job,null);}
 public JsonObject evaluate(Application app,Job job,CourseSection section){
  JsonObject result=new JsonObject();JsonArray matched=new JsonArray(),missing=new JsonArray(),preferred=new JsonArray();result.add("matched",matched);result.add("missingRequired",missing);result.add("unmatchedPreferred",preferred);result.add("score",JsonNull.INSTANCE);
  JsonObject q=app.getQualificationSnapshot(),c=null;
  if(job!=null&&job.getPeriodCriteria()!=null&&app.getPeriodId()!=null&&job.getPeriodCriteria().has(app.getPeriodId()))c=job.getPeriodCriteria().getAsJsonObject(app.getPeriodId());
  else if(job!=null&&(app.getPeriodId()==null||"period_legacy".equals(app.getPeriodId())))c=job.getMatchingCriteria();
  if(section!=null&&section.getMatchingCriteria()!=null)c=section.getMatchingCriteria();
  if(q==null||c==null){result.addProperty("status","needs_confirmation");result.addProperty("message",q==null?"No submitted qualification snapshot.":"No structured matching criteria configured.");return result;}
  result.add("criteria",c.deepCopy());int total=0,points=0;Set<String> skills=terms(q,"skills");
  for(String key:List.of("requiredSkills","preferredSkills"))for(String skill:terms(c,key)){int weight=key.equals("requiredSkills")?2:1;total+=weight;if(skills.contains(skill)){points+=weight;matched.add((weight==2?"Required skill: ":"Preferred skill: ")+skill);}else if(weight==2)missing.add("Required skill: "+skill);else preferred.add("Preferred skill: "+skill);}
  Set<String> levels=terms(c,"studentLevels");if(!levels.isEmpty()){total+=2;if(q.has("studentLevel")&&levels.contains(q.get("studentLevel").getAsString())){points+=2;matched.add("Student level: "+q.get("studentLevel").getAsString());}else missing.add("Student level outside configured course criteria");}
  if(c.get("trainingRequired").getAsBoolean()){total+=2;if(q.has("trainingStatus")&&"completed".equals(q.get("trainingStatus").getAsString())){points+=2;matched.add("Required training reported completed");}else missing.add("Required training not reported completed");}
  if(c.has("sexualHarassmentTrainingRequired")&&c.get("sexualHarassmentTrainingRequired").getAsBoolean()){total+=2;if(q.has("sexualHarassmentTrainingCompleted")&&q.get("sexualHarassmentTrainingCompleted").getAsBoolean()){points+=2;matched.add("Online Sexual Harassment Training reported completed");}else missing.add("Online Sexual Harassment Training not reported completed");}
  Set<String> interests=terms(c,"subjectInterests");if(!interests.isEmpty()){total++;interests.retainAll(terms(q,"interests"));if(!interests.isEmpty()){points++;matched.add("Shared interests: "+String.join(", ",interests));}else preferred.add("No shared course/subject interest");}
  if(c.get("teachingExperiencePreferred").getAsBoolean()){total++;if(q.has("teachingExperience")&&!q.get("teachingExperience").getAsString().isBlank()){points++;matched.add("Applicant reports teaching experience");}else preferred.add("No teaching experience reported");}
  result.addProperty("status",missing.isEmpty()?"meets_required":"missing_required");if(total>0)result.addProperty("score",Math.round(points*100.0/total));result.addProperty("message",total==0?"No matching criteria selected. Review manually.":"Required criteria weight 2; preferences weight 1. Exact case-insensitive skill/interest matching against submitted information. Staff make the final decision.");return result;
 }
}
