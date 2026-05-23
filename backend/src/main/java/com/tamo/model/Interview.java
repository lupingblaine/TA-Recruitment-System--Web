package com.tamo.model;

/**
 * Represents a Interview entity in the system.
 */
public class Interview {
    /**
     * The id of the entity.
     */
    private String id;
    /**
     * The applicationId of the entity.
     */
    private String applicationId;
    /**
     * The candidateId of the entity.
     */
    private String candidateId;
    /**
     * The candidateName of the entity.
     */
    private String candidateName;
    /**
     * The moduleCode of the entity.
     */
    private String moduleCode;
    /**
     * The moduleName of the entity.
     */
    private String moduleName;
    /**
     * The date of the entity.
     */
    private String date;
    /**
     * The startTime of the entity.
     */
    private String startTime;
    /**
     * The endTime of the entity.
     */
    private String endTime;
    private String type;       // video | in-person
    /**
     * The location of the entity.
     */
    private String location;
    private String status;     // scheduled | upcoming | completed | cancelled
    private String outcome;    // null | offer | reject
    /**
     * The organiserId of the entity.
     */
    private String organiserId;

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
     * Gets the applicationid.
     * @return the applicationid
     */
    public String getApplicationId() { return applicationId; }
    /**
     * Sets the applicationid.
     * @param applicationId the applicationid to set
     */
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    /**
     * Gets the candidateid.
     * @return the candidateid
     */
    public String getCandidateId() { return candidateId; }
    /**
     * Sets the candidateid.
     * @param candidateId the candidateid to set
     */
    public void setCandidateId(String candidateId) { this.candidateId = candidateId; }
    /**
     * Gets the candidatename.
     * @return the candidatename
     */
    public String getCandidateName() { return candidateName; }
    /**
     * Sets the candidatename.
     * @param candidateName the candidatename to set
     */
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }
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
     * Gets the modulename.
     * @return the modulename
     */
    public String getModuleName() { return moduleName; }
    /**
     * Sets the modulename.
     * @param moduleName the modulename to set
     */
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    /**
     * Gets the date.
     * @return the date
     */
    public String getDate() { return date; }
    /**
     * Sets the date.
     * @param date the date to set
     */
    public void setDate(String date) { this.date = date; }
    /**
     * Gets the starttime.
     * @return the starttime
     */
    public String getStartTime() { return startTime; }
    /**
     * Sets the starttime.
     * @param startTime the starttime to set
     */
    public void setStartTime(String startTime) { this.startTime = startTime; }
    /**
     * Gets the endtime.
     * @return the endtime
     */
    public String getEndTime() { return endTime; }
    /**
     * Sets the endtime.
     * @param endTime the endtime to set
     */
    public void setEndTime(String endTime) { this.endTime = endTime; }
    /**
     * Gets the type.
     * @return the type
     */
    public String getType() { return type; }
    /**
     * Sets the type.
     * @param type the type to set
     */
    public void setType(String type) { this.type = type; }
    /**
     * Gets the location.
     * @return the location
     */
    public String getLocation() { return location; }
    /**
     * Sets the location.
     * @param location the location to set
     */
    public void setLocation(String location) { this.location = location; }
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
    /**
     * Gets the outcome.
     * @return the outcome
     */
    public String getOutcome() { return outcome; }
    /**
     * Sets the outcome.
     * @param outcome the outcome to set
     */
    public void setOutcome(String outcome) { this.outcome = outcome; }
    /**
     * Gets the organiserid.
     * @return the organiserid
     */
    public String getOrganiserId() { return organiserId; }
    /**
     * Sets the organiserid.
     * @param organiserId the organiserid to set
     */
    public void setOrganiserId(String organiserId) { this.organiserId = organiserId; }
}
