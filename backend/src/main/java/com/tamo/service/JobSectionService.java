package com.tamo.service;

import com.google.gson.*;
import com.tamo.model.*;
import java.util.*;

/** Shared posting scope. Legacy postings retain their course-wide scope. */
public final class JobSectionService {
    private JobSectionService() {}
    public static boolean includes(Job job, CourseSection section) {
        return job.getSectionIds()==null || job.getSectionIds().contains(section.getId());
    }
    public static List<String> validate(DataStore data, String moduleId, JsonElement value) {
        if(value==null || !value.isJsonArray() || value.getAsJsonArray().isEmpty() || value.getAsJsonArray().size()>70)
            throw new IllegalArgumentException("Select at least one active Section (maximum 70).");
        List<String> ids=new ArrayList<>(); String period=null;
        for(JsonElement element:value.getAsJsonArray()) {
            if(!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) throw new IllegalArgumentException("Invalid Section selection.");
            String id=element.getAsString();
            CourseSection section=data.getCourseSections().stream().filter(s->id.equals(s.getId()) && Objects.equals(moduleId,s.getModuleId()) && "active".equals(s.getStatus())).findFirst()
                .orElseThrow(()->new IllegalArgumentException("Select active Sections belonging to this course."));
            if(section.getPeriodId()==null) throw new IllegalArgumentException("Copy historical Sections into an application cycle first.");
            if(period!=null && !period.equals(section.getPeriodId())) throw new IllegalArgumentException("Selected Sections must belong to the same application cycle.");
            period=section.getPeriodId();
            if(ids.contains(id)) throw new IllegalArgumentException("A Section may only be selected once.");
            ids.add(id);
        }
        return ids;
    }
    public static boolean acceptsPeriod(DataStore data, Job job, String periodId) {
        return job.getSectionIds()==null || data.getCourseSections().stream().anyMatch(s->includes(job,s) && Objects.equals(s.getModuleId(),job.getModuleId()) && Objects.equals(s.getPeriodId(),periodId) && "active".equals(s.getStatus()));
    }
}
