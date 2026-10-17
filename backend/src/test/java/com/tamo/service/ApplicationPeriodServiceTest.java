package com.tamo.service;

import com.google.gson.JsonObject;
import com.tamo.model.*;
import org.junit.jupiter.api.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationPeriodServiceTest {
    private JsonDataService data;
    private DataStore ds;
    private ApplicationPeriodService service;
    private final Instant now = Instant.parse("2026-10-16T19:00:00Z");

    @BeforeEach void setup() {
        data = mock(JsonDataService.class);
        ds = TestDataFactory.createTestDataStore();
        when(data.getData()).thenReturn(ds);
        service = new ApplicationPeriodService(data, Clock.fixed(now, ZoneOffset.UTC));
    }

    private JsonObject details() {
        JsonObject body = new JsonObject();
        body.addProperty("name", "Fall 2026 applications"); body.addProperty("termId", "t001");
        body.addProperty("opensAt", "2026-10-16T11:00:00-07:00");
        body.addProperty("closesAt", "2026-10-17T12:00:00-07:00");
        return body;
    }

    @Test void createStartsAsDraftAndNormalizesTimezone() {
        ApplicationPeriod period = service.create(details());
        assertEquals("draft", period.getStatus());
        assertEquals("2026-10-16T18:00:00Z", period.getOpensAt());
        assertFalse(period.getLegacy());
        verify(data).save();
    }

    @Test void closingAndReopeningDoesNotChangeApplications() {
        int count = ds.getApplications().size();
        service.changeStatus("period_test", "close");
        assertNull(service.acceptingPeriod());
        assertEquals(count, service.applications("period_test").size());
        service.changeStatus("period_test", "open");
        assertEquals("period_test", service.acceptingPeriod().getId());
    }

    @Test void openingBoundaryIsInclusiveAndDeadlineIsExclusive() {
        ApplicationPeriod p = ds.getApplicationPeriods().get(0);
        p.setOpensAt(now.toString());
        assertEquals("open", service.effectiveStatus(p));
        p.setClosesAt(now.toString());
        assertEquals("expired", service.effectiveStatus(p));
        assertNull(service.acceptingPeriod());
    }

    @Test void futureWindowIsScheduledAndDoesNotAcceptApplications() {
        ds.getApplicationPeriods().get(0).setOpensAt(now.plusSeconds(1).toString());
        assertEquals("scheduled", service.current().get("effectiveStatus").getAsString());
        assertFalse(service.current().get("accepting").getAsBoolean());
    }

    @Test void cannotOpenTwoPeriods() {
        ApplicationPeriod second = service.create(details());
        assertEquals(409, assertThrows(ApplicationPeriodService.Problem.class,
                () -> service.changeStatus(second.getId(), "open")).status);
        assertEquals("draft", second.getStatus());
    }

    @Test void expiredPeriodNeedsDeadlineExtensionBeforeReopen() {
        ApplicationPeriod p = ds.getApplicationPeriods().get(0);
        p.setClosesAt(now.minusSeconds(1).toString()); p.setStatus("closed");
        assertThrows(ApplicationPeriodService.Problem.class, () -> service.changeStatus(p.getId(), "open"));
        service.update(p.getId(), details());
        service.changeStatus(p.getId(), "open");
        assertNotNull(service.acceptingPeriod());
    }

    @Test void rejectsInvalidDatesUnknownTermAndMissingTimezoneWithoutMutation() {
        for (String invalid : List.of("bad-date", "2026-10-17T12:00:00", "2026-10-15T00:00:00Z")) {
            JsonObject body = details(); body.addProperty("closesAt", invalid);
            assertEquals(400, assertThrows(ApplicationPeriodService.Problem.class, () -> service.create(body)).status);
        }
        JsonObject unknownTerm = details(); unknownTerm.addProperty("termId", "not-a-term");
        assertThrows(ApplicationPeriodService.Problem.class, () -> service.create(unknownTerm));
        assertEquals(1, ds.getApplicationPeriods().size());
        verify(data, never()).save();
    }

    @Test void cannotChangeTermAfterSubmissionOrReopenHistoricalPeriod() {
        Term other = new Term(); other.setId("t002"); other.setName("Another term"); ds.getTerms().add(other);
        JsonObject otherTerm = details(); otherTerm.addProperty("termId", "t002");
        assertEquals(409, assertThrows(ApplicationPeriodService.Problem.class,
                () -> service.update("period_test", otherTerm)).status);
        ds.getApplicationPeriods().get(0).setLegacy(true);
        assertThrows(ApplicationPeriodService.Problem.class, () -> service.changeStatus("period_test", "open"));
        assertThrows(ApplicationPeriodService.Problem.class, () -> service.update("period_test", details()));
    }

    @Test void noConfiguredPeriodFailsClosed() {
        ds.getApplicationPeriods().clear();
        assertNull(service.acceptingPeriod());
        assertFalse(service.current().get("accepting").getAsBoolean());
    }

    @Test void applicationSubmissionEnforcesWindowAndStoresPeriodId() {
        when(data.generateId(anyString(), anyList())).thenReturn("a999");
        ApplicantService applicants = new ApplicantService(data);
        ds.getApplicationPeriods().get(0).setStatus("closed");
        assertEquals(403, applicants.applyForJob("u002", "j001", "test", List.of()).get("code"));
        ds.getApplicationPeriods().get(0).setStatus("open");
        ds.getApplications().clear();
        Map<String, Object> result = applicants.applyForJob("u002", "j001", "test", List.of());
        assertEquals("period_test", ((Application) result.get("application")).getPeriodId());
        assertEquals(409, applicants.applyForJob("u002", "j001", "test", List.of()).get("code"));
    }

    @Test void previousCycleDoesNotBlockNewCycleButStaleModalDoes() {
        ApplicantService applicants = new ApplicantService(data);
        ds.getApplications().forEach(a -> a.setPeriodId("older_period"));
        assertEquals(409, applicants.applyForJob("u001", "j001", "test", List.of(), "older_period").get("code"));
        when(data.generateId(anyString(), anyList())).thenReturn("a999");
        assertTrue(applicants.applyForJob("u001", "j001", "test", List.of(), "period_test").containsKey("application"));
        assertEquals(1, applicants.getMyApplications("u001", "period_test").size());
    }

    @Test void organiserHistoryStillHonorsPositionOwnership() {
        OrganiserService organisers = new OrganiserService(data);
        JsonObject all = organisers.getPipeline("u005", null, "period_test");
        assertTrue(all.getAsJsonArray("applied").size() > 0);
        JsonObject empty = organisers.getPipeline("u005", null, "other_period");
        assertEquals(0, empty.getAsJsonArray("applied").size());
    }
}
