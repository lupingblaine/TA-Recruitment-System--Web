package com.tamo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Model Layer — POJO Tests")
class ModelTest {

    @Nested
    @DisplayName("User")
    class UserTests {
        @Test
        void shouldSetAndGetAllFields() {
            User user = new User();
            user.setId("u001");
            user.setName("Alice");
            user.setEmail("alice@university.edu");
            user.setPassword("secret");
            user.setRole("applicant");
            user.setStatus("active");
            user.setDepartment("CS");
            user.setPhone("123456");
            user.setAvatar("A");
            user.setCreatedAt("2026-01-01T00:00:00");
            user.setLastLogin("2026-05-01T10:00:00");

            assertEquals("u001", user.getId());
            assertEquals("Alice", user.getName());
            assertEquals("alice@university.edu", user.getEmail());
            assertEquals("secret", user.getPassword());
            assertEquals("applicant", user.getRole());
            assertEquals("active", user.getStatus());
            assertEquals("CS", user.getDepartment());
            assertEquals("123456", user.getPhone());
            assertEquals("A", user.getAvatar());
            assertEquals("2026-01-01T00:00:00", user.getCreatedAt());
            assertEquals("2026-05-01T10:00:00", user.getLastLogin());
        }

        @Test
        void defaultFieldsShouldBeNull() {
            User user = new User();
            assertNull(user.getId());
            assertNull(user.getName());
            assertNull(user.getEmail());
        }
    }

    @Nested
    @DisplayName("Job")
    class JobTests {
        @Test
        void shouldSetAndGetAllFields() {
            Job job = new Job();
            job.setId("j001");
            job.setOrganiserId("u005");
            job.setModuleId("m001");
            job.setModuleCode("EBU6304");
            job.setTitle("TA for SE");
            job.setDescription("Assist with labs");
            job.setProfessor("Prof. Smith");
            job.setDepartment("CS");
            job.setType("Part-time");
            job.setHoursPerWeek(10);
            job.setPositions(2);
            job.setDeadline("2026-06-01");
            job.setStatus("active");
            job.setCreatedAt("2026-01-15T09:00:00");

            List<String> tags = List.of("java", "testing");
            job.setTags(new ArrayList<>(tags));
            List<String> reqs = List.of("GPA > 3.0");
            job.setRequirements(new ArrayList<>(reqs));
            List<String> resps = List.of("Grade assignments");
            job.setResponsibilities(new ArrayList<>(resps));
            List<String> dates = List.of("2026-03-01", "2026-03-08");
            job.setWorkDates(new ArrayList<>(dates));

            assertEquals("j001", job.getId());
            assertEquals("u005", job.getOrganiserId());
            assertEquals("m001", job.getModuleId());
            assertEquals("EBU6304", job.getModuleCode());
            assertEquals("TA for SE", job.getTitle());
            assertEquals("Assist with labs", job.getDescription());
            assertEquals("Prof. Smith", job.getProfessor());
            assertEquals("CS", job.getDepartment());
            assertEquals("Part-time", job.getType());
            assertEquals(10, job.getHoursPerWeek());
            assertEquals(2, job.getPositions());
            assertEquals("2026-06-01", job.getDeadline());
            assertEquals("active", job.getStatus());
            assertEquals(2, job.getTags().size());
            assertEquals(1, job.getRequirements().size());
            assertEquals(1, job.getResponsibilities().size());
            assertEquals(2, job.getWorkDates().size());
        }
    }

    @Nested
    @DisplayName("Application")
    class ApplicationTests {
        @Test
        void shouldSetAndGetAllFields() {
            Application app = new Application();
            app.setId("a001");
            app.setJobId("j001");
            app.setApplicantId("u001");
            app.setStatus("applied");
            app.setCoverLetter("I am interested");
            app.setDocumentIds(List.of("doc001"));
            app.setMatchScore(85);
            app.setRiskFlag(null);
            app.setAiSummary("Good candidate");
            app.setAppliedAt("2026-04-01T10:00:00");
            app.setUpdatedAt("2026-04-01T10:00:00");

            assertEquals("a001", app.getId());
            assertEquals("j001", app.getJobId());
            assertEquals("u001", app.getApplicantId());
            assertEquals("applied", app.getStatus());
            assertEquals("I am interested", app.getCoverLetter());
            assertEquals(1, app.getDocumentIds().size());
            assertEquals(85, app.getMatchScore());
            assertNull(app.getRiskFlag());
            assertEquals("Good candidate", app.getAiSummary());
        }

        @Test
        void timelineEntryShouldWork() {
            Application.TimelineEntry entry = new Application.TimelineEntry();
            entry.setStatus("applied");
            entry.setDate("2026-04-01");
            entry.setCompleted(true);
            entry.setPending(null);
            entry.setError(null);

            assertEquals("applied", entry.getStatus());
            assertEquals("2026-04-01", entry.getDate());
            assertTrue(entry.isCompleted());
            assertNull(entry.getPending());
            assertNull(entry.getError());
        }
    }

