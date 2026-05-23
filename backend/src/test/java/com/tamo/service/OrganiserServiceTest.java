package com.tamo.service;

import com.google.gson.JsonObject;
import com.tamo.model.Application;
import com.tamo.model.DataStore;
import com.tamo.model.Interview;
import com.tamo.model.Job;
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

@DisplayName("OrganiserService — Module Organiser Feature Tests")
@ExtendWith(MockitoExtension.class)
class OrganiserServiceTest {

    @Mock private JsonDataService dataService;
    private OrganiserService service;
    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = TestDataFactory.createTestDataStore();
        lenient().when(dataService.getData()).thenReturn(dataStore);
        lenient().when(dataService.generateId(anyString(), anyList())).thenReturn("new001");
        service = new OrganiserService(dataService);
    }

    @Nested
    @DisplayName("getDashboard")
    class DashboardTests {
        @Test
        @DisplayName("Should return stats for organiser u005")
        void shouldReturnStats() {
            JsonObject result = service.getDashboard("u005");
            assertNotNull(result);
            assertTrue(result.has("activePostings"));
            assertTrue(result.has("currentApplicants"));
            assertTrue(result.has("totalApplications"));
            assertTrue(result.has("recentApplications"));
            assertTrue(result.has("activeJobs"));

            assertEquals(1, result.get("activePostings").getAsLong());
        }

        @Test
        @DisplayName("Organiser with no jobs should get zeros")
        void noJobsShouldGetZeros() {
            JsonObject result = service.getDashboard("u999");
            assertEquals(0, result.get("activePostings").getAsLong());
            assertEquals(0, result.get("totalApplications").getAsLong());
        }
    }

    @Nested
    @DisplayName("getJobs")
    class GetJobsTests {
        @Test
        @DisplayName("Should return only organiser's jobs")
        void shouldReturnOwnJobs() {
            List<JsonObject> jobs = service.getJobs("u005");
            assertEquals(2, jobs.size());
            jobs.forEach(j -> assertEquals("u005", j.get("organiserId").getAsString()));
        }

        @Test
        @DisplayName("Jobs should include application counts")
        void jobsShouldIncludeCounts() {
            List<JsonObject> jobs = service.getJobs("u005");
            for (JsonObject j : jobs) {
                assertTrue(j.has("applicants"));
                assertTrue(j.has("totalApplications"));
            }
        }
    }

    @Nested
    @DisplayName("createJob")
    class CreateJobTests {
        @Test
        @DisplayName("Valid job creation should succeed")
        void validJobShouldSucceed() {
            JsonObject body = new JsonObject();
            body.addProperty("title", "New TA Position");
            body.addProperty("moduleId", "m001");
            body.addProperty("moduleCode", "EBU6304");
            body.addProperty("description", "New position");
            body.addProperty("hoursPerWeek", 8);
            body.addProperty("positions", 1);
            body.addProperty("deadline", "2027-01-01");

            Map<String, Object> result = service.createJob("u005", body);
            assertNotNull(result.get("job"));
            assertNull(result.get("error"));
            verify(dataService).save();
        }

        @Test
        @DisplayName("Missing title should fail")
        void missingTitleShouldFail() {
            JsonObject body = new JsonObject();
            body.addProperty("moduleId", "m001");

            Map<String, Object> result = service.createJob("u005", body);
            assertEquals(400, result.get("code"));
        }

        @Test
        @DisplayName("Organiser posting under wrong module should fail with 403")
        void wrongModuleShouldFail() {
            JsonObject body = new JsonObject();
            body.addProperty("title", "Unauthorized");
            body.addProperty("moduleId", "m002");
            body.addProperty("moduleCode", "EBU5476");

            Map<String, Object> result = service.createJob("u005", body);
            assertEquals(403, result.get("code"));
        }
    }

    @Nested
    @DisplayName("updateJob")
    class UpdateJobTests {
        @Test
        @DisplayName("Owner can update their job")
        void ownerCanUpdate() {
            JsonObject body = new JsonObject();
            body.addProperty("title", "Updated Title");

            Map<String, Object> result = service.updateJob("u005", "j001", body);
            assertNotNull(result.get("job"));
            Job updated = (Job) result.get("job");
            assertEquals("Updated Title", updated.getTitle());
        }

        @Test
        @DisplayName("Non-owner cannot update")
        void nonOwnerCannotUpdate() {
            JsonObject body = new JsonObject();
            body.addProperty("title", "Hack");

            Map<String, Object> result = service.updateJob("u006", "j001", body);
            assertEquals(403, result.get("code"));
        }

        @Test
        @DisplayName("Updating non-existent job should fail")
        void nonExistentShouldFail() {
            Map<String, Object> result = service.updateJob("u005", "j999", new JsonObject());
            assertEquals(404, result.get("code"));
        }
    }

    @Nested
    @DisplayName("deleteJob")
    class DeleteJobTests {
        @Test
        @DisplayName("Owner can delete their job")
        void ownerCanDelete() {
            int jobsBefore = dataStore.getJobs().size();
            Map<String, Object> result = service.deleteJob("u005", "j001");
            assertTrue((Boolean) result.get("success"));
            assertEquals(jobsBefore - 1, dataStore.getJobs().size());
        }

        @Test
        @DisplayName("Deleting job should also clean up related applications")
        void deleteShouldCascade() {
            long appsBefore = dataStore.getApplications().stream()
                    .filter(a -> "j001".equals(a.getJobId())).count();
            assertTrue(appsBefore > 0);

            service.deleteJob("u005", "j001");

            long appsAfter = dataStore.getApplications().stream()
                    .filter(a -> "j001".equals(a.getJobId())).count();
            assertEquals(0, appsAfter);
        }

        @Test
        @DisplayName("Non-owner cannot delete")
        void nonOwnerCannotDelete() {
            Map<String, Object> result = service.deleteJob("u006", "j001");
            assertEquals(403, result.get("code"));
        }
    }

    @Nested
    @DisplayName("getPipeline")
    class PipelineTests {
        @Test
        @DisplayName("Should return applications grouped by status")
        void shouldGroupByStatus() {
            JsonObject pipeline = service.getPipeline("u005", null);
            assertNotNull(pipeline);
            assertTrue(pipeline.has("applied"));
            assertTrue(pipeline.has("review"));
            assertTrue(pipeline.has("shortlisted"));
            assertTrue(pipeline.has("interview"));
            assertTrue(pipeline.has("offer"));
        }

        @Test
        @DisplayName("Applied column should have correct count")
        void appliedShouldHaveCorrectCount() {
            JsonObject pipeline = service.getPipeline("u005", null);
            assertEquals(1, pipeline.getAsJsonArray("applied").size());
        }
    }

    @Nested
    @DisplayName("updateCandidateStatus")
    class StatusUpdateTests {
        @Test
        @DisplayName("Valid status transition should succeed")
        void validTransitionShouldSucceed() {
            Map<String, Object> result = service.updateCandidateStatus("u005", "a001", "review");
            assertNotNull(result.get("application"));
            Application app = (Application) result.get("application");
            assertEquals("review", app.getStatus());
        }

        @Test
        @DisplayName("Invalid status value should fail")
        void invalidStatusShouldFail() {
            Map<String, Object> result = service.updateCandidateStatus("u005", "a001", "invalid");
            assertEquals(400, result.get("code"));
        }

        @Test
        @DisplayName("Missing status should fail")
        void missingStatusShouldFail() {
            Map<String, Object> result = service.updateCandidateStatus("u005", "a001", "");
            assertEquals(400, result.get("code"));
        }

        @Test
        @DisplayName("Non-owner organiser should fail")
        void nonOwnerShouldFail() {
            Map<String, Object> result = service.updateCandidateStatus("u006", "a001", "review");
            assertEquals(403, result.get("code"));
        }

        @Test
        @DisplayName("Non-existent application should fail")
        void nonExistentShouldFail() {
            Map<String, Object> result = service.updateCandidateStatus("u005", "a999", "review");
            assertEquals(404, result.get("code"));
        }

        @Test
        @DisplayName("Status change should create notification for applicant")
        void statusChangeShouldNotify() {
            int notifsBefore = dataStore.getNotifications().size();
            service.updateCandidateStatus("u005", "a001", "shortlisted");
            assertTrue(dataStore.getNotifications().size() > notifsBefore);
        }
    }

    @Nested
    @DisplayName("applicationAction")
    class ActionTests {
        @Test
        @DisplayName("Shortlist action should change status to shortlisted")
        void shortlistShouldWork() {
            Map<String, Object> result = service.applicationAction("u005", "a001", "shortlist");
            Application app = (Application) result.get("application");
            assertEquals("shortlisted", app.getStatus());
        }

        @Test
        @DisplayName("Reject action should change status to rejected")
        void rejectShouldWork() {
            Map<String, Object> result = service.applicationAction("u005", "a001", "reject");
            Application app = (Application) result.get("application");
            assertEquals("rejected", app.getStatus());
        }

        @Test
        @DisplayName("Invalid action should fail")
        void invalidActionShouldFail() {
            Map<String, Object> result = service.applicationAction("u005", "a001", "promote");
            assertEquals(400, result.get("code"));
        }
    }

    @Nested
    @DisplayName("Interviews")
    class InterviewTests {
        @Test
        @DisplayName("getInterviews should return organiser's interviews")
        void shouldReturnInterviews() {
            List<Object> interviews = service.getInterviews("u005");
            assertEquals(1, interviews.size());
        }

        @Test
        @DisplayName("createInterview should succeed with valid data")
        void createShouldSucceed() {
            JsonObject body = new JsonObject();
            body.addProperty("applicationId", "a001");
            body.addProperty("candidateId", "u001");
            body.addProperty("date", "2026-06-01");
            body.addProperty("startTime", "14:00");
            body.addProperty("type", "video");

            Map<String, Object> result = service.createInterview("u005", body);
            assertNotNull(result.get("interview"));
            verify(dataService, atLeastOnce()).save();
        }

        @Test
        @DisplayName("createInterview with missing fields should fail")
        void createWithMissingFieldsShouldFail() {
            JsonObject body = new JsonObject();
            body.addProperty("applicationId", "a001");

            Map<String, Object> result = service.createInterview("u005", body);
            assertEquals(400, result.get("code"));
        }

        @Test
        @DisplayName("updateInterview should succeed for owner")
        void updateShouldSucceed() {
            JsonObject body = new JsonObject();
            body.addProperty("date", "2026-05-25");
            body.addProperty("status", "completed");

            Map<String, Object> result = service.updateInterview("u005", "i001", body);
            assertNotNull(result.get("interview"));
            Interview iv = (Interview) result.get("interview");
            assertEquals("2026-05-25", iv.getDate());
            assertEquals("completed", iv.getStatus());
        }

        @Test
        @DisplayName("updateInterview for non-owner should fail")
        void updateNonOwnerShouldFail() {
            Map<String, Object> result = service.updateInterview("u006", "i001", new JsonObject());
            assertEquals(403, result.get("code"));
        }

        @Test
        @DisplayName("deleteInterview should succeed for owner")
        void deleteShouldSucceed() {
            Map<String, Object> result = service.deleteInterview("u005", "i001");
            assertTrue((Boolean) result.get("success"));
        }

        @Test
        @DisplayName("deleteInterview for non-owner should fail")
        void deleteNonOwnerShouldFail() {
            Map<String, Object> result = service.deleteInterview("u006", "i001");
            assertEquals(403, result.get("code"));
        }
    }

    @Nested
    @DisplayName("getMyModules")
    class MyModulesTests {
        @Test
        @DisplayName("Should return modules owned by organiser")
        void shouldReturnOwnedModules() {
            List<JsonObject> modules = service.getMyModules("u005");
            assertEquals(1, modules.size());
            assertEquals("EBU6304", modules.get(0).get("code").getAsString());
        }

        @Test
        @DisplayName("Organiser with no modules should get empty list")
        void noModulesShouldReturnEmpty() {
            List<JsonObject> modules = service.getMyModules("u999");
            assertEquals(0, modules.size());
        }
    }

    @Nested
    @DisplayName("Notifications")
    class NotificationTests {
        @Test
        @DisplayName("Should return notifications for organiser")
        void shouldReturnNotifications() {
            List<JsonObject> notifs = service.getNotifications("u005");
            assertNotNull(notifs);
        }

        @Test
        @DisplayName("markAllNotificationsRead should update all")
        void markAllShouldUpdate() {
            service.markAllNotificationsRead("u005");
            verify(dataService, atLeastOnce()).save();
        }
    }
}
