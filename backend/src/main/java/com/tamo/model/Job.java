package com.tamo.model;

import java.util.List;

/**
 * Represents a Job entity in the system.
 */
public class Job {
    /**
     * The id of the entity.
     */
    private String id;
    /**
     * The moduleId of the entity.
     */
    private String moduleId;
    /**
     * The moduleCode of the entity.
     */
    private String moduleCode;
    /**
     * The title of the entity.
     */
    private String title;
    /**
     * The description of the entity.
     */
    private String description;
    /**
     * The professor of the entity.
     */
    private String professor;
    /**
     * The department of the entity.
     */
    private String department;
    private String type;        // Part-time | Full-time
    /**
     * The hoursPerWeek of the entity.
     */
    private int hoursPerWeek;
    /**
     * The positions of the entity.
     */
    private int positions;
    /**
     * The deadline of the entity.
     */
    private String deadline;
    private String status;      // active | draft | closed
    /**
     * The tags of the entity.
     */
    private List<String> tags;
    /**
     * The requirements of the entity.
     */
    private List<String> requirements;
    /**
     * The responsibilities of the entity.
     */
    private List<String> responsibilities;
    /**
     * The organiserId of the entity.
     */
    private String organiserId;
    /**
     * The createdAt of the entity.
     */
    private String createdAt;
    /**
     * The workDates of the entity.
     */
    private List<String> workDates;
    // Null retains historical course-wide scope; explicit selections belong to one cycle.
    private List<String> sectionIds;
    public List<String> getSectionIds() { return sectionIds; }
    public void setSectionIds(List<String> value) { sectionIds = value; }


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
     * Gets the moduleid.
     * @return the moduleid
     */
    public String getModuleId() { return moduleId; }
    /**
     * Sets the moduleid.
     * @param moduleId the moduleid to set
     */
    public void setModuleId(String moduleId) { this.moduleId = moduleId; }
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
     * Gets the title.
     * @return the title
     */
    public String getTitle() { return title; }
    /**
     * Sets the title.
     * @param title the title to set
     */
    public void setTitle(String title) { this.title = title; }
    /**
     * Gets the description.
     * @return the description
     */
    public String getDescription() { return description; }
    /**
     * Sets the description.
     * @param description the description to set
     */
    public void setDescription(String description) { this.description = description; }
    /**
     * Gets the professor.
     * @return the professor
     */
    public String getProfessor() { return professor; }
    /**
     * Sets the professor.
     * @param professor the professor to set
     */
    public void setProfessor(String professor) { this.professor = professor; }
    /**
     * Gets the department.
     * @return the department
     */
    public String getDepartment() { return department; }
    /**
     * Sets the department.
     * @param department the department to set
     */
    public void setDepartment(String department) { this.department = department; }
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
     * Gets the positions.
     * @return the positions
     */
    public int getPositions() { return positions; }
    /**
     * Sets the positions.
     * @param positions the positions to set
     */
    public void setPositions(int positions) { this.positions = positions; }
    /**
     * Gets the deadline.
     * @return the deadline
     */
    public String getDeadline() { return deadline; }
    /**
     * Sets the deadline.
     * @param deadline the deadline to set
     */
    public void setDeadline(String deadline) { this.deadline = deadline; }
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
     * Gets the tags.
     * @return the tags
     */
    public List<String> getTags() { return tags; }
    /**
     * Sets the tags.
     * @param tags the tags to set
     */
    public void setTags(List<String> tags) { this.tags = tags; }
    /**
     * Gets the requirements.
     * @return the requirements
     */
    public List<String> getRequirements() { return requirements; }
    /**
     * Sets the requirements.
     * @param requirements the requirements to set
     */
    public void setRequirements(List<String> requirements) { this.requirements = requirements; }
    /**
     * Gets the responsibilities.
     * @return the responsibilities
     */
    public List<String> getResponsibilities() { return responsibilities; }
    /**
     * Sets the responsibilities.
     * @param responsibilities the responsibilities to set
     */
    public void setResponsibilities(List<String> responsibilities) { this.responsibilities = responsibilities; }
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
    /**
     * Gets the createdat.
     * @return the createdat
     */
    public String getCreatedAt() { return createdAt; }
    /**
     * Sets the createdat.
     * @param createdAt the createdat to set
     */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    /**
     * Gets the workdates.
     * @return the workdates
     */
    public List<String> getWorkDates() { return workDates; }
    /**
     * Sets the workdates.
     * @param workDates the workdates to set
     */
    public void setWorkDates(List<String> workDates) { this.workDates = workDates; }
    private com.google.gson.JsonElement matchingCriteria;
    public com.google.gson.JsonObject getMatchingCriteria(){return matchingCriteria==null||matchingCriteria.isJsonNull()?null:matchingCriteria.getAsJsonObject();}
    public void setMatchingCriteria(com.google.gson.JsonObject value){matchingCriteria=value;}
    private com.google.gson.JsonElement periodCriteria;
    public com.google.gson.JsonObject getPeriodCriteria(){return periodCriteria==null||periodCriteria.isJsonNull()?null:periodCriteria.getAsJsonObject();}
    public void setPeriodCriteria(com.google.gson.JsonObject value){periodCriteria=value;}
}
