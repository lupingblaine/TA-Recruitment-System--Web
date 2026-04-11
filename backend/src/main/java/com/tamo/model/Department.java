package com.tamo.model;

public class Department {
    private String id;
    private String name;
    private int moduleCount;
    private String status;  // active | inactive

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getModuleCount() { return moduleCount; }
    public void setModuleCount(int moduleCount) { this.moduleCount = moduleCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
