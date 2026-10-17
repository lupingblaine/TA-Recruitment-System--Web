package com.tamo.model;
public class ReviewRecord{
 private String sectionId;public String getSectionId(){return sectionId;}public void setSectionId(String value){sectionId=value;}
 private com.google.gson.JsonElement sourceMetadata;public com.google.gson.JsonElement getSourceMetadata(){return sourceMetadata;}public void setSourceMetadata(com.google.gson.JsonElement value){sourceMetadata=value;}
 private String id;public String getId(){return id;}public void setId(String value){id=value;}
 private String applicationId;public String getApplicationId(){return applicationId;}public void setApplicationId(String value){applicationId=value;}
 private String actorId;public String getActorId(){return actorId;}public void setActorId(String value){actorId=value;}
 private String action;public String getAction(){return action;}public void setAction(String value){action=value;}
 private String note;public String getNote(){return note;}public void setNote(String value){note=value;}
 private String createdAt;public String getCreatedAt(){return createdAt;}public void setCreatedAt(String value){createdAt=value;}
 private com.google.gson.JsonElement criteriaSnapshot;public com.google.gson.JsonElement getCriteriaSnapshot(){return criteriaSnapshot;}public void setCriteriaSnapshot(com.google.gson.JsonElement value){criteriaSnapshot=value;}
 private com.google.gson.JsonElement scheduleSnapshot;public com.google.gson.JsonElement getScheduleSnapshot(){return scheduleSnapshot;}public void setScheduleSnapshot(com.google.gson.JsonElement value){scheduleSnapshot=value;}
}