    @Nested
    @DisplayName("Profile")
    class ProfileTests {
        @Test
        void shouldSetAndGetAllFields() {
            Profile profile = new Profile();
            profile.setId("p001");
            profile.setUserId("u001");
            profile.setStudentId("2023001");
            profile.setDegree("BSc Computer Science");
            profile.setYear(3);
            profile.setGpa(3.8);
            profile.setMaxGpa(4.0);
            profile.setProfileCompletion(75);
            profile.setMaxWeeklyHours(20);
            profile.setCurrentWeeklyHours(8);
            profile.setSkills(List.of("Java", "Python"));
            profile.setCoursework(new ArrayList<>());
            profile.setExperience(new ArrayList<>());
            profile.setDocuments(new ArrayList<>());

            assertEquals("p001", profile.getId());
            assertEquals("u001", profile.getUserId());
            assertEquals("2023001", profile.getStudentId());
            assertEquals("BSc Computer Science", profile.getDegree());
            assertEquals(3, profile.getYear());
            assertEquals(3.8, profile.getGpa(), 0.001);
            assertEquals(4.0, profile.getMaxGpa(), 0.001);
            assertEquals(75, profile.getProfileCompletion());
            assertEquals(20, profile.getMaxWeeklyHours());
            assertEquals(8, profile.getCurrentWeeklyHours());
            assertEquals(2, profile.getSkills().size());
        }

        @Test
        void documentShouldWork() {
            Profile.Document doc = new Profile.Document();
            doc.setId("doc001");
            doc.setName("resume.pdf");
            doc.setType("application/pdf");
            doc.setSize("120KB");
            doc.setUploadedAt("2026-04-01T10:00:00");

            assertEquals("doc001", doc.getId());
            assertEquals("resume.pdf", doc.getName());
            assertEquals("application/pdf", doc.getType());
            assertEquals("120KB", doc.getSize());
        }
    }

    @Nested
    @DisplayName("Interview")
    class InterviewTests {
        @Test
        void shouldSetAndGetAllFields() {
            Interview iv = new Interview();
            iv.setId("i001");
            iv.setApplicationId("a001");
            iv.setOrganiserId("u005");
            iv.setCandidateId("u001");
            iv.setCandidateName("Alice");
            iv.setModuleCode("EBU6304");
            iv.setModuleName("Software Engineering");
            iv.setDate("2026-05-15");
            iv.setStartTime("10:00");
            iv.setEndTime("10:30");
            iv.setType("video");
            iv.setLocation("Zoom");
            iv.setStatus("scheduled");
            iv.setOutcome(null);

            assertEquals("i001", iv.getId());
            assertEquals("a001", iv.getApplicationId());
            assertEquals("u005", iv.getOrganiserId());
            assertEquals("u001", iv.getCandidateId());
            assertEquals("Alice", iv.getCandidateName());
            assertEquals("EBU6304", iv.getModuleCode());
            assertEquals("Software Engineering", iv.getModuleName());
            assertEquals("2026-05-15", iv.getDate());
            assertEquals("10:00", iv.getStartTime());
            assertEquals("10:30", iv.getEndTime());
            assertEquals("video", iv.getType());
            assertEquals("Zoom", iv.getLocation());
            assertEquals("scheduled", iv.getStatus());
            assertNull(iv.getOutcome());
        }
    }

    @Nested
    @DisplayName("Module")
    class ModuleTests {
        @Test
        void shouldSetAndGetAllFields() {
            Module module = new Module();
            module.setId("m001");
            module.setCode("EBU6304");
            module.setName("Software Engineering");
            module.setDepartment("CS");
            module.setOrganiserId("u005");
            module.setOrganiserName("Prof. Turing");
            module.setTerm("2025-2026 Spring");
            module.setStatus("active");

            assertEquals("m001", module.getId());
            assertEquals("EBU6304", module.getCode());
            assertEquals("Software Engineering", module.getName());
            assertEquals("CS", module.getDepartment());
            assertEquals("u005", module.getOrganiserId());
            assertEquals("Prof. Turing", module.getOrganiserName());
            assertEquals("2025-2026 Spring", module.getTerm());
            assertEquals("active", module.getStatus());
        }
    }

    @Nested
    @DisplayName("DataStore")
    class DataStoreTests {
        @Test
        void shouldSupportGettersAfterSetters() {
            DataStore ds = new DataStore();
            ds.setUsers(new ArrayList<>());
            ds.setJobs(new ArrayList<>());
            ds.setApplications(new ArrayList<>());
            ds.setProfiles(new ArrayList<>());
            ds.setModules(new ArrayList<>());
            ds.setInterviews(new ArrayList<>());
            ds.setTerms(new ArrayList<>());
            ds.setDepartments(new ArrayList<>());
            ds.setLogs(new ArrayList<>());
            ds.setWorkloadAssignments(new ArrayList<>());

            assertNotNull(ds.getUsers());
            assertNotNull(ds.getJobs());
            assertNotNull(ds.getApplications());
            assertNotNull(ds.getProfiles());
            assertNotNull(ds.getModules());
            assertNotNull(ds.getInterviews());
            assertNotNull(ds.getTerms());
            assertNotNull(ds.getDepartments());
            assertNotNull(ds.getLogs());
            assertNotNull(ds.getWorkloadAssignments());
        }

