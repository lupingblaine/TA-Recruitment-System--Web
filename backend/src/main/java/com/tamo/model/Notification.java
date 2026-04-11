package com.tamo.model;

public class Notification {
    private String id;
    private String userId;
    private String type;        // status_change | interview_scheduled | offer | rejection
    private String title;
    private String message;
    private String relatedId;   // applicationId or interviewId
    private String createdAt;
    private boolean read;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getRelatedId() { return relatedId; }
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
