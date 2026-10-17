package com.tamo.model;

import java.util.List;

/** A lecture, lab or discussion belonging to one course record. */
public class CourseSection {
    private String id;
    private String moduleId;
    private String name;
    private String type;
    private String timeZone;
    private List<MeetingTime> meetingTimes;
    private int taPositions;
    private double hoursPerWeek;
    private String location;
    private String notes;
    private String status;
    private String createdAt;
    private String updatedAt;
    public String getId() { return id; }
    public void setId(String value) { id = value; }
    public String getModuleId() { return moduleId; }
    public void setModuleId(String value) { moduleId = value; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getType() { return type; }
    public void setType(String value) { type = value; }
    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String value) { timeZone = value; }
    public List<MeetingTime> getMeetingTimes() { return meetingTimes; }
    public void setMeetingTimes(List<MeetingTime> value) { meetingTimes = value; }
    public int getTaPositions() { return taPositions; }
    public void setTaPositions(int value) { taPositions = value; }
    public double getHoursPerWeek() { return hoursPerWeek; }
    public void setHoursPerWeek(double value) { hoursPerWeek = value; }
    public String getLocation() { return location; }
    public void setLocation(String value) { location = value; }
    public String getNotes() { return notes; }
    public void setNotes(String value) { notes = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String value) { createdAt = value; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String value) { updatedAt = value; }
    public static class MeetingTime {
        private int dayOfWeek;
        public int getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(int value) { dayOfWeek = value; }
        private String startTime;
        public String getStartTime() { return startTime; }
        public void setStartTime(String value) { startTime = value; }
        private String endTime;
        public String getEndTime() { return endTime; }
        public void setEndTime(String value) { endTime = value; }
    }
    private com.google.gson.JsonElement matchingCriteria;
    public com.google.gson.JsonObject getMatchingCriteria(){return matchingCriteria==null||matchingCriteria.isJsonNull()?null:matchingCriteria.getAsJsonObject();}
    public void setMatchingCriteria(com.google.gson.JsonObject value){matchingCriteria=value;}
    private String periodId;
    public String getPeriodId(){return periodId;}
    public void setPeriodId(String value){periodId=value;}
    private String facultyName;
    public String getFacultyName(){return facultyName;}
    public void setFacultyName(String value){facultyName=value;}
}
