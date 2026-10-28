package com.tamo.service;

import com.google.gson.JsonObject;
import com.tamo.model.DataStore;
import com.tamo.model.ReviewRecord;
import com.tamo.model.SectionAssignment;
import com.tamo.model.CourseSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminAccessRegressionTest {
    private DataStore data;
    private AdminService service;
    private JsonDataService storage;
    @BeforeEach void setup() {
        data = TestDataFactory.createTestDataStore();
        storage = mock(JsonDataService.class);
        when(storage.getData()).thenReturn(data);
        service = new AdminService(storage);
    }
    private JsonObject owner(String id) { JsonObject b=new JsonObject(); b.addProperty("organiserId",id); b.addProperty("organiserName","Forged display name"); return b; }
    @Test void courseTransferMovesAllExistingPositionsButPreservesHistoricalRecords() {
        ReviewRecord review = new ReviewRecord(); review.setId("review_transfer"); review.setApplicationId("a001"); review.setActorId("u005"); review.setAction("approve");
        JsonObject frozen = new JsonObject(); frozen.addProperty("organiserId", "u005"); review.setCriteriaSnapshot(frozen); data.getReviewRecords().add(review);
        SectionAssignment assignment = new SectionAssignment(); assignment.setId("assignment_transfer"); assignment.setApplicationId("a001"); assignment.setAssignedBy("u005"); assignment.setStatus("active");
        CourseSection snapshot = new CourseSection(); snapshot.setModuleId("m001"); assignment.setSectionSnapshot(snapshot); data.getSectionAssignments().add(assignment);
        String reviews = com.tamo.util.JsonUtil.getGson().toJson(data.getReviewRecords());
        String assignments = com.tamo.util.JsonUtil.getGson().toJson(data.getSectionAssignments());
        String applications = com.tamo.util.JsonUtil.getGson().toJson(data.getApplications());
        String interviews = com.tamo.util.JsonUtil.getGson().toJson(data.getInterviews());
        assertFalse(service.updateModule("m001",owner("u006")).containsKey("error"));
        assertEquals("u006",data.getModules().get(0).getOrganiserId());
        assertEquals("Grace Hopper",data.getModules().get(0).getOrganiserName());
        data.getJobs().stream().filter(j->"m001".equals(j.getModuleId())).forEach(j->{assertEquals("u006",j.getOrganiserId());assertEquals("Grace Hopper",j.getProfessor());});
        assertEquals(applications,com.tamo.util.JsonUtil.getGson().toJson(data.getApplications()));
        assertEquals(interviews,com.tamo.util.JsonUtil.getGson().toJson(data.getInterviews()));
        assertEquals(reviews,com.tamo.util.JsonUtil.getGson().toJson(data.getReviewRecords()));
        assertEquals(assignments,com.tamo.util.JsonUtil.getGson().toJson(data.getSectionAssignments()));
    }
    @Test void genericCourseTransferUsesSamePolicy() {
        Map<String,Object> result=service.updateCollectionItem("modules","m001",owner("u006"));
        assertFalse(result.containsKey("error")); assertNotNull(result.get("item"));
        assertEquals("u006",data.getJobs().get(0).getOrganiserId());
        assertEquals("Grace Hopper",data.getJobs().get(0).getProfessor());
    }
    @Test void invalidOwnerCannotPartiallyChangeCourse() {
        for(String id:new String[]{"missing","u001","u003"}) {
            JsonObject b=owner(id); b.addProperty("name","Changed");
            assertEquals(400,service.updateModule("m001",b).get("code_http"));
            assertEquals("Software Engineering",data.getModules().get(0).getName());
            assertEquals("u005",data.getJobs().get(0).getOrganiserId());
        }
        data.getUsers().stream().filter(u->"u006".equals(u.getId())).findFirst().orElseThrow().setStatus("suspended");
        assertEquals(400,service.updateCollectionItem("modules","m001",owner("u006")).get("code"));
        verify(storage,never()).save();
    }
    @Test void genericAccountWritesAreForbiddenBeforeMutation() {
        String before=com.tamo.util.JsonUtil.getGson().toJson(data);
        JsonObject b=new JsonObject();b.addProperty("name","Injected");b.addProperty("email","injected@example.edu");b.addProperty("password","password");b.addProperty("role","root");
        assertEquals(403,service.addToCollection("users",b).get("code"));
        assertEquals(403,service.updateCollectionItem("users","u010",b).get("code"));
        assertEquals(403,service.deleteFromCollection("users","u010").get("code"));
        assertEquals(before,com.tamo.util.JsonUtil.getGson().toJson(data));
        verify(storage,never()).save();
    }
    @Test void genericAccountListingNeverExposesPasswordHashes() {
        service.getCollectionData("users").getAsJsonArray().forEach(u->assertFalse(u.getAsJsonObject().has("password")));
    }
    @Test void usersBusinessPathRejectsSelfDisableAndUnknownStateBeforeMutation() {
        JsonObject b=new JsonObject();b.addProperty("status","suspended");b.addProperty("name","Changed");
        assertEquals(400,service.updateUser("u010","u010",b).get("code"));
        assertEquals("System Admin",data.getUsers().get(5).getName());
        b.addProperty("status","made_up");
        assertEquals(400,service.updateUser("u010","u001",b).get("code"));
        assertEquals("Alex Johnson",data.getUsers().get(0).getName());
        verify(storage,never()).save();
    }
    @Test void userCreationRejectsUnknownState() {
        JsonObject b=new JsonObject();b.addProperty("name","New");b.addProperty("email","new@example.edu");b.addProperty("role","applicant");b.addProperty("status","made_up");
        assertEquals(400,service.createUser(b).get("code"));assertEquals(6,data.getUsers().size());
    }
    @Test void genericApplicationStatusChangesRequireWorkflow() {
        var app=data.getApplications().get(0);
        String previous=app.getStatus();
        JsonObject body=new JsonObject();body.addProperty("status","rejected");body.addProperty("coverLetter","Should not mutate");
        assertEquals(409,service.updateCollectionItem("applications",app.getId(),body).get("code"));
        assertEquals(previous,app.getStatus());
        verify(storage,never()).save();
        body.addProperty("status",previous);
        assertFalse(service.updateCollectionItem("applications",app.getId(),body).containsKey("error"));
    }
}
