package com.tamo.service;

import com.google.gson.JsonObject;
import com.tamo.model.DataStore;
import com.tamo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageAccessRegressionTest {
    private DataStore data;
    private MessageService service;

    @BeforeEach void setUp() {
        data = TestDataFactory.createTestDataStore();
        JsonDataService store = mock(JsonDataService.class);
        when(store.getData()).thenReturn(data);
        service = new MessageService(store);
    }
    private User user(String id) { return data.getUsers().stream().filter(u -> id.equals(u.getId())).findFirst().orElseThrow(); }
    private void forbidden(String requester, String target) {
        assertEquals(403, assertThrows(QualificationService.Problem.class,
                () -> service.getUserInfo(requester, target)).status);
    }
    @Test void applicantsCannotInspectOtherApplicantsEvenWithForgedPermissions() {
        user("u001").setPermissions(List.of("review.read"));
        forbidden("u001", "u002");
    }
    @Test void unrelatedOrganiserCannotInspectApplicant() { forbidden("u006", "u002"); }
    @Test void missingAndInactiveRequestersCannotInspectUsers() {
        forbidden("missing", "u001");
        forbidden("u003", "u001");
        user("u005").setStatus("suspended");
        forbidden("u005", "u001");
        assertFalse(service.canMessage("u005", "u001"));
        assertTrue(service.getContacts("u005").isEmpty());
        assertTrue(service.getConversations("u005").isEmpty());
    }
    @Test void teacherOnlySeesApplicationsForTheirCurrentCourses() {
        JsonObject info = service.getUserInfo("u005", "u001");
        assertTrue(info.has("profile"));
        assertEquals(1, info.getAsJsonArray("applications").size());
        assertEquals("EBU6304", info.getAsJsonArray("applications").get(0).getAsJsonObject().get("moduleCode").getAsString());
    }
    @Test void revokedReadCannotUseAnyMessageReadPathToInspectStudent() {
        data.getMessages().get(0).setRead(false);
        assertEquals(1, service.getUnreadCount("u005"));
        user("u005").setPermissions(List.of("review.note"));
        forbidden("u005", "u001");
        assertFalse(service.canMessage("u005", "u001"));
        assertFalse(service.canMessage("u001", "u005"));
        assertFalse(service.getContacts("u005").stream().anyMatch(c -> "u001".equals(c.get("id").getAsString())));
        assertTrue(service.getConversations("u005").isEmpty());
        assertTrue(service.getConversationMessages("u005", "u001_u005").isEmpty());
        assertEquals(0, service.getUnreadCount("u005"));
        service.markConversationRead("u005", "u001_u005");
        assertFalse(data.getMessages().get(0).isRead());
    }
    @Test void reassignmentRevokesStaleJobOwnerAndAuthorizesCurrentCourseOwner() {
        data.getModules().get(0).setOrganiserId("u006");
        forbidden("u005", "u001");
        assertFalse(service.canMessage("u005", "u001"));
        assertFalse(service.canMessage("u001", "u005"));
        assertTrue(service.canMessage("u006", "u001"));
        assertTrue(service.canMessage("u001", "u006"));
        assertFalse(service.getContacts("u001").stream().anyMatch(c -> "u005".equals(c.get("id").getAsString())));
        assertFalse(service.getContacts("u005").stream().anyMatch(c -> "u001".equals(c.get("id").getAsString())));
        assertTrue(service.getConversations("u005").isEmpty());
        assertTrue(service.getConversationMessages("u005", "u001_u005").isEmpty());
    }
    @Test void studentSeesOnlyRelatedTeacherCoursesAndContactFields() {
        data.getModules().get(1).setOrganiserId("u005");
        // Bob only applied to the first course; the second must not be exposed.
        JsonObject info = service.getUserInfo("u002", "u005");
        assertEquals(1, info.getAsJsonArray("modules").size());
        assertTrue(info.has("email"));
        assertFalse(info.has("profile"));
        assertFalse(info.has("applications"));
    }
    @Test void selfAndAdminKeepFullProfileAccessAndMissingTargetRemainsNull() {
        assertEquals(2, service.getUserInfo("u001", "u001").getAsJsonArray("applications").size());
        assertEquals(2, service.getUserInfo("u010", "u001").getAsJsonArray("applications").size());
        assertNull(service.getUserInfo("u001", "missing"));
    }
    @Test void inactiveTargetIsNotContactableAndUnrelatedStaffCannotInspect() {
        assertFalse(service.canMessage("u010", "u003"));
        user("u006").setRole("reviewer");
        user("u006").setCourseIds(List.of("m001"));
        forbidden("u006", "u001");
    }
}
