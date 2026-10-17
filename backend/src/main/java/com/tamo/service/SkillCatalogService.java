package com.tamo.service;
import com.google.gson.*;
import com.tamo.model.*;
import com.tamo.util.JsonUtil;
import java.util.*;
/** Sponsor vocabulary plus the current staff-maintained course/section options. */
public final class SkillCatalogService {
 public static final List<String> DEFAULTS=List.of("Genetics","Lab Tech","Python","Active Teaching","Public Speaking","Biochemistry","Mass Spectrometry","Physiology","Human Anatomy","R","Linux","Git","Slurm","General Bio");
 public static String canonical(String s){return s.trim().equalsIgnoreCase("Mass Spec")?"Mass Spectrometry":s.trim();}
 public static List<String> skills(DataStore ds){TreeMap<String,String> values=new TreeMap<>();DEFAULTS.forEach(s->values.put(s.toLowerCase(Locale.ROOT),s));
  for(Job j:ds.getJobs()){add(values,j.getMatchingCriteria());if(j.getPeriodCriteria()!=null)j.getPeriodCriteria().entrySet().forEach(e->{if(e.getValue().isJsonObject())add(values,e.getValue().getAsJsonObject());});}
  for(CourseSection s:ds.getCourseSections())if("active".equals(s.getStatus()))add(values,s.getMatchingCriteria());return new ArrayList<>(values.values());}
 private static void add(Map<String,String> values,JsonObject c){if(c!=null)for(String k:List.of("skillOptions","requiredSkills","preferredSkills"))if(c.has(k)&&c.get(k).isJsonArray())for(JsonElement e:c.getAsJsonArray(k)){String s=canonical(e.getAsString());values.put(s.toLowerCase(Locale.ROOT),s);}}
 public static void validateSelection(DataStore ds,JsonObject q){Set<String> allowed=new HashSet<>();skills(ds).forEach(s->allowed.add(s.toLowerCase(Locale.ROOT)));for(JsonElement e:q.getAsJsonArray("skills"))if(!allowed.contains(canonical(e.getAsString()).toLowerCase(Locale.ROOT)))throw new QualificationService.Problem(400,"Choose skills provided by course staff: "+e.getAsString());}
 public static JsonObject options(DataStore ds){JsonObject r=new JsonObject();r.add("skills",JsonUtil.getGson().toJsonTree(skills(ds)));JsonArray courses=new JsonArray();for(com.tamo.model.Module m:ds.getModules())if("active".equals(m.getStatus())){JsonObject c=new JsonObject();c.addProperty("code",m.getCode());c.addProperty("name",m.getName());courses.add(c);}r.add("courses",courses);return r;}
}
