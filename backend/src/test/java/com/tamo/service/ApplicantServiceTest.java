package com.tamo.service;

import com.google.gson.JsonObject;
import com.tamo.model.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ApplicantService — TA/Applicant Feature Tests")
@ExtendWith(MockitoExtension.class)
class ApplicantServiceTest {

    @Mock private JsonDataService dataService;
    private ApplicantService service;
    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = TestDataFactory.createTestDataStore();
        lenient().when(dataService.getData()).thenReturn(dataStore);
        lenient().when(dataService.generateId(anyString(), anyList())).thenReturn("new001");
        service = new ApplicantService(dataService);
    }

    @Nested
    @DisplayName("getDashboard")
    class DashboardTests {
        @Test
        @DisplayName("Should return correct stats for applicant u001")
        void shouldReturnCorrectStats() {
            JsonObject result = service.getDashboard("u001");

            assertNotNull(result);
            assertTrue(result.has("activeApplications"));
            assertTrue(result.has("interviewCount"));
            assertTrue(result.has("profileCompletion"));
            assertTrue(result.has("recentApplications"));
            assertTrue(result.has("workload"));
            assertTrue(result.has("upcomingDeadlines"));
            assertTrue(result.has("notifications"));

            assertEquals(80, result.get("profileCompletion").getAsInt());
        }

        @Test
        @DisplayName("Workload should reflect offer status")
        void workloadShouldReflectOffers() {
            JsonObject result = service.getDashboard("u001");
            JsonObject workload = result.getAsJsonObject("workload");

            assertTrue(workload.get("current").getAsInt() > 0);
            assertEquals(20, workload.get("max").getAsInt());
        }

        @Test
        @DisplayName("Should include unread notification count")
        void shouldIncludeUnreadNotifications() {
            JsonObject result = service.getDashboard("u001");
            assertTrue(result.has("unreadNotifications"));
            assertEquals(1, result.get("unreadNotifications").getAsLong());
        }
    }

    @Nested
    @DisplayName("getJobs")
    class GetJobsTests {
        @Test
        @DisplayName("Should return all jobs without filters")
        void shouldReturnAllJobs() {
            List<JsonObject> jobs = service.getJobs(null, null, null, null);
            assertEquals(3, jobs.size());
        }

        @Test
        @DisplayName("Should filter by status=active")
        void shouldFilterByStatus() {
            List<JsonObject> jobs = service.getJobs(null, null, null, "active");
            assertEquals(2, jobs.size());
            jobs.forEach(j -> assertEquals("active", j.get("status").getAsString()));
        }

        @Test
        @DisplayName("Should filter by keyword in title")
        void shouldFilterByKeyword() {
            List<JsonObject> jobs = service.getJobs("Digital", null, null, null);
            assertEquals(1, jobs.size());
            assertTrue(jobs.get(0).get("title").getAsString().contains("Digital"));
        }

        @Test
        @DisplayName("Should filter by department")
        void shouldFilterByDepartment() {
            List<JsonObject> jobs = service.getJobs(null, "EE", null, null);
            assertEquals(1, jobs.size());
        }

        @Test
        @DisplayName("Each job should include applicantCount")
        void jobsShouldIncludeApplicantCount() {
            List<JsonObject> jobs = service.getJobs(null, null, null, null);
            for (JsonObject job : jobs) {
                assertTrue(job.has("applicantCount"));
            }
        }
    }

    @Nested
    @DisplayName("getJobById")
    class GetJobByIdTests {
        @Test
        @DisplayName("Existing job ID should return job")
        void existingIdShouldReturnJob() {
            assertNotNull(service.getJobById("j001"));
        }

        @Test
        @DisplayName("Non-existent job ID should return null")
        void nonExistentShouldReturnNull() {
            assertNull(service.getJobById("j999"));
        }
    }

    @Nested
    @DisplayName("applyForJob")
    class ApplyTests {
        @Test
        @DisplayName("Valid application should succeed")
        void validApplicationShouldSucceed() {
            Map<String, Object> result = service.applyForJob("u002", "j003", "I want this", List.of());

            assertNotNull(result.get("application"));
            assertNull(result.get("error"));
            verify(dataService).save();
        }

        @Test
        @DisplayName("Applying for non-existent job should fail with 404")
        void nonExistentJobShouldFail() {
            Map<String, Object> result = service.applyForJob("u001", "j999", "cover", List.of());
            assertEquals("Job not found", result.get("error"));
            assertEquals(404, result.get("code"));
        }

        @Test
        @DisplayName("Applying for closed job should fail with 400")
        void closedJobShouldFail() {
            Map<String, Object> result = service.applyForJob("u002", "j002", "cover", List.of());
            assertEquals(400, result.get("code"));
        }

        @Test
        @DisplayName("Missing cover letter should fail with 400")
        void missingCoverLetterShouldFail() {
            Map<String, Object> result = service.applyForJob("u002", "j001", "", List.of());
            assertEquals(400, result.get("code"));
        }

        @Test
        @DisplayName("Duplicate application should fail with 409")
        void duplicateApplicationShouldFail() {
            Map<String, Object> result = service.applyForJob("u001", "j001", "again", List.of());
            assertEquals(409, result.get("code"));
        }
    }

    @Nested
    @DisplayName("getMyApplications")
    class MyApplicationsTests {
        @Test
        @DisplayName("Should return all applications for user u001")
        void shouldReturnUserApplications() {
            List<JsonObject> apps = service.getMyApplications("u001");
            assertEquals(2, apps.size());
        }

        @Test
        @DisplayName("Should return empty for user with no applications")
        void shouldReturnEmptyForNoApps() {
            List<JsonObject> apps = service.getMyApplications("u003");
            assertEquals(0, apps.size());
        }

        @Test
        @DisplayName("Should enrich with jobTitle and moduleCode")
        void shouldEnrichWithJobInfo() {
            List<JsonObject> apps = service.getMyApplications("u001");
            for (JsonObject app : apps) {
                assertTrue(app.has("jobTitle"));
                assertTrue(app.has("moduleCode"));
            }
        }
    }

    @Nested
    @DisplayName("getApplicationDetail")
    class AppDetailTests {
        @Test
        @DisplayName("Owner should see their application detail")
        void ownerShouldSeeDetail() {
            Map<String, Object> result = service.getApplicationDetail("u001", "a001");
            assertNotNull(result.get("data"));
        }

        @Test
        @DisplayName("Non-owner should get forbidden")
        void nonOwnerShouldGetForbidden() {
            Map<String, Object> result = service.getApplicationDetail("u002", "a001");
            assertTrue((Boolean) result.get("forbidden"));
        }

        @Test
        @DisplayName("Non-existent application should get notFound")
        void nonExistentShouldGetNotFound() {
            Map<String, Object> result = service.getApplicationDetail("u001", "a999");
            assertTrue((Boolean) result.get("notFound"));
        }
    }

    @Nested
    @DisplayName("withdrawApplication")
    class WithdrawTests {
        @Test
        @DisplayName("Owner can withdraw applied application")
        void ownerCanWithdrawApplied() {
            Map<String, Object> result = service.withdrawApplication("u001", "a001");
            assertTrue((Boolean) result.get("success"));
            verify(dataService).save();
        }

        @Test
        @DisplayName("Cannot withdraw offer-status application")
        void cannotWithdrawOffer() {
            Map<String, Object> result = service.withdrawApplication("u001", "a002");
            assertEquals(400, result.get("code"));
        }

        @Test
        @DisplayName("Non-owner cannot withdraw")
        void nonOwnerCannotWithdraw() {
            Map<String, Object> result = service.withdrawApplication("u002", "a001");
            assertEquals(403, result.get("code"));
        }

        @Test
        @DisplayName("Non-existent application cannot be withdrawn")
        void nonExistentCannotWithdraw() {
            Map<String, Object> result = service.withdrawApplication("u001", "a999");
            assertEquals(404, result.get("code"));
        }
    }

    @Nested
    @DisplayName("Profile & Documents")
    class ProfileTests {
        @Test
        @DisplayName("getProfile should return user and profile info")
        void getProfileShouldReturnInfo() {
            JsonObject result = service.getProfile("u001");
            assertNotNull(result);
            assertTrue(result.has("user"));
            assertTrue(result.has("profile"));
            assertEquals("Alex Johnson", result.getAsJsonObject("user").get("name").getAsString());
        }

        @Test
        @DisplayName("getProfile for non-existent user should return null")
        void getProfileForNonExistent() {
            JsonObject result = service.getProfile("u999");
            assertNull(result);
        }

        @Test
        @DisplayName("updateProfile should save changes")
        void updateProfileShouldSave() {
            JsonObject updates = new JsonObject();
            updates.addProperty("name", "Alex Updated");
            updates.addProperty("degree", "MSc CS");
            updates.addProperty("gpa", 3.9);

            String error = service.updateProfile("u001", updates);
            assertNull(error);
            verify(dataService).save();
        }

        @Test
        @DisplayName("GPA exceeding max should fail")
        void gpaExceedingMaxShouldFail() {
            JsonObject updates = new JsonObject();
            updates.addProperty("gpa", 5.0);

            String error = service.updateProfile("u001", updates);
            assertNotNull(error);
            assertTrue(error.contains("GPA"));
        }

        @Test
        @DisplayName("Upload valid document should succeed")
        void uploadDocShouldSucceed() {
            Map<String, Object> result = service.uploadDocument("u001", "cv.pdf", "application/pdf", "200KB");
            assertNotNull(result.get("document"));
        }

        @Test
        @DisplayName("Upload unsupported file type should fail")
        void uploadUnsupportedTypeShouldFail() {
            Map<String, Object> result = service.uploadDocument("u001", "photo.jpg", "image/jpeg", "1MB");
            assertEquals(400, result.get("code"));
        }

        @Test
        @DisplayName("Upload with empty filename should fail")
        void uploadEmptyFilenameShouldFail() {
            Map<String, Object> result = service.uploadDocument("u001", "", "text/plain", "0");
            assertEquals(400, result.get("code"));
        }

        @Test
        @DisplayName("Delete existing document should succeed")
        void deleteDocShouldSucceed() {
            Map<String, Object> result = service.deleteDocument("u001", "doc001");
            assertTrue((Boolean) result.get("success"));
        }

        @Test
        @DisplayName("Delete non-existent document should fail")
        void deleteNonExistentDocShouldFail() {
            Map<String, Object> result = service.deleteDocument("u001", "doc999");
            assertEquals(404, result.get("code"));
        }
    }

    @Nested
    @DisplayName("Notifications")
    class NotificationTests {
        @Test
        @DisplayName("Should return notifications for user")
        void shouldReturnNotifications() {
            List<JsonObject> notifs = service.getNotifications("u001");
            assertEquals(2, notifs.size());
        }

        @Test
        @DisplayName("markNotificationRead should update and save")
        void markReadShouldSave() {
            service.markNotificationRead("u001", "n001");
            verify(dataService).save();
            assertTrue(dataStore.getNotifications().get(0).isRead());
        }

        @Test
        @DisplayName("markAllNotificationsRead should update all")
        void markAllReadShouldSave() {
            service.markAllNotificationsRead("u001");
            verify(dataService).save();
            assertTrue(dataStore.getNotifications().stream()
                    .filter(n -> "u001".equals(n.getUserId()))
                    .allMatch(n -> n.isRead()));
        }
    }
}
