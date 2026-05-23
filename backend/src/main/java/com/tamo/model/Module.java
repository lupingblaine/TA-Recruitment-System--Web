package com.tamo.model;

/**
 * Represents a Module entity in the system.
 */
public class Module {
    /**
     * The id of the entity.
     */
    private String id;
    /**
     * The code of the entity.
     */
    private String code;
    /**
     * The name of the entity.
     */
    private String name;
    /**
     * The department of the entity.
     */
    private String department;
    /**
     * The organiserId of the entity.
     */
    private String organiserId;
    /**
     * The organiserName of the entity.
     */
    private String organiserName;
    /**
     * The term of the entity.
     */
    private String term;
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
     * Gets the code.
     * @return the code
     */
    public String getCode() { return code; }
    /**
     * Sets the code.
     * @param code the code to set
     */
    public void setCode(String code) { this.code = code; }
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
     * Gets the organisername.
     * @return the organisername
     */
    public String getOrganiserName() { return organiserName; }
    /**
     * Sets the organisername.
     * @param organiserName the organisername to set
     */
    public void setOrganiserName(String organiserName) { this.organiserName = organiserName; }
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
