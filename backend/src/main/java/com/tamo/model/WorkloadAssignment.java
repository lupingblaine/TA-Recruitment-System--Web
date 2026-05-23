package com.tamo.model;

/**
 * Represents a WorkloadAssignment entity in the system.
 */
public class WorkloadAssignment {
    /**
     * The id of the entity.
     */
    private String id;
    /**
     * The taUserId of the entity.
     */
    private String taUserId;
    /**
     * The moduleCode of the entity.
     */
    private String moduleCode;
    /**
     * The hoursPerWeek of the entity.
     */
    private int hoursPerWeek;
    /**
     * The term of the entity.
     */
    private String term;
    private String status;  // active | inactive

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
     * Gets the tauserid.
     * @return the tauserid
     */
    public String getTaUserId() { return taUserId; }
    /**
     * Sets the tauserid.
     * @param taUserId the tauserid to set
     */
    public void setTaUserId(String taUserId) { this.taUserId = taUserId; }
    /**
     * Gets the modulecode.
     * @return the modulecode
     */
    public String getModuleCode() { return moduleCode; }
    /**
     * Sets the modulecode.
     * @param moduleCode the modulecode to set
     */
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }
    /**
     * Gets the hoursperweek.
     * @return the hoursperweek
     */
    public int getHoursPerWeek() { return hoursPerWeek; }
    /**
     * Sets the hoursperweek.
     * @param hoursPerWeek the hoursperweek to set
     */
    public void setHoursPerWeek(int hoursPerWeek) { this.hoursPerWeek = hoursPerWeek; }
    /**
     * Gets the term.
     * @return the term
     */
    public String getTerm() { return term; }
    /**
     * Sets the term.
     * @param term the term to set
     */
    public void setTerm(String term) { this.term = term; }
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
