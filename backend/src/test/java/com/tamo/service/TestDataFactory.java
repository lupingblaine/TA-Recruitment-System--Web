package com.tamo.service;

import com.tamo.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared test data builder used across all service tests.
 * Provides a pre-populated DataStore that mirrors realistic application state.
 */
public final class TestDataFactory {

    private TestDataFactory() {}

    public static DataStore createTestDataStore() {
        DataStore ds = new DataStore();
        ApplicationPeriod period = new ApplicationPeriod();
        period.setId("period_test"); period.setName("Test application period"); period.setTermId("t001");
        period.setOpensAt("2000-01-01T00:00:00Z"); period.setClosesAt("2100-01-01T00:00:00Z");
        period.setStatus("open"); period.setCreatedAt("2000-01-01T00:00:00Z"); period.setUpdatedAt("2000-01-01T00:00:00Z");
        ds.setApplicationPeriods(new ArrayList<>(List.of(period)));
        ds.setUsers(createUsers());
        ds.setProfiles(createProfiles());
        for(Profile p:ds.getProfiles())p.setQualifications(com.google.gson.JsonParser.parseString("{\"studentLevel\":\"masters\",\"uciStudentId\":\"12345678\",\"uciEmail\":\"synthetic@uci.edu\",\"program\":\"Synthetic program\",\"skills\":[\"Python\"],\"interests\":[\"Biology\"],\"trainingStatus\":\"completed\",\"acknowledged\":true}").getAsJsonObject());
        for(Profile p:ds.getProfiles()){var q=p.getQualifications();q.addProperty("firstName","Synthetic");q.addProperty("lastName",p.getUserId());q.addProperty("facultyPI","Synthetic PI");q.addProperty("graduateStartYear",2024);q.addProperty("taQuarters",1);q.addProperty("advancedToCandidacy","no");q.addProperty("sexualHarassmentTrainingCompleted",true);q.addProperty("additionalEmployment","None");q.addProperty("documentedAccommodation",false);q.addProperty("ackSchedule",true);q.addProperty("ackEnrollment",true);}
        List<WeeklyAvailability> schedules = new ArrayList<>();
        ds.getUsers().stream().filter(u -> "applicant".equals(u.getRole())).forEach(u -> {
            WeeklyAvailability a = new WeeklyAvailability(); a.setId("availability_" + u.getId());
            a.setApplicantId(u.getId()); a.setPeriodId("period_test"); a.setTimeZone("America/Los_Angeles");
            WeeklyAvailability.Slot slot = new WeeklyAvailability.Slot(); slot.setDayOfWeek(1); slot.setStartTime("09:00"); slot.setEndTime("17:00"); slot.setReason("");
            a.setAvailableSlots(new ArrayList<>(List.of(slot))); a.setConflictSlots(new ArrayList<>());
            a.setNotes(""); a.setUpdatedAt("2026-10-16T19:00:00Z"); schedules.add(a);
        });
        ds.setWeeklyAvailabilities(schedules);
        ds.setModules(createModules());
        ds.setJobs(createJobs());
        ds.setApplications(createApplications());
        ds.setInterviews(createInterviews());
        ds.setNotifications(createNotifications());
        ds.setTerms(createTerms());
        ds.setDepartments(createDepartments());
        ds.setLogs(new ArrayList<>());
        ds.setWorkloadAssignments(new ArrayList<>());
        ds.setMessages(createMessages());
        return ds;
    }

