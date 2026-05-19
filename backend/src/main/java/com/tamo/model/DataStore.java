package com.tamo.model;

import java.util.List;

public class DataStore {
    private List<User> users;
    private List<Profile> profiles;
    private List<Module> modules;
    private List<Job> jobs;
    private List<Application> applications;
    private List<Interview> interviews;
    private List<Notification> notifications;
    private List<Term> terms;
    private List<Department> departments;
    private List<LogEntry> logs;
    private List<WorkloadAssignment> workloadAssignments;
    private List<Message> messages;


    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { this.users = users; }
    public List<Profile> getProfiles() { return profiles; }
    public void setProfiles(List<Profile> profiles) { this.profiles = profiles; }
    public List<Module> getModules() { return modules; }
    public void setModules(List<Module> modules) { this.modules = modules; }
    public List<Job> getJobs() { return jobs; }
    public void setJobs(List<Job> jobs) { this.jobs = jobs; }
    public List<Application> getApplications() { return applications; }
    public void setApplications(List<Application> applications) { this.applications = applications; }
    public List<Interview> getInterviews() { return interviews; }
    public void setInterviews(List<Interview> interviews) { this.interviews = interviews; }
    public List<Notification> getNotifications() { return notifications; }
    public void setNotifications(List<Notification> notifications) { this.notifications = notifications; }
    public List<Term> getTerms() { return terms; }
    public void setTerms(List<Term> terms) { this.terms = terms; }
    public List<Department> getDepartments() { return departments; }
    public void setDepartments(List<Department> departments) { this.departments = departments; }
    public List<LogEntry> getLogs() { return logs; }
    public void setLogs(List<LogEntry> logs) { this.logs = logs; }
    public List<WorkloadAssignment> getWorkloadAssignments() { return workloadAssignments; }
    public void setWorkloadAssignments(List<WorkloadAssignment> workloadAssignments) { this.workloadAssignments = workloadAssignments; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

}
