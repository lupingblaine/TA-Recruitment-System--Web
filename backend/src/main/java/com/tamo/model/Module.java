package com.tamo.model;

public class Module {
    private String id;
    private String code;
    private String name;
    private String department;
    private String organiserId;
    private String organiserName;
    private String term;
    private String status;  // active | draft | inactive

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getOrganiserId() { return organiserId; }
    public void setOrganiserId(String organiserId) { this.organiserId = organiserId; }
    public String getOrganiserName() { return organiserName; }
    public void setOrganiserName(String organiserName) { this.organiserName = organiserName; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