    public static List<User> createUsers() {
        List<User> users = new ArrayList<>();

        User u1 = new User();
        u1.setId("u001"); u1.setName("Alex Johnson"); u1.setEmail("alex@university.edu");
        u1.setPassword("pass1"); u1.setRole("applicant"); u1.setStatus("active");
        u1.setDepartment("CS"); u1.setCreatedAt("2026-01-01T00:00:00");
        users.add(u1);

        User u2 = new User();
        u2.setId("u002"); u2.setName("Bob Smith"); u2.setEmail("bob@university.edu");
        u2.setPassword("pass2"); u2.setRole("applicant"); u2.setStatus("active");
        u2.setDepartment("EE"); u2.setCreatedAt("2026-01-02T00:00:00");
        users.add(u2);

        User u3 = new User();
        u3.setId("u003"); u3.setName("Carol Williams"); u3.setEmail("carol@university.edu");
        u3.setPassword("pass3"); u3.setRole("applicant"); u3.setStatus("suspended");
        u3.setDepartment("CS"); u3.setCreatedAt("2026-01-03T00:00:00");
        users.add(u3);

        User u5 = new User();
        u5.setId("u005"); u5.setName("Alan Turing"); u5.setEmail("alan@university.edu");
        u5.setPassword("pass5"); u5.setRole("organiser"); u5.setStatus("active");
        u5.setDepartment("CS"); u5.setCreatedAt("2026-01-05T00:00:00");
        users.add(u5);

        User u6 = new User();
        u6.setId("u006"); u6.setName("Grace Hopper"); u6.setEmail("grace@university.edu");
        u6.setPassword("pass6"); u6.setRole("organiser"); u6.setStatus("active");
        u6.setDepartment("EE"); u6.setCreatedAt("2026-01-06T00:00:00");
        users.add(u6);

        User admin = new User();
        admin.setId("u010"); admin.setName("System Admin"); admin.setEmail("admin@university.edu");
        admin.setPassword("admin"); admin.setRole("admin"); admin.setStatus("active");
        admin.setDepartment("Admin"); admin.setCreatedAt("2026-01-10T00:00:00");
        users.add(admin);

        return users;
    }

    public static List<Profile> createProfiles() {
        List<Profile> profiles = new ArrayList<>();

        Profile p1 = new Profile();
        p1.setId("p001"); p1.setUserId("u001"); p1.setStudentId("2023001");
        p1.setDegree("BSc CS"); p1.setYear(3); p1.setGpa(3.8); p1.setMaxGpa(4.0);
        p1.setProfileCompletion(80); p1.setMaxWeeklyHours(20); p1.setCurrentWeeklyHours(0);
        p1.setSkills(new ArrayList<>(List.of("Java", "Python", "SQL")));
        p1.setCoursework(new ArrayList<>());
        p1.setExperience(new ArrayList<>());
        p1.setDocuments(new ArrayList<>());
        Profile.Document doc = new Profile.Document();
        doc.setId("doc001"); doc.setName("resume.pdf"); doc.setType("application/pdf");
        doc.setSize("100KB"); doc.setUploadedAt("2026-03-01T10:00:00");
        p1.getDocuments().add(doc);
        profiles.add(p1);

        Profile p2 = new Profile();
        p2.setId("p002"); p2.setUserId("u002"); p2.setStudentId("2023002");
        p2.setDegree("BSc EE"); p2.setYear(2); p2.setGpa(3.5); p2.setMaxGpa(4.0);
        p2.setProfileCompletion(60); p2.setMaxWeeklyHours(15); p2.setCurrentWeeklyHours(0);
        p2.setSkills(new ArrayList<>(List.of("C++", "VHDL")));
        p2.setCoursework(new ArrayList<>());
        p2.setExperience(new ArrayList<>());
        p2.setDocuments(new ArrayList<>());
        profiles.add(p2);

        return profiles;
    }

    public static List<com.tamo.model.Module> createModules() {
        List<com.tamo.model.Module> modules = new ArrayList<>();

        com.tamo.model.Module m1 = new com.tamo.model.Module();
        m1.setId("m001"); m1.setCode("EBU6304"); m1.setName("Software Engineering");
        m1.setDepartment("Computer Science"); m1.setOrganiserId("u005");
        m1.setOrganiserName("Alan Turing"); m1.setTerm("2025-2026 Spring"); m1.setStatus("active");
        modules.add(m1);

        com.tamo.model.Module m2 = new com.tamo.model.Module();
        m2.setId("m002"); m2.setCode("EBU5476"); m2.setName("Digital Circuits");
        m2.setDepartment("Electronic Engineering"); m2.setOrganiserId("u006");
        m2.setOrganiserName("Grace Hopper"); m2.setTerm("2025-2026 Spring"); m2.setStatus("active");
        modules.add(m2);

        return modules;
    }

