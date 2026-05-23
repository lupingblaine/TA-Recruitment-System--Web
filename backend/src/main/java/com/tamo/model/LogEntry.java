package com.tamo.model;

/**
 * Represents a LogEntry entity in the system.
 */
public class LogEntry {
    /**
     * The id of the entity.
     */
    private String id;
    /**
     * The timestamp of the entity.
     */
    private String timestamp;
    /**
     * The action of the entity.
     */
    private String action;
    /**
     * The actor of the entity.
     */
    private String actor;
    /**
     * The actorId of the entity.
     */
    private String actorId;
    private String type;     // info | success | warning | error
    /**
     * The details of the entity.
     */
    private String details;
    /**
     * The ip of the entity.
     */
    private String ip;

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
     * Gets the timestamp.
     * @return the timestamp
     */
    public String getTimestamp() { return timestamp; }
    /**
     * Sets the timestamp.
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    /**
     * Gets the action.
     * @return the action
     */
    public String getAction() { return action; }
    /**
     * Sets the action.
     * @param action the action to set
     */
    public void setAction(String action) { this.action = action; }
    /**
     * Gets the actor.
     * @return the actor
     */
    public String getActor() { return actor; }
    /**
     * Sets the actor.
     * @param actor the actor to set
     */
    public void setActor(String actor) { this.actor = actor; }
    /**
     * Gets the actorid.
     * @return the actorid
     */
    public String getActorId() { return actorId; }
    /**
     * Sets the actorid.
     * @param actorId the actorid to set
     */
    public void setActorId(String actorId) { this.actorId = actorId; }
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
     * Gets the details.
     * @return the details
     */
    public String getDetails() { return details; }
    /**
     * Sets the details.
     * @param details the details to set
     */
    public void setDetails(String details) { this.details = details; }
    /**
     * Gets the ip.
     * @return the ip
     */
    public String getIp() { return ip; }
    /**
     * Sets the ip.
     * @param ip the ip to set
     */
    public void setIp(String ip) { this.ip = ip; }
}
