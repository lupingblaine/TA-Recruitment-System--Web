package com.tamo.model;

/** An administrator-managed intake window; timestamps use ISO-8601 with a UTC offset. */
public class ApplicationPeriod {
    private String id;
    private String name;
    private String termId;
    private String opensAt;
    private String closesAt;
    private String status;
    private boolean legacy;
    private String createdAt;
    private String updatedAt;
    public String getId() { return id; }
    public void setId(String value) { id = value; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getTermId() { return termId; }
    public void setTermId(String value) { termId = value; }
    public String getOpensAt() { return opensAt; }
    public void setOpensAt(String value) { opensAt = value; }
    public String getClosesAt() { return closesAt; }
    public void setClosesAt(String value) { closesAt = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public boolean getLegacy() { return legacy; }
    public void setLegacy(boolean value) { legacy = value; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String value) { createdAt = value; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String value) { updatedAt = value; }
}