    public static List<Job> createJobs() {
        List<Job> jobs = new ArrayList<>();

        Job j1 = new Job();
        j1.setId("j001"); j1.setOrganiserId("u005"); j1.setModuleId("m001");
        j1.setModuleCode("EBU6304"); j1.setTitle("TA for Software Engineering");
        j1.setDescription("Help with labs"); j1.setDepartment("CS"); j1.setType("Part-time");
        j1.setHoursPerWeek(10); j1.setPositions(2); j1.setDeadline("2026-12-01");
        j1.setStatus("active"); j1.setTags(new ArrayList<>()); j1.setRequirements(new ArrayList<>());
        j1.setResponsibilities(new ArrayList<>()); j1.setCreatedAt("2026-03-01T09:00:00");
        jobs.add(j1);

        Job j2 = new Job();
        j2.setId("j002"); j2.setOrganiserId("u005"); j2.setModuleId("m001");
        j2.setModuleCode("EBU6304"); j2.setTitle("Lab Assistant");
        j2.setDescription("Support lab sessions"); j2.setDepartment("CS"); j2.setType("Part-time");
        j2.setHoursPerWeek(8); j2.setPositions(1); j2.setDeadline("2026-11-01");
        j2.setStatus("closed"); j2.setTags(new ArrayList<>()); j2.setRequirements(new ArrayList<>());
        j2.setResponsibilities(new ArrayList<>()); j2.setCreatedAt("2026-02-15T09:00:00");
        jobs.add(j2);

        Job j3 = new Job();
        j3.setId("j003"); j3.setOrganiserId("u006"); j3.setModuleId("m002");
        j3.setModuleCode("EBU5476"); j3.setTitle("TA for Digital Circuits");
        j3.setDescription("Help with hardware labs"); j3.setDepartment("EE"); j3.setType("Part-time");
        j3.setHoursPerWeek(12); j3.setPositions(1); j3.setDeadline("2026-12-15");
        j3.setStatus("active"); j3.setTags(new ArrayList<>()); j3.setRequirements(new ArrayList<>());
        j3.setResponsibilities(new ArrayList<>()); j3.setCreatedAt("2026-03-05T09:00:00");
        jobs.add(j3);

        return jobs;
    }

    public static List<Application> createApplications() {
        List<Application> apps = new ArrayList<>();

        Application a1 = new Application();
        a1.setId("a001"); a1.setJobId("j001"); a1.setApplicantId("u001");
        a1.setStatus("applied"); a1.setCoverLetter("I love SE");
        a1.setDocumentIds(List.of("doc001")); a1.setMatchScore(88);
        a1.setAiSummary("Strong candidate"); a1.setAppliedAt("2026-04-01T10:00:00");
        a1.setUpdatedAt("2026-04-01T10:00:00");
        List<Application.TimelineEntry> tl = new ArrayList<>();
        Application.TimelineEntry t1 = new Application.TimelineEntry();
        t1.setStatus("applied"); t1.setDate("2026-04-01"); t1.setCompleted(true);
        tl.add(t1);
        for (String s : new String[]{"review", "shortlisted", "interview", "offer"}) {
            Application.TimelineEntry t = new Application.TimelineEntry();
            t.setStatus(s); t.setDate(null); t.setCompleted(false);
            tl.add(t);
        }
        a1.setTimeline(tl);
        apps.add(a1);

        Application a2 = new Application();
        a2.setId("a002"); a2.setJobId("j003"); a2.setApplicantId("u001");
        a2.setStatus("offer"); a2.setCoverLetter("Good at circuits");
        a2.setDocumentIds(List.of()); a2.setMatchScore(75);
        a2.setAiSummary("OK"); a2.setAppliedAt("2026-04-05T10:00:00");
        a2.setUpdatedAt("2026-04-15T10:00:00");
        a2.setTimeline(new ArrayList<>());
        apps.add(a2);

        Application a3 = new Application();
        a3.setId("a003"); a3.setJobId("j001"); a3.setApplicantId("u002");
        a3.setStatus("interview"); a3.setCoverLetter("Interested in SE");
        a3.setDocumentIds(List.of()); a3.setMatchScore(82);
        a3.setAiSummary("Good fit"); a3.setAppliedAt("2026-04-02T10:00:00");
        a3.setUpdatedAt("2026-04-10T10:00:00");
        a3.setTimeline(new ArrayList<>());
        apps.add(a3);

        apps.forEach(a -> a.setPeriodId("period_test"));
        return apps;
    }

