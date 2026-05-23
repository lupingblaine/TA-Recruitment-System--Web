package com.tamo.model;

/**
 * Represents a Message entity in the system.
 */
public class Message {
    /**
     * The id of the entity.
     */
    private String id;
    /**
     * The conversationId of the entity.
     */
    private String conversationId;
    /**
     * The senderId of the entity.
     */
    private String senderId;
    /**
     * The receiverId of the entity.
     */
    private String receiverId;
    /**
     * The content of the entity.
     */
    private String content;
    /**
     * The timestamp of the entity.
     */
    private String timestamp;
    /**
     * The read of the entity.
     */
    private boolean read;
    /**
     * The replyToId of the entity.
     */
    private String replyToId;
    /**
     * The recalled of the entity.
     */
    private boolean recalled;
    /**
     * The edited of the entity.
     */
    private boolean edited;
    /**
     * The editedAt of the entity.
     */
    private String editedAt;
    private String messageType;   // text | image | file
    /**
     * The attachmentUrl of the entity.
     */
    private String attachmentUrl;
    /**
     * The attachmentName of the entity.
     */
    private String attachmentName;
    /**
     * The attachmentSize of the entity.
     */
    private String attachmentSize;

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
     * Gets the conversationid.
     * @return the conversationid
     */
    public String getConversationId() { return conversationId; }
    /**
     * Sets the conversationid.
     * @param conversationId the conversationid to set
     */
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    /**
     * Gets the senderid.
     * @return the senderid
     */
    public String getSenderId() { return senderId; }
    /**
     * Sets the senderid.
     * @param senderId the senderid to set
     */
    public void setSenderId(String senderId) { this.senderId = senderId; }
    /**
     * Gets the receiverid.
     * @return the receiverid
     */
    public String getReceiverId() { return receiverId; }
    /**
     * Sets the receiverid.
     * @param receiverId the receiverid to set
     */
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    /**
     * Gets the content.
     * @return the content
     */
    public String getContent() { return content; }
    /**
     * Sets the content.
     * @param content the content to set
     */
    public void setContent(String content) { this.content = content; }
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
     * Gets the read.
     * @return the read
     */
    public boolean isRead() { return read; }
    /**
     * Sets the read.
     * @param read the read to set
     */
    public void setRead(boolean read) { this.read = read; }
    /**
     * Gets the replytoid.
     * @return the replytoid
     */
    public String getReplyToId() { return replyToId; }
    /**
     * Sets the replytoid.
     * @param replyToId the replytoid to set
     */
    public void setReplyToId(String replyToId) { this.replyToId = replyToId; }
    /**
     * Gets the recalled.
     * @return the recalled
     */
    public boolean isRecalled() { return recalled; }
    /**
     * Sets the recalled.
     * @param recalled the recalled to set
     */
    public void setRecalled(boolean recalled) { this.recalled = recalled; }
    /**
     * Gets the edited.
     * @return the edited
     */
    public boolean isEdited() { return edited; }
    /**
     * Sets the edited.
     * @param edited the edited to set
     */
    public void setEdited(boolean edited) { this.edited = edited; }
    /**
     * Gets the editedat.
     * @return the editedat
     */
    public String getEditedAt() { return editedAt; }
    /**
     * Sets the editedat.
     * @param editedAt the editedat to set
     */
    public void setEditedAt(String editedAt) { this.editedAt = editedAt; }
    /**
     * Gets the messagetype.
     * @return the messagetype
     */
    public String getMessageType() { return messageType; }
    /**
     * Sets the messagetype.
     * @param messageType the messagetype to set
     */
    public void setMessageType(String messageType) { this.messageType = messageType; }
    /**
     * Gets the attachmenturl.
     * @return the attachmenturl
     */
    public String getAttachmentUrl() { return attachmentUrl; }
    /**
     * Sets the attachmenturl.
     * @param attachmentUrl the attachmenturl to set
     */
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    /**
     * Gets the attachmentname.
     * @return the attachmentname
     */
    public String getAttachmentName() { return attachmentName; }
    /**
     * Sets the attachmentname.
     * @param attachmentName the attachmentname to set
     */
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }
    /**
     * Gets the attachmentsize.
     * @return the attachmentsize
     */
    public String getAttachmentSize() { return attachmentSize; }
    /**
     * Sets the attachmentsize.
     * @param attachmentSize the attachmentsize to set
     */
    public void setAttachmentSize(String attachmentSize) { this.attachmentSize = attachmentSize; }
}
