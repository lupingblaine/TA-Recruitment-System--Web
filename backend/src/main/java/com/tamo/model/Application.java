package com.tamo.model;

import java.util.List;

/**
 * Represents a Application entity in the system.
 */
public class Application {
    /**
     * The id of the entity.
     */
    private String id;
    /**
     * The jobId of the entity.
     */
    private String jobId;
    /**
     * The applicantId of the entity.
     */
    private String applicantId;
    private String status;       // applied | review | shortlisted | interview | offer | rejected
    /**
     * The coverLetter of the entity.
     */
    private String coverLetter;
    /**
     * The documentIds of the entity.
     */
    private List<String> documentIds;
    /**
     * The matchScore of the entity.
     */
    private int matchScore;
    private String riskFlag;     // null | overload | missing_req
    /**
     * The aiSummary of the entity.
     */
    private String aiSummary;
    /**
     * The appliedAt of the entity.
     */
    private String appliedAt;
    /**
     * The updatedAt of the entity.
     */
    private String updatedAt;
    /**
     * The timeline of the entity.
     */
    private List<TimelineEntry> timeline;

    public static class TimelineEntry {
        /**
         * The status of the entity.
         */
        private String status;
        /**
         * The date of the entity.
         */
        private String date;
        /**
         * The completed of the entity.
         */
        private boolean completed;
        /**
         * The pending of the entity.
         */
        private Boolean pending;
        /**
         * The error of the entity.
         */
        private Boolean error;

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
         * Gets the completed.
         * @return the completed
         */
        public boolean isCompleted() { return completed; }
        /**
         * Sets the completed.
         * @param completed the completed to set
         */
        public void setCompleted(boolean completed) { this.completed = completed; }
        /**
         * Gets the pending.
         * @return the pending
         */
        public Boolean getPending() { return pending; }
        /**
         * Sets the pending.
         * @param pending the pending to set
         */
        public void setPending(Boolean pending) { this.pending = pending; }
        /**
         * Gets the error.
         * @return the error
         */
        public Boolean getError() { return error; }
        /**
         * Sets the error.
         * @param error the error to set
         */
        public void setError(Boolean error) { this.error = error; }
    }

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
     * Gets the jobid.
     * @return the jobid
     */
    public String getJobId() { return jobId; }
    /**
     * Sets the jobid.
     * @param jobId the jobid to set
     */
    public void setJobId(String jobId) { this.jobId = jobId; }
    /**
     * Gets the applicantid.
     * @return the applicantid
     */
    public String getApplicantId() { return applicantId; }
    /**
     * Sets the applicantid.
     * @param applicantId the applicantid to set
     */
    public void setApplicantId(String applicantId) { this.applicantId = applicantId; }
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
     * Gets the coverletter.
     * @return the coverletter
     */
    public String getCoverLetter() { return coverLetter; }
    /**
     * Sets the coverletter.
     * @param coverLetter the coverletter to set
     */
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
    /**
     * Gets the documentids.
     * @return the documentids
     */
    public List<String> getDocumentIds() { return documentIds; }
    /**
     * Sets the documentids.
     * @param documentIds the documentids to set
     */
    public void setDocumentIds(List<String> documentIds) { this.documentIds = documentIds; }
    /**
     * Gets the matchscore.
     * @return the matchscore
     */
    public int getMatchScore() { return matchScore; }
    /**
     * Sets the matchscore.
     * @param matchScore the matchscore to set
     */
    public void setMatchScore(int matchScore) { this.matchScore = matchScore; }
    /**
     * Gets the riskflag.
     * @return the riskflag
     */
    public String getRiskFlag() { return riskFlag; }
    /**
     * Sets the riskflag.
     * @param riskFlag the riskflag to set
     */
    public void setRiskFlag(String riskFlag) { this.riskFlag = riskFlag; }
    /**
     * Gets the aisummary.
     * @return the aisummary
     */
    public String getAiSummary() { return aiSummary; }
    /**
     * Sets the aisummary.
     * @param aiSummary the aisummary to set
     */
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
    /**
     * Gets the appliedat.
     * @return the appliedat
     */
    public String getAppliedAt() { return appliedAt; }
    /**
     * Sets the appliedat.
     * @param appliedAt the appliedat to set
     */
    public void setAppliedAt(String appliedAt) { this.appliedAt = appliedAt; }
    /**
     * Gets the updatedat.
     * @return the updatedat
     */
    public String getUpdatedAt() { return updatedAt; }
    /**
     * Sets the updatedat.
     * @param updatedAt the updatedat to set
     */
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    /**
     * Gets the timeline.
     * @return the timeline
     */
    public List<TimelineEntry> getTimeline() { return timeline; }
    /**
     * Sets the timeline.
     * @param timeline the timeline to set
     */
    public void setTimeline(List<TimelineEntry> timeline) { this.timeline = timeline; }
}
