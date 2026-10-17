package com.tamo.service;

import com.google.gson.*;
import com.tamo.model.*;
import com.tamo.util.JsonUtil;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SectionMatchingServiceTest {
    DataStore data; Application app; Job job; CourseSection section; WeeklyAvailability snapshot;
    SectionMatchingService service = new SectionMatchingService();
    @BeforeEach void setup() {
        data = TestDataFactory.createTestDataStore();
        job = data.getJobs().stream().filter(j -> "u005".equals(j.getOrganiserId())).findFirst().orElseThrow();
        app = data.getApplications().stream().filter(a -> job.getId().equals(a.getJobId())).findFirst().orElseThrow();
        app.setPeriodId("period_test");
        snapshot = new WeeklyAvailability(); snapshot.setApplicantId(app.getApplicantId()); snapshot.setPeriodId(app.getPeriodId());
        snapshot.setTimeZone(AvailabilityService.TIME_ZONE); snapshot.setAvailableSlots(new ArrayList<>()); snapshot.setConflictSlots(new ArrayList<>());
        app.setAvailabilitySnapshot(snapshot);
        section = new CourseSection(); section.setId("section_test"); section.setPeriodId("period_test"); section.setModuleId(job.getModuleId()); section.setName("Lab A");
        section.setStatus("active"); section.setType("lab"); section.setTimeZone(AvailabilityService.TIME_ZONE); section.setTaPositions(2);
        section.setMeetingTimes(List.of(meeting(1,"10:00","12:00")));
        data.getCourseSections().add(section);
    }
    WeeklyAvailability.Slot slot(int day, String start, String end) {
        WeeklyAvailability.Slot s = new WeeklyAvailability.Slot(); s.setDayOfWeek(day); s.setStartTime(start); s.setEndTime(end); return s;
    }
    CourseSection.MeetingTime meeting(int day, String start, String end) {
        CourseSection.MeetingTime m = new CourseSection.MeetingTime(); m.setDayOfWeek(day); m.setStartTime(start); m.setEndTime(end); return m;
    }
    JsonObject match() { return service.match(data, app, job, "u005"); }
    JsonObject row() { return match().getAsJsonArray("sections").get(0).getAsJsonObject(); }
    String status() { return row().get("status").getAsString(); }
    @Test void fullCoverageAndAdjacentSlotsCountAsAvailable() {
        snapshot.setAvailableSlots(List.of(slot(1,"09:00","11:00"), slot(1,"11:00","12:00")));
        assertEquals("available",status()); assertTrue(row().getAsJsonArray("issues").isEmpty());
    }
    @Test void gapsAreReportedPrecisely() {
        snapshot.setAvailableSlots(List.of(slot(1,"10:00","10:30"),slot(1,"11:00","12:00")));
        JsonObject issue=row().getAsJsonArray("issues").get(0).getAsJsonObject();
        assertEquals("conflict",status()); assertEquals("10:30",issue.get("startTime").getAsString()); assertEquals("11:00",issue.get("endTime").getAsString());
    }
    @Test void explicitConflictsOverrideAvailabilityButTouchingEndpointsDoNotOverlap() {
        snapshot.setAvailableSlots(List.of(slot(1,"00:00","24:00")));
        snapshot.setConflictSlots(List.of(slot(1,"09:00","10:00"),slot(1,"12:00","13:00")));
        assertEquals("available",status());
        snapshot.setConflictSlots(List.of(slot(1,"10:30","11:00")));
        assertEquals("Explicit time conflict",row().getAsJsonArray("issues").get(0).getAsJsonObject().get("reason").getAsString());
    }
    @Test void allMeetingsMustFitAndDaysAreDistinct() {
        snapshot.setAvailableSlots(List.of(slot(1,"10:00","12:00")));
        section.setMeetingTimes(List.of(meeting(1,"10:00","12:00"),meeting(3,"10:00","12:00")));
        assertEquals("conflict",status()); assertEquals(3,row().getAsJsonArray("issues").get(0).getAsJsonObject().get("dayOfWeek").getAsInt());
    }
    @Test void noAvailabilityConflictsEvenIfSlotsExist() {
        snapshot.setAvailableSlots(List.of(slot(1,"10:00","12:00"))); snapshot.setNoAvailability(true);
        assertEquals("conflict",status());
    }
    @Test void missingSnapshotNeverFallsBackToLiveAvailability() {
        snapshot.setAvailableSlots(List.of(slot(1,"10:00","12:00"))); data.getWeeklyAvailabilities().add(snapshot);
        app.setAvailabilitySnapshot(null); assertEquals("pending",status());
    }
    @Test void differentIdentityPeriodOrZoneNeedsConfirmation() {
        snapshot.setApplicantId("someone_else"); assertEquals("pending",status()); snapshot.setApplicantId(app.getApplicantId());
        snapshot.setPeriodId("other"); assertEquals("pending",status()); snapshot.setPeriodId(app.getPeriodId());
        snapshot.setTimeZone("UTC"); assertEquals("pending",status());
    }
    @Test void malformedDataCannotProduceAvailable() {
        snapshot.setAvailableSlots(List.of(slot(0,"10:00","12:00"))); assertEquals("pending",status());
        snapshot.setAvailableSlots(List.of(slot(1,"10:00","25:00"))); assertEquals("pending",status());
        snapshot.setAvailableSlots(List.of(slot(1,"12:00","10:00"))); assertEquals("pending",status());
        snapshot.setAvailableSlots(null); assertEquals("pending",status());
    }
    @Test void midnightAndSundayAreSupported() {
        section.setMeetingTimes(List.of(meeting(7,"23:00","24:00")));
        snapshot.setAvailableSlots(List.of(slot(7,"23:00","24:00"))); assertEquals("available",status());
    }
    @Test void archivedAndOtherCourseSectionsAreExcludedAndEmptyIsPending() {
        section.setStatus("archived"); assertTrue(match().getAsJsonArray("sections").isEmpty());
        assertEquals("pending",match().get("status").getAsString());
        section.setStatus("active"); section.setModuleId("other"); assertTrue(match().getAsJsonArray("sections").isEmpty());
    }
    @Test void aggregateIsAnyAvailableAndRecalculatesWithoutMutatingSnapshot() {
        String before=JsonUtil.getGson().toJson(snapshot);
        CourseSection other=JsonUtil.getGson().fromJson(JsonUtil.getGson().toJson(section),CourseSection.class);
        other.setId("other"); other.setMeetingTimes(List.of(meeting(2,"10:00","12:00"))); data.getCourseSections().add(other);
        assertEquals("conflict",match().get("status").getAsString());
        snapshot.setAvailableSlots(List.of(slot(2,"10:00","12:00")));
        String saved=JsonUtil.getGson().toJson(snapshot);
        assertEquals("available",match().get("status").getAsString());
        assertEquals(saved,JsonUtil.getGson().toJson(snapshot)); assertNotEquals(before,saved);
        other.setStatus("archived"); assertEquals("conflict",match().get("status").getAsString());
    }
    @Test void ownershipChangeDoesNotExposeSections() {
        assertTrue(service.match(data,app,job,"u006").getAsJsonArray("sections").isEmpty());
        data.getModules().stream().filter(m->m.getId().equals(job.getModuleId())).findFirst().orElseThrow().setOrganiserId("u006");
        assertTrue(match().getAsJsonArray("sections").isEmpty());
    }
    @Test void pipelineAndDetailIncludeSameAdviceAndKeepExistingAccessChecks() {
        JsonDataService ds=mock(JsonDataService.class); when(ds.getData()).thenReturn(data);
        OrganiserService organiser=new OrganiserService(ds);
        JsonObject detail=(JsonObject)organiser.getApplicationDetails("u005",app.getId()).get("data");
        assertEquals(match(),detail.getAsJsonObject("sectionMatching"));
        JsonObject pipeline=organiser.getPipeline("u005",null);
        JsonObject card=null;
        for (JsonElement group:pipeline.asMap().values()) for(JsonElement item:group.getAsJsonArray())
            if(app.getId().equals(item.getAsJsonObject().get("id").getAsString())) card=item.getAsJsonObject();
        assertNotNull(card); assertEquals(match(),card.getAsJsonObject("sectionMatching"));
        assertEquals(403,organiser.getApplicationDetails("u006",app.getId()).get("code"));
    }
}
