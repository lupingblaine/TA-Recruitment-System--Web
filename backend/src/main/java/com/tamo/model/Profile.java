package com.tamo.model;

import java.util.List;

/**
 * Represents a Profile entity in the system.
 */
public class Profile {
    /**
     * The id of the entity.
     */
    private String id;
    /**
     * The userId of the entity.
     */
    private String userId;
    /**
     * The studentId of the entity.
     */
    private String studentId;
    /**
     * The degree of the entity.
     */
    private String degree;
    /**
     * The year of the entity.
     */
    private int year;
    /**
     * The gpa of the entity.
     */
    private double gpa;
    /**
     * The maxGpa of the entity.
     */
    private double maxGpa;
    /**
     * The profileCompletion of the entity.
     */
    private int profileCompletion;
    /**
     * The skills of the entity.
     */
    private List<String> skills;
    /**
     * The coursework of the entity.
     */
    private List<Coursework> coursework;
    /**
     * The experience of the entity.
     */
    private List<Experience> experience;
    /**
     * The documents of the entity.
     */
    private List<Document> documents;
    /**
     * The maxWeeklyHours of the entity.
     */
    private int maxWeeklyHours;
    /**
     * The currentWeeklyHours of the entity.
     */
    private int currentWeeklyHours;

    public static class Coursework {
        /**
         * The code of the entity.
         */
        private String code;
        /**
         * The name of the entity.
         */
        private String name;
        /**
         * The grade of the entity.
         */
        private String grade;
        /**
         * The term of the entity.
         */
        private String term;

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
         * Gets the grade.
         * @return the grade
         */
        public String getGrade() { return grade; }
        /**
         * Sets the grade.
         * @param grade the grade to set
         */
        public void setGrade(String grade) { this.grade = grade; }
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
    }

    public static class Experience {
        /**
         * The role of the entity.
         */
        private String role;
        /**
         * The course of the entity.
         */
        private String course;
        /**
         * The company of the entity.
         */
        private String company;
        /**
         * The term of the entity.
         */
        private String term;
        /**
         * The description of the entity.
         */
        private String description;

        /**
         * Gets the role.
         * @return the role
         */
        public String getRole() { return role; }
        /**
         * Sets the role.
         * @param role the role to set
         */
        public void setRole(String role) { this.role = role; }
        /**
         * Gets the course.
         * @return the course
         */
        public String getCourse() { return course; }
        /**
         * Sets the course.
         * @param course the course to set
         */
        public void setCourse(String course) { this.course = course; }
        /**
         * Gets the company.
         * @return the company
         */
        public String getCompany() { return company; }
        /**
         * Sets the company.
         * @param company the company to set
         */
        public void setCompany(String company) { this.company = company; }
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
         * Gets the description.
         * @return the description
         */
        public String getDescription() { return description; }
        /**
         * Sets the description.
         * @param description the description to set
         */
        public void setDescription(String description) { this.description = description; }
    }

    public static class Document {
        /**
         * The id of the entity.
         */
        private String id;
        /**
         * The name of the entity.
         */
        private String name;
        private String type;   // cv | transcript
        /**
         * The size of the entity.
         */
        private String size;
        /**
         * The uploadedAt of the entity.
         */
        private String uploadedAt;

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
         * Gets the size.
         * @return the size
         */
        public String getSize() { return size; }
        /**
         * Sets the size.
         * @param size the size to set
         */
        public void setSize(String size) { this.size = size; }
        /**
         * Gets the uploadedat.
         * @return the uploadedat
         */
        public String getUploadedAt() { return uploadedAt; }
        /**
         * Sets the uploadedat.
         * @param uploadedAt the uploadedat to set
         */
        public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }
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
     * Gets the studentid.
     * @return the studentid
     */
    public String getStudentId() { return studentId; }
    /**
     * Sets the studentid.
     * @param studentId the studentid to set
     */
    public void setStudentId(String studentId) { this.studentId = studentId; }
    /**
     * Gets the degree.
     * @return the degree
     */
    public String getDegree() { return degree; }
    /**
     * Sets the degree.
     * @param degree the degree to set
     */
    public void setDegree(String degree) { this.degree = degree; }
    /**
     * Gets the year.
     * @return the year
     */
    public int getYear() { return year; }
    /**
     * Sets the year.
     * @param year the year to set
     */
    public void setYear(int year) { this.year = year; }
    /**
     * Gets the gpa.
     * @return the gpa
     */
    public double getGpa() { return gpa; }
    /**
     * Sets the gpa.
     * @param gpa the gpa to set
     */
    public void setGpa(double gpa) { this.gpa = gpa; }
    /**
     * Gets the maxgpa.
     * @return the maxgpa
     */
    public double getMaxGpa() { return maxGpa; }
    /**
     * Sets the maxgpa.
     * @param maxGpa the maxgpa to set
     */
    public void setMaxGpa(double maxGpa) { this.maxGpa = maxGpa; }
    /**
     * Gets the profilecompletion.
     * @return the profilecompletion
     */
    public int getProfileCompletion() { return profileCompletion; }
    /**
     * Sets the profilecompletion.
     * @param profileCompletion the profilecompletion to set
     */
    public void setProfileCompletion(int profileCompletion) { this.profileCompletion = profileCompletion; }
    /**
     * Gets the skills.
     * @return the skills
     */
    public List<String> getSkills() { return skills; }
    /**
     * Sets the skills.
     * @param skills the skills to set
     */
    public void setSkills(List<String> skills) { this.skills = skills; }
    /**
     * Gets the coursework.
     * @return the coursework
     */
    public List<Coursework> getCoursework() { return coursework; }
    /**
     * Sets the coursework.
     * @param coursework the coursework to set
     */
    public void setCoursework(List<Coursework> coursework) { this.coursework = coursework; }
    /**
     * Gets the experience.
     * @return the experience
     */
    public List<Experience> getExperience() { return experience; }
    /**
     * Sets the experience.
     * @param experience the experience to set
     */
    public void setExperience(List<Experience> experience) { this.experience = experience; }
    /**
     * Gets the documents.
     * @return the documents
     */
    public List<Document> getDocuments() { return documents; }
    /**
     * Sets the documents.
     * @param documents the documents to set
     */
    public void setDocuments(List<Document> documents) { this.documents = documents; }
    /**
     * Gets the maxweeklyhours.
     * @return the maxweeklyhours
     */
    public int getMaxWeeklyHours() { return maxWeeklyHours; }
    /**
     * Sets the maxweeklyhours.
     * @param maxWeeklyHours the maxweeklyhours to set
     */
    public void setMaxWeeklyHours(int maxWeeklyHours) { this.maxWeeklyHours = maxWeeklyHours; }
    /**
     * Gets the currentweeklyhours.
     * @return the currentweeklyhours
     */
    public int getCurrentWeeklyHours() { return currentWeeklyHours; }
    /**
     * Sets the currentweeklyhours.
     * @param currentWeeklyHours the currentweeklyhours to set
     */
    public void setCurrentWeeklyHours(int currentWeeklyHours) { this.currentWeeklyHours = currentWeeklyHours; }
}
