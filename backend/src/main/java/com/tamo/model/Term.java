package com.tamo.model;

/**
 * Represents a Term entity in the system.
 */
public class Term {
    /**
     * The id of the entity.
     */
    private String id;
    /**
     * The name of the entity.
     */
    private String name;
    /**
     * The startDate of the entity.
     */
    private String startDate;
    /**
     * The endDate of the entity.
     */
    private String endDate;
    private String status;  // active | draft | inactive

    /**
     * Gets the id.
     * @return the id
     */
    public String getId() { return id; }
    /**
     * Sets the id.
     * @param id the id to set
     */
    public void setId(String id) { this.id = id; }
    /**
     * Gets the name.
     * @return the name
     */
    public String getName() { return name; }
    /**
     * Sets the name.
     * @param name the name to set
     */
    public void setName(String name) { this.name = name; }
    /**
     * Gets the startdate.
     * @return the startdate
     */
    public String getStartDate() { return startDate; }
    /**
     * Sets the startdate.
     * @param startDate the startdate to set
     */
    public void setStartDate(String startDate) { this.startDate = startDate; }
    /**
     * Gets the enddate.
     * @return the enddate
     */
    public String getEndDate() { return endDate; }
    /**
     * Sets the enddate.
     * @param endDate the enddate to set
     */
    public void setEndDate(String endDate) { this.endDate = endDate; }
    /**
     * Gets the status.
     * @return the status
     */
    public String getStatus() { return status; }
    /**
     * Sets the status.
     * @param status the status to set
     */
    public void setStatus(String status) { this.status = status; }
}
