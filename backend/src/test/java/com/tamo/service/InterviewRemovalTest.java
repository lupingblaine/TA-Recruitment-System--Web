package com.tamo.service;

import com.google.gson.*;
import com.tamo.model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class InterviewRemovalTest {
    DataStore ds; JsonDataService data; OrganiserService organiser; ApplicantService applicant;
    @BeforeEach void setup() {
        ds=TestDataFactory.createTestDataStore(); data=JsonDataService.detached(ds);
        organiser=new OrganiserService(data); applicant=new ApplicantService(data);
        ds.getApplications().get(0).setStatus("interview");
    }
    @Test void dashboardsContainNoInterviewStatsOrEvents() {
        JsonObject student=applicant.getDashboard("u001");
        assertFalse(student.has("interviewCount"));
        student.getAsJsonArray("schedule").forEach(e->assertNotEquals("interview",e.getAsJsonObject().get("type").getAsString()));
        JsonObject staff=organiser.getDashboard("u005");
        assertFalse(staff.has("interviews"));assertFalse(staff.has("pendingInterviews"));assertFalse(staff.has("interviewsToSchedule"));
        staff.getAsJsonArray("recentApplications").forEach(e->assertEquals("application",e.getAsJsonObject().get("type").getAsString()));
    }
    @Test void historicalApplicationsRemainReviewableWithoutRewritingHistory() {
        String id=ds.getApplications().get(0).getId();int history=ds.getInterviews().size();
        JsonObject pipeline=organiser.getPipeline("u005",null);assertFalse(pipeline.has("interview"));
        assertTrue(pipeline.getAsJsonArray("review").asList().stream().anyMatch(e->id.equals(e.getAsJsonObject().get("id").getAsString())));
        assertEquals("interview",ds.getApplications().get(0).getStatus());assertEquals(history,ds.getInterviews().size());
    }
    @Test void newInterviewStatusIsRejected() {
        var result=organiser.updateCandidateStatus("u005",ds.getApplications().get(0).getId(),"interview");
        assertEquals(400,result.get("code"));
    }
    @Test void legacyNotificationsAreNotPresentedButRemainStored() {
        Notification n=new Notification();n.setId("legacy_notice");n.setUserId("u001");n.setType("interview_scheduled");n.setTitle("Old interview");n.setMessage("Historical");n.setCreatedAt("2026-10-01");ds.getNotifications().add(n);
        assertTrue(applicant.getNotifications("u001").stream().noneMatch(v->"legacy_notice".equals(v.get("id").getAsString())));
        assertTrue(ds.getNotifications().contains(n));
    }
    @Test void dataEditorCannotCreateChangeOrDeleteArchivedInterviews() {
        AdminService admin = new AdminService(data); int count = ds.getInterviews().size();
        assertEquals(410, admin.addToCollection("interviews", new JsonObject()).get("code"));
        assertEquals(410, admin.updateCollectionItem("interviews", "i001", new JsonObject()).get("code"));
        assertEquals(410, admin.deleteFromCollection("interviews", "i001").get("code"));
        assertEquals(count, ds.getInterviews().size());
        assertFalse(admin.getCollectionNames().asList().stream().anyMatch(v -> "interviews".equals(v.getAsString())));
    }
    @Test void editingOtherLegacyApplicationFieldsDoesNotRequireRewritingStatus() {
        AdminService admin = new AdminService(data);
        Application legacy = ds.getApplications().get(0);
        JsonObject update = new JsonObject();update.addProperty("status", "interview");update.addProperty("coverLetter", "Updated synthetic note");
        var result = admin.updateCollectionItem("applications", legacy.getId(), update);
        assertFalse(result.containsKey("error"), result.toString());
        assertEquals("interview", legacy.getStatus());
        Application active = ds.getApplications().get(1);active.setStatus("review");
        assertEquals(400, admin.updateCollectionItem("applications", active.getId(), update).get("code"));
    }
}
