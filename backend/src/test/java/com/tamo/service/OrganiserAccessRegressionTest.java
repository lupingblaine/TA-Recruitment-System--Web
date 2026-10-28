package com.tamo.service;

import com.google.gson.*;
import com.tamo.model.*;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class OrganiserAccessRegressionTest {
    DataStore ds;JsonDataService data;OrganiserService service;Application app;User teacher;
    @BeforeEach void setup(){
        ds=TestDataFactory.createTestDataStore();data=JsonDataService.detached(ds);service=new OrganiserService(data);
        app=ds.getApplications().get(0);teacher=ds.getUsers().stream().filter(u->"u005".equals(u.getId())).findFirst().orElseThrow();
    }
    @Test void revokedReadDeniesAllApplicationReadPaths(){
        teacher.setPermissions(List.of());
        assertEquals(403,assertThrows(QualificationService.Problem.class,()->service.getPipeline("u005",null)).status);
        assertEquals(403,assertThrows(QualificationService.Problem.class,()->service.getDashboard("u005")).status);
        assertEquals(403,service.getApplicationDetails("u005",app.getId()).get("code"));
    }
    @Test void revokedRecommendationCannotUseOldStatusEndpoint(){
        teacher.setPermissions(List.of("review.read"));
        assertEquals(403,service.updateCandidateStatus("u005",app.getId(),"shortlisted").get("code"));
        assertNotEquals("shortlisted",app.getStatus());
    }
    @Test void previousCourseOwnerCannotReadEditOrDeleteUsingStaleJobOwnership(){
        ds.getModules().stream().filter(m->"m001".equals(m.getId())).forEach(m->m.setOrganiserId("u006"));
        assertEquals(403,service.getApplicationDetails("u005",app.getId()).get("code"));
        JsonObject edit=new JsonObject();edit.addProperty("title","Must not change");
        assertEquals(403,service.updateJob("u005",app.getJobId(),edit).get("code"));
        assertEquals(403,service.deleteJob("u005",app.getJobId()).get("code"));
        assertTrue(service.getPipeline("u005",null).asMap().values().stream().allMatch(v->v.getAsJsonArray().isEmpty()));
    }
    @Test void currentCourseOwnerCanReadInheritedApplicationsEvenWithStalePostingMetadata(){
        ds.getModules().stream().filter(m->"m001".equals(m.getId())).forEach(m->m.setOrganiserId("u006"));
        assertTrue(service.getApplicationDetails("u006",app.getId()).containsKey("data"));
        assertTrue(service.getJobs("u006").stream().anyMatch(j->app.getJobId().equals(j.get("id").getAsString())));
    }
    @Test void explicitlyRestrictedTeacherCannotRejectBypassingFormalDecisionPermission(){
        teacher.setPermissions(List.of("review.read","review.recommend"));
        assertEquals(403,service.updateCandidateStatus("u005",app.getId(),"rejected").get("code"));
        assertNotEquals("rejected",app.getStatus());assertTrue(ds.getReviewRecords().isEmpty());
    }
    @Test void actionCapabilitiesReflectGrants(){
        JsonObject details=(JsonObject)service.getApplicationDetails("u005",app.getId()).get("data");
        assertTrue(details.get("canApprove").getAsBoolean());assertTrue(details.get("canRecommend").getAsBoolean());
    }
    @Test void authorizedRejectionRequiresReasonAndKeepsFormalDecisionConsistent(){
        teacher.setPermissions(List.of("review.read","review.approve"));
        assertEquals(400,service.updateCandidateStatus("u005",app.getId(),"rejected").get("code"));
        assertEquals(400,service.updateCandidateStatus("u005",app.getId(),"rejected","short").get("code"));
        ReviewRecord prior=new ReviewRecord();prior.setId("prior");prior.setActorId("u010");prior.setApplicationId(app.getId());prior.setAction("approve");ds.getReviewRecords().add(prior);
        var result=service.updateCandidateStatus("u005",app.getId(),"rejected","The applicant cannot cover this role.");
        assertFalse(result.containsKey("error"),result.toString());
        assertEquals("rejected",app.getStatus());assertFalse(ReviewService.approved(ds,app.getId()));
        assertEquals("not_selected",new ReviewService(data).row(teacher,app).get("selectionStatus").getAsString());
        ReviewRecord decision=ds.getReviewRecords().get(ds.getReviewRecords().size()-1);
        assertEquals("u005",decision.getActorId());assertEquals("The applicant cannot cover this role.",decision.getNote());
    }
    @Test void terminalDecisionCannotBeUndoneByMovingOldPipelineBackwards(){
        ReviewRecord prior=new ReviewRecord();prior.setId("prior");prior.setActorId("u010");prior.setApplicationId(app.getId());prior.setAction("approve");ds.getReviewRecords().add(prior);app.setStatus("offer");
        assertEquals(409,service.updateCandidateStatus("u005",app.getId(),"review").get("code"));assertEquals("offer",app.getStatus());
    }
    @Test void revokedReadAlsoDeniesAssignmentCandidateList(){
        teacher.setPermissions(List.of());
        assertEquals(403,assertThrows(SectionAssignmentService.Problem.class,()->new SectionAssignmentService(data).list("u005","organiser")).status);
    }
}
