package com.tamo.service;

import com.tamo.model.DataStore;
import com.tamo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthService — Authentication & Registration Tests")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private JsonDataService dataService;
    private AuthService authService;
    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = TestDataFactory.createTestDataStore();
        lenient().when(dataService.getData()).thenReturn(dataStore);
        lenient().when(dataService.generateId(anyString(), anyList())).thenReturn("u999");
        authService = new AuthService(dataService);
    }

    @Nested
    @DisplayName("findByEmailAndPassword")
    class LoginTests {
        @Test
        @DisplayName("Valid credentials should return user")
        void validCredentialsShouldReturnUser() {
            User user = authService.findByEmailAndPassword("alex@university.edu", "pass1");
            assertNotNull(user);
            assertEquals("u001", user.getId());
            assertEquals("Alex Johnson", user.getName());
        }

        @Test
        @DisplayName("Wrong password should return null")
        void wrongPasswordShouldReturnNull() {
            User user = authService.findByEmailAndPassword("alex@university.edu", "wrong");
            assertNull(user);
        }

        @Test
        @DisplayName("Non-existent email should return null")
        void nonExistentEmailShouldReturnNull() {
            User user = authService.findByEmailAndPassword("nobody@university.edu", "pass");
            assertNull(user);
        }

        @Test
        @DisplayName("Case-insensitive email matching")
        void emailShouldBeCaseInsensitive() {
            User user = authService.findByEmailAndPassword("ALEX@University.edu", "pass1");
            assertNotNull(user);
            assertEquals("u001", user.getId());
        }
    }

    @Nested
    @DisplayName("recordLogin")
    class RecordLoginTests {
        @Test
        @DisplayName("Should update lastLogin and save")
        void shouldUpdateLastLoginAndSave() {
            User user = dataStore.getUsers().get(0);
            assertNull(user.getLastLogin());

            authService.recordLogin(user);

            assertNotNull(user.getLastLogin());
            verify(dataService).save();
        }
    }

    @Nested
    @DisplayName("signup")
    class SignupTests {
        @Test
        @DisplayName("New applicant should be created with profile")
        void newApplicantShouldBeCreated() {
            when(dataService.generateId(eq("u"), anyList())).thenReturn("u099");
            when(dataService.generateId(eq("p"), anyList())).thenReturn("p099");

            User result = authService.signup("New User", "new@university.edu", "newpass", "applicant");

            assertNotNull(result);
            assertEquals("u099", result.getId());
            assertEquals("New User", result.getName());
            assertEquals("new@university.edu", result.getEmail());
            assertEquals("applicant", result.getRole());
            assertEquals("active", result.getStatus());

            assertEquals(7, dataStore.getUsers().size());
            assertEquals(3, dataStore.getProfiles().size());
            verify(dataService).save();
        }

        @Test
        @DisplayName("New organiser should be created without profile")
        void newOrganiserShouldNotHaveProfile() {
            int profilesBefore = dataStore.getProfiles().size();
            User result = authService.signup("Prof. New", "prof@university.edu", "pass", "organiser");

            assertNotNull(result);
            assertEquals("organiser", result.getRole());
            assertEquals(profilesBefore, dataStore.getProfiles().size());
        }

        @Test
        @DisplayName("Duplicate email should return null")
        void duplicateEmailShouldReturnNull() {
            User result = authService.signup("Dup", "alex@university.edu", "pass", "applicant");
            assertNull(result);
        }

        @Test
        @DisplayName("Duplicate email should be case insensitive")
        void duplicateEmailCaseInsensitive() {
            User result = authService.signup("Dup", "ALEX@University.edu", "pass", "applicant");
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserByIdTests {
        @Test
        @DisplayName("Existing ID should return user")
        void existingIdShouldReturnUser() {
            User user = authService.getUserById("u001");
            assertNotNull(user);
            assertEquals("Alex Johnson", user.getName());
        }

        @Test
        @DisplayName("Non-existent ID should return null")
        void nonExistentIdShouldReturnNull() {
            User user = authService.getUserById("u999");
            assertNull(user);
        }
    }

    @Nested
    @DisplayName("Static Validation")
    class ValidationTests {
        @Test
        @DisplayName("Valid email formats should pass")
        void validEmailsShouldPass() {
            assertTrue(AuthService.isValidEmail("user@example.com"));
            assertTrue(AuthService.isValidEmail("test.user@university.edu"));
            assertTrue(AuthService.isValidEmail("a+b@domain.co.uk"));
        }

        @Test
        @DisplayName("Invalid email formats should fail")
        void invalidEmailsShouldFail() {
            assertFalse(AuthService.isValidEmail(null));
            assertFalse(AuthService.isValidEmail(""));
            assertFalse(AuthService.isValidEmail("notanemail"));
            assertFalse(AuthService.isValidEmail("@domain.com"));
            assertFalse(AuthService.isValidEmail("user@"));
        }

        @Test
        @DisplayName("Valid roles should pass")
        void validRolesShouldPass() {
            assertTrue(AuthService.isValidRole("applicant"));
            assertTrue(AuthService.isValidRole("organiser"));
            assertTrue(AuthService.isValidRole("admin"));
        }

        @Test
        @DisplayName("Invalid roles should fail")
        void invalidRolesShouldFail() {
            assertFalse(AuthService.isValidRole(null));
            assertFalse(AuthService.isValidRole(""));
            assertFalse(AuthService.isValidRole("student"));
            assertFalse(AuthService.isValidRole("ADMIN"));
        }
    }
}
