package com.tamo.model;

import java.util.List;

/** A student's recurring weekly schedule for one application period. */
public class WeeklyAvailability {
    private boolean conflictsOnly;
    public boolean getConflictsOnly(){return conflictsOnly;}
    public void setConflictsOnly(boolean value){conflictsOnly=value;}
    private String id;
    private String applicantId;
    private String periodId;
    private String timeZone;
    private List<Slot> availableSlots;
    private List<Slot> conflictSlots;
    private boolean noAvailability;
    private String notes;
    private String updatedAt;
    public String getId() { return id; }
    public void setId(String value) { id = value; }
    public String getApplicantId() { return applicantId; }
    public void setApplicantId(String value) { applicantId = value; }
    public String getPeriodId() { return periodId; }
    public void setPeriodId(String value) { periodId = value; }
    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String value) { timeZone = value; }
    public List<Slot> getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(List<Slot> value) { availableSlots = value; }
    public List<Slot> getConflictSlots() { return conflictSlots; }
    public void setConflictSlots(List<Slot> value) { conflictSlots = value; }
    public boolean getNoAvailability() { return noAvailability; }
    public void setNoAvailability(boolean value) { noAvailability = value; }
    public String getNotes() { return notes; }
    public void setNotes(String value) { notes = value; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String value) { updatedAt = value; }
    public static class Slot {
        private int dayOfWeek;
        public int getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(int value) { dayOfWeek = value; }
        private String startTime;
        public String getStartTime() { return startTime; }
        public void setStartTime(String value) { startTime = value; }
        private String endTime;
        public String getEndTime() { return endTime; }
        public void setEndTime(String value) { endTime = value; }
        private String reason;
        public String getReason() { return reason; }
        public void setReason(String value) { reason = value; }
    }
}