        @Test
        void shouldSupportSettersForLists() {
            DataStore ds = new DataStore();
            List<User> users = new ArrayList<>();
            users.add(new User());
            ds.setUsers(users);
            assertEquals(1, ds.getUsers().size());
        }
    }

    @Nested
    @DisplayName("Term & Department")
    class TermDepartmentTests {
        @Test
        void termShouldWork() {
            Term term = new Term();
            term.setId("t001");
            term.setName("2025-2026 Spring");
            term.setStartDate("2026-02-01");
            term.setEndDate("2026-06-30");
            term.setStatus("active");

            assertEquals("t001", term.getId());
            assertEquals("2025-2026 Spring", term.getName());
            assertEquals("2026-02-01", term.getStartDate());
            assertEquals("2026-06-30", term.getEndDate());
            assertEquals("active", term.getStatus());
        }

        @Test
        void departmentShouldWork() {
            Department dept = new Department();
            dept.setId("d001");
            dept.setName("Computer Science");
            dept.setModuleCount(5);
            dept.setStatus("active");

            assertEquals("d001", dept.getId());
            assertEquals("Computer Science", dept.getName());
            assertEquals(5, dept.getModuleCount());
            assertEquals("active", dept.getStatus());
        }
    }

    @Nested
    @DisplayName("LogEntry")
    class LogEntryTests {
        @Test
        void shouldSetAndGetAllFields() {
            LogEntry log = new LogEntry();
            log.setId("EVT-9001");
            log.setTimestamp("2026-05-01T10:00:00");
            log.setAction("LOGIN");
            log.setType("auth");
            log.setActor("Alice");
            log.setActorId("u001");
            log.setDetails("User logged in");
            log.setIp("192.168.1.1");

            assertEquals("EVT-9001", log.getId());
            assertEquals("2026-05-01T10:00:00", log.getTimestamp());
            assertEquals("LOGIN", log.getAction());
            assertEquals("auth", log.getType());
            assertEquals("Alice", log.getActor());
            assertEquals("u001", log.getActorId());
            assertEquals("User logged in", log.getDetails());
            assertEquals("192.168.1.1", log.getIp());
        }
    }

    @Nested
    @DisplayName("WorkloadAssignment")
    class WorkloadAssignmentTests {
        @Test
        void shouldSetAndGetAllFields() {
            WorkloadAssignment wa = new WorkloadAssignment();
            wa.setId("w001");
            wa.setTaUserId("u001");
            wa.setModuleCode("EBU6304");
            wa.setHoursPerWeek(10);
            wa.setTerm("2025-2026 Spring");
            wa.setStatus("active");

            assertEquals("w001", wa.getId());
            assertEquals("u001", wa.getTaUserId());
            assertEquals("EBU6304", wa.getModuleCode());
            assertEquals(10, wa.getHoursPerWeek());
            assertEquals("2025-2026 Spring", wa.getTerm());
            assertEquals("active", wa.getStatus());
        }
    }

    @Nested
    @DisplayName("Notification")
    class NotificationTests {
        @Test
        void shouldSetAndGetAllFields() {
            Notification n = new Notification();
            n.setId("n001");
            n.setUserId("u001");
            n.setType("status_change");
            n.setTitle("Application Updated");
            n.setMessage("Your status changed");
            n.setRelatedId("a001");
            n.setCreatedAt("2026-05-01T10:00:00");
            n.setRead(false);

            assertEquals("n001", n.getId());
            assertEquals("u001", n.getUserId());
            assertEquals("status_change", n.getType());
            assertEquals("Application Updated", n.getTitle());
            assertEquals("Your status changed", n.getMessage());
            assertEquals("a001", n.getRelatedId());
            assertFalse(n.isRead());

            n.setRead(true);
            assertTrue(n.isRead());
        }
    }

    @Nested
    @DisplayName("Message")
    class MessageTests {
        @Test
        void shouldSetAndGetAllFields() {
            Message msg = new Message();
            msg.setId("msg001");
            msg.setConversationId("u001_u005");
            msg.setSenderId("u001");
            msg.setReceiverId("u005");
            msg.setContent("Hello!");
            msg.setTimestamp("2026-05-01T10:00:00");
            msg.setRead(false);
            msg.setReplyToId(null);
            msg.setRecalled(false);
            msg.setEdited(false);
            msg.setEditedAt(null);
            msg.setMessageType("text");
            msg.setAttachmentUrl(null);
            msg.setAttachmentName(null);
            msg.setAttachmentSize(null);

            assertEquals("msg001", msg.getId());
            assertEquals("u001_u005", msg.getConversationId());
            assertEquals("u001", msg.getSenderId());
            assertEquals("u005", msg.getReceiverId());
            assertEquals("Hello!", msg.getContent());
            assertFalse(msg.isRead());
            assertFalse(msg.isRecalled());
            assertFalse(msg.isEdited());
            assertNull(msg.getReplyToId());
            assertEquals("text", msg.getMessageType());
        }
    }
}
