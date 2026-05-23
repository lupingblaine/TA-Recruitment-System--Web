package com.tamo.model;

/**
 * Represents a Department entity in the system.
 */
public class Department {
    /**
     * The id of the entity.
     */
    private String id;
    /**
     * The name of the entity.
     */
    private String name;
    /**
     * The moduleCount of the entity.
     */
    private int moduleCount;
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
     * Gets the modulecount.
     * @return the modulecount
     */
    public int getModuleCount() { return moduleCount; }
    /**
     * Sets the modulecount.
     * @param moduleCount the modulecount to set
     */
    public void setModuleCount(int moduleCount) { this.moduleCount = moduleCount; }
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
