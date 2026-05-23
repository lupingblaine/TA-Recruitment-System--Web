package com.tamo.model;

/**
 * Represents a Notification entity in the system.
 */
public class Notification {
    /**
     * The id of the entity.
     */
    private String id;
    /**
     * The userId of the entity.
     */
    private String userId;
    private String type;        // status_change | interview_scheduled | offer | rejection
    /**
     * The title of the entity.
     */
    private String title;
    /**
     * The message of the entity.
     */
    private String message;
    private String relatedId;   // applicationId or interviewId
    /**
     * The createdAt of the entity.
     */
    private String createdAt;
    /**
     * The read of the entity.
     */
    private boolean read;

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
     * Gets the userid.
     * @return the userid
     */
    public String getUserId() { return userId; }
    /**
     * Sets the userid.
     * @param userId the userid to set
     */
    public void setUserId(String userId) { this.userId = userId; }
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
     * Gets the message.
     * @return the message
     */
    public String getMessage() { return message; }
    /**
     * Sets the message.
     * @param message the message to set
     */
    public void setMessage(String message) { this.message = message; }
    /**
     * Gets the relatedid.
     * @return the relatedid
     */
    public String getRelatedId() { return relatedId; }
    /**
     * Sets the relatedid.
     * @param relatedId the relatedid to set
     */
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }
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
     * Gets the read.
     * @return the read
     */
    public boolean isRead() { return read; }
    /**
     * Sets the read.
     * @param read the read to set
     */
    public void setRead(boolean read) { this.read = read; }
}
