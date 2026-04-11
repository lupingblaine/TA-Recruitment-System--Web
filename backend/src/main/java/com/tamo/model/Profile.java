package com.tamo.model;

import java.util.List;

public class Profile {
    private String id;
    private String userId;
    private String studentId;
    private String degree;
    private int year;
    private double gpa;
    private double maxGpa;
    private int profileCompletion;
    private List<String> skills;
    private List<Coursework> coursework;
    private List<Experience> experience;
    private List<Document> documents;
    private int maxWeeklyHours;
    private int currentWeeklyHours;

    public static class Coursework {
        private String code;
        private String name;
        private String grade;
        private String term;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
        public String getTerm() { return term; }
        public void setTerm(String term) { this.term = term; }
    }

    public static class Experience {
        private String role;
        private String course;
        private String company;
        private String term;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getCourse() { return course; }
        public void setCourse(String course) { this.course = course; }
        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }
        public String getTerm() { return term; }
        public void setTerm(String term) { this.term = term; }
    }

    public static class Document {
        private String id;
        private String name;
        private String type;   // cv | transcript
        private String size;
        private String uploadedAt;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }
        public String getUploadedAt() { return uploadedAt; }
        public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public double getGpa() { return gpa; }
    public void setGpa(double gpa) { this.gpa = gpa; }
    public double getMaxGpa() { return maxGpa; }
    public void setMaxGpa(double maxGpa) { this.maxGpa = maxGpa; }
    public int getProfileCompletion() { return profileCompletion; }
    public void setProfileCompletion(int profileCompletion) { this.profileCompletion = profileCompletion; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    public List<Coursework> getCoursework() { return coursework; }
    public void setCoursework(List<Coursework> coursework) { this.coursework = coursework; }
    public List<Experience> getExperience() { return experience; }
    public void setExperience(List<Experience> experience) { this.experience = experience; }
    public List<Document> getDocuments() { return documents; }
    public void setDocuments(List<Document> documents) { this.documents = documents; }
    public int getMaxWeeklyHours() { return maxWeeklyHours; }
    public void setMaxWeeklyHours(int maxWeeklyHours) { this.maxWeeklyHours = maxWeeklyHours; }
    public int getCurrentWeeklyHours() { return currentWeeklyHours; }
    public void setCurrentWeeklyHours(int currentWeeklyHours) { this.currentWeeklyHours = currentWeeklyHours; }
}