    public static List<Interview> createInterviews() {
        List<Interview> interviews = new ArrayList<>();

        Interview iv1 = new Interview();
        iv1.setId("i001"); iv1.setApplicationId("a003"); iv1.setOrganiserId("u005");
        iv1.setCandidateId("u002"); iv1.setCandidateName("Bob Smith");
        iv1.setModuleCode("EBU6304"); iv1.setModuleName("Software Engineering");
        iv1.setDate("2026-05-20"); iv1.setStartTime("10:00"); iv1.setEndTime("10:30");
        iv1.setType("video"); iv1.setLocation("Zoom"); iv1.setStatus("scheduled");
        interviews.add(iv1);

        return interviews;
    }

    public static List<Notification> createNotifications() {
        List<Notification> notifications = new ArrayList<>();

        Notification n1 = new Notification();
        n1.setId("n001"); n1.setUserId("u001"); n1.setType("status_change");
        n1.setTitle("Application Updated"); n1.setMessage("Your application status changed");
        n1.setRelatedId("a001"); n1.setCreatedAt("2026-04-10T10:00:00"); n1.setRead(false);
        notifications.add(n1);

        Notification n2 = new Notification();
        n2.setId("n002"); n2.setUserId("u001"); n2.setType("offer");
        n2.setTitle("Offer Received"); n2.setMessage("You got an offer");
        n2.setRelatedId("a002"); n2.setCreatedAt("2026-04-15T10:00:00"); n2.setRead(true);
        notifications.add(n2);

        return notifications;
    }

    public static List<Term> createTerms() {
        List<Term> terms = new ArrayList<>();
        Term t = new Term();
        t.setId("t001"); t.setName("2025-2026 Spring");
        t.setStartDate("2026-02-01"); t.setEndDate("2026-06-30"); t.setStatus("active");
        terms.add(t);
        return terms;
    }

    public static List<Department> createDepartments() {
        List<Department> depts = new ArrayList<>();
        Department d1 = new Department();
        d1.setId("d001"); d1.setName("Computer Science"); d1.setModuleCount(3); d1.setStatus("active");
        depts.add(d1);
        Department d2 = new Department();
        d2.setId("d002"); d2.setName("Electronic Engineering"); d2.setModuleCount(2); d2.setStatus("active");
        depts.add(d2);
        return depts;
    }

    public static List<Message> createMessages() {
        List<Message> msgs = new ArrayList<>();

        Message msg1 = new Message();
        msg1.setId("msg001"); msg1.setConversationId("u001_u005");
        msg1.setSenderId("u001"); msg1.setReceiverId("u005");
        msg1.setContent("Hello professor!"); msg1.setTimestamp("2026-05-01T10:00:00");
        msg1.setRead(true); msg1.setRecalled(false); msg1.setEdited(false);
        msg1.setMessageType("text");
        msgs.add(msg1);

        Message msg2 = new Message();
        msg2.setId("msg002"); msg2.setConversationId("u001_u005");
        msg2.setSenderId("u005"); msg2.setReceiverId("u001");
        msg2.setContent("Hi Alex, how can I help?"); msg2.setTimestamp("2026-05-01T10:05:00");
        msg2.setRead(false); msg2.setRecalled(false); msg2.setEdited(false);
        msg2.setMessageType("text");
        msgs.add(msg2);

        return msgs;
    }
}
