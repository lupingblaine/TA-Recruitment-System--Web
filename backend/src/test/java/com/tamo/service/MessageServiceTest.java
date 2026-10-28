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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("MessageService — Messaging System Tests")
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock private JsonDataService dataService;
    private MessageService service;
    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = TestDataFactory.createTestDataStore();
        lenient().when(dataService.getData()).thenReturn(dataStore);
        lenient().when(dataService.generateId(anyString(), anyList())).thenReturn("msg999");
        service = new MessageService(dataService);
    }

    @Nested
    @DisplayName("canMessage")
    class CanMessageTests {
        @Test
        @DisplayName("Admin can message anyone")
        void adminCanMessageAnyone() {
            assertTrue(service.canMessage("u010", "u001"));
            assertTrue(service.canMessage("u010", "u005"));
        }

        @Test
        @DisplayName("Anyone can message admin")
        void anyoneCanMessageAdmin() {
            assertTrue(service.canMessage("u001", "u010"));
            assertTrue(service.canMessage("u005", "u010"));
        }

        @Test
        @DisplayName("Applicant can message organiser of applied job")
        void applicantCanMessageRelatedOrganiser() {
            assertTrue(service.canMessage("u001", "u005"));
        }

        @Test
        @DisplayName("Organiser can message applicant who applied")
        void organiserCanMessageApplicant() {
            assertTrue(service.canMessage("u005", "u001"));
        }

        @Test
        @DisplayName("Unrelated applicant-organiser pair cannot message")
        void unrelatedCannotMessage() {
            assertFalse(service.canMessage("u002", "u006"));
        }

        @Test
        @DisplayName("Non-existent user cannot message")
        void nonExistentCannotMessage() {
            assertFalse(service.canMessage("u999", "u001"));
        }
    }

    @Nested
    @DisplayName("getContacts")
    class ContactsTests {
        @Test
        @DisplayName("Admin should see all active users as contacts")
        void adminSeesAll() {
            List<JsonObject> contacts = service.getContacts("u010");
            assertTrue(contacts.size() >= 4);
        }

        @Test
        @DisplayName("Applicant should see related organisers and admins")
        void applicantSeesRelated() {
            List<JsonObject> contacts = service.getContacts("u001");
            assertFalse(contacts.isEmpty());
            assertTrue(contacts.stream().anyMatch(c -> "u005".equals(c.get("id").getAsString())));
            assertTrue(contacts.stream().anyMatch(c -> "u010".equals(c.get("id").getAsString())));
        }

        @Test
        @DisplayName("Null userId should return empty")
        void nullUserShouldReturnEmpty() {
            List<JsonObject> contacts = service.getContacts(null);
            assertTrue(contacts.isEmpty());
        }

        @Test
        @DisplayName("Non-existent user should return empty")
        void nonExistentShouldReturnEmpty() {
            List<JsonObject> contacts = service.getContacts("u999");
            assertTrue(contacts.isEmpty());
        }
    }

    @Nested
    @DisplayName("getConversations")
    class ConversationsTests {
        @Test
        @DisplayName("Should return conversations for user u001")
        void shouldReturnConversations() {
            List<JsonObject> convs = service.getConversations("u001");
            assertEquals(1, convs.size());

            JsonObject conv = convs.get(0);
            assertEquals("u001_u005", conv.get("conversationId").getAsString());
            assertTrue(conv.has("otherUserName"));
            assertTrue(conv.has("lastMessage"));
            assertTrue(conv.has("unreadCount"));
        }

        @Test
        @DisplayName("Unread count should be correct")
        void unreadCountShouldBeCorrect() {
            List<JsonObject> convs = service.getConversations("u001");
            JsonObject conv = convs.get(0);
            assertEquals(1, conv.get("unreadCount").getAsLong());
        }
    }

    @Nested
    @DisplayName("getConversationMessages")
    class ConvMessagesTests {
        @Test
        @DisplayName("Should return messages in conversation order")
        void shouldReturnOrdered() {
            List<JsonObject> msgs = service.getConversationMessages("u001", "u001_u005");
            assertEquals(2, msgs.size());
            assertTrue(msgs.get(0).get("timestamp").getAsString()
                    .compareTo(msgs.get(1).get("timestamp").getAsString()) <= 0);
        }
    }

    @Nested
    @DisplayName("sendMessage")
    class SendTests {
        @Test
        @DisplayName("Valid text message should be sent")
        void validMessageShouldBeSent() {
            Map<String, Object> result = service.sendMessage(
                    "u001", "u005", "Test message", null, null, null, null, null);

            assertNotNull(result.get("message"));
            assertNull(result.get("error"));
            verify(dataService).save();
        }

        @Test
        @DisplayName("Empty content without attachment should fail")
        void emptyContentShouldFail() {
            Map<String, Object> result = service.sendMessage(
                    "u001", "u005", "", null, null, null, null, null);
            assertEquals(400, result.get("code"));
        }

        @Test
        @DisplayName("Missing receiver should fail")
        void missingReceiverShouldFail() {
            Map<String, Object> result = service.sendMessage(
                    "u001", "", "Hello", null, null, null, null, null);
            assertEquals(400, result.get("code"));
        }

        @Test
        @DisplayName("Sending to self should fail")
        void selfMessageShouldFail() {
            Map<String, Object> result = service.sendMessage(
                    "u001", "u001", "Hello self", null, null, null, null, null);
            assertEquals(400, result.get("code"));
        }

        @Test
        @DisplayName("Unauthorized pair should fail with 403")
        void unauthorizedShouldFail() {
            Map<String, Object> result = service.sendMessage(
                    "u002", "u006", "Unauthorized", null, null, null, null, null);
            assertEquals(403, result.get("code"));
        }

        @Test
        @DisplayName("Message with attachment only should succeed")
        void attachmentOnlyShouldSucceed() {
            Map<String, Object> result = service.sendMessage(
                    "u001", "u005", null, null, "file",
                    "/uploads/file.pdf", "file.pdf", "100KB");
            assertNotNull(result.get("message"));
        }
    }

    @Nested
    @DisplayName("editMessage")
    class EditTests {
        @Test
        @DisplayName("Sender can edit recent message")
        void senderCanEdit() {
            dataStore.getMessages().get(0).setTimestamp(
                    LocalDateTime.now().minusMinutes(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            Map<String, Object> result = service.editMessage("u001", "msg001", "Edited content");
            assertNotNull(result.get("message"));
        }

        @Test
        @DisplayName("Non-sender cannot edit")
        void nonSenderCannotEdit() {
            Map<String, Object> result = service.editMessage("u005", "msg001", "Hack");
            assertEquals(403, result.get("code"));
        }

        @Test
        @DisplayName("Edit window expired should fail")
        void expiredWindowShouldFail() {
            Map<String, Object> result = service.editMessage("u001", "msg001", "Too late");
            assertEquals(400, result.get("code"));
        }
    }

    @Nested
    @DisplayName("recallMessage")
    class RecallTests {
        @Test
        @DisplayName("Sender can recall recent message")
        void senderCanRecall() {
            dataStore.getMessages().get(0).setTimestamp(
                    LocalDateTime.now().minusSeconds(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            Map<String, Object> result = service.recallMessage("u001", "msg001");
            assertNotNull(result.get("message"));
        }

        @Test
        @DisplayName("Non-sender cannot recall")
        void nonSenderCannotRecall() {
            Map<String, Object> result = service.recallMessage("u005", "msg001");
            assertEquals(403, result.get("code"));
        }

        @Test
        @DisplayName("Already recalled message cannot be recalled again")
        void alreadyRecalledShouldFail() {
            dataStore.getMessages().get(0).setRecalled(true);
            Map<String, Object> result = service.recallMessage("u001", "msg001");
            assertEquals(400, result.get("code"));
        }
    }

    @Nested
    @DisplayName("markConversationRead & getUnreadCount")
    class ReadStatusTests {
        @Test
        @DisplayName("markConversationRead should mark all as read")
        void markReadShouldWork() {
            service.markConversationRead("u001", "u001_u005");
            verify(dataService).save();
        }

        @Test
        @DisplayName("getUnreadCount should return correct count")
        void unreadCountShouldBeCorrect() {
            int count = service.getUnreadCount("u001");
            assertEquals(1, count);
        }

        @Test
        @DisplayName("getUnreadCount for user with no messages should be 0")
        void noMessagesUnreadShouldBeZero() {
            int count = service.getUnreadCount("u003");
            assertEquals(0, count);
        }
    }

    @Nested
    @DisplayName("getUserInfo")
    class UserInfoTests {
        @Test
        @DisplayName("Should return info for applicant with profile")
        void applicantInfoShouldIncludeProfile() {
            JsonObject info = service.getUserInfo("u005", "u001");
            assertNotNull(info);
            assertEquals("Alex Johnson", info.get("name").getAsString());
            assertEquals("applicant", info.get("role").getAsString());
            assertTrue(info.has("profile"));
            assertTrue(info.has("applications"));
            assertEquals(1, info.getAsJsonArray("applications").size());
        }

        @Test
        @DisplayName("Should return info for organiser with modules")
        void organiserInfoShouldIncludeModules() {
            JsonObject info = service.getUserInfo("u001", "u005");
            assertNotNull(info);
            assertEquals("organiser", info.get("role").getAsString());
            assertTrue(info.has("modules"));
        }

        @Test
        @DisplayName("Non-existent user should return null")
        void nonExistentShouldReturnNull() {
            JsonObject info = service.getUserInfo("u001", "u999");
            assertNull(info);
        }
    }
}
