package com.tamo.model;

public class WorkloadAssignment {
    private String id;
    private String taUserId;
    private String moduleCode;
    private int hoursPerWeek;
    private String term;
    private String status;  // active | inactive

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTaUserId() { return taUserId; }
    public void setTaUserId(String taUserId) { this.taUserId = taUserId; }
    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }
    public int getHoursPerWeek() { return hoursPerWeek; }
    public void setHoursPerWeek(int hoursPerWeek) { this.hoursPerWeek = hoursPerWeek; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
