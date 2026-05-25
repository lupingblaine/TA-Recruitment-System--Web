package com.tamo.service;

import com.tamo.model.DataStore;
import com.tamo.model.Profile;
import com.tamo.model.User;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of AuthService.
 * Provides necessary operations and logic for the system.
 */
public class AuthService {

    private static final List<String> VALID_ROLES = Arrays.asList("applicant", "organiser", "admin");

    private final JsonDataService dataService;

    /**
     * Constructs a new AuthService instance.
     */
    /**
     * Constructs a new AuthService instance.
     */
    public AuthService(JsonDataService dataService) {
        this.dataService = dataService;
    }

    /**
     * Find user by email and password (without role validation)

     */
    /**
     * Executes the findByEmailAndPassword operation.
     * @param email the email argument
     * @param password the password argument
     * @return the resulting object or status
     */
    /**
     * Executes the findByEmailAndPassword operation.
     * @param email the email argument
     * @param password the password argument
     * @return the resulting object or status
     */
    public User findByEmailAndPassword(String email, String password) {
        DataStore data = dataService.getData();
        for (User user : data.getUsers()) {
            if (user.getEmail().equalsIgnoreCase(email) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Execute login and update last login time

     */
    /**
     * Executes the recordLogin operation.
     * @param user the user argument
     */
    /**
     * Executes the recordLogin operation.
     * @param user the user argument
     */
    public void recordLogin(User user) {
        user.setLastLogin(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        dataService.save();
    }

    /**
     * User registration

     */
    /**
     * Executes the signup operation.
     * @param name the name argument
     * @param email the email argument
     * @param password the password argument
     * @param role the role argument
     * @return the resulting object or status
     */
    /**
     * Executes the signup operation.
     * @param name the name argument
     * @param email the email argument
     * @param password the password argument
     * @param role the role argument
     * @return the resulting object or status
     */
    public User signup(String name, String email, String password, String role) {
        DataStore data = dataService.getData();

        for (User u : data.getUsers()) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return null;
            }
        }

        User user = new User();
        user.setId(dataService.generateId("u", data.getUsers()));
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setStatus("active");
        user.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        user.setLastLogin(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        data.getUsers().add(user);

        if ("applicant".equals(role)) {
            Profile profile = new Profile();
            profile.setId(dataService.generateId("p", data.getProfiles()));
            profile.setUserId(user.getId());
            profile.setStudentId("");
            profile.setDegree("");
            profile.setYear(0);
            profile.setGpa(0);
            profile.setMaxGpa(4.0);
            profile.setProfileCompletion(0);
            profile.setSkills(new ArrayList<>());
            profile.setCoursework(new ArrayList<>());
            profile.setExperience(new ArrayList<>());
            profile.setDocuments(new ArrayList<>());
            profile.setMaxWeeklyHours(20);
            profile.setCurrentWeeklyHours(0);
            data.getProfiles().add(profile);
        }

        dataService.save();
        return user;
    }

    /**
     * Get user by ID

     */
    /**
     * Executes the getUserById operation.
     * @param userId the userId argument
     * @return the resulting object or status
     */
    /**
     * Executes the getUserById operation.
     * @param userId the userId argument
     * @return the resulting object or status
     */
    public User getUserById(String userId) {
        DataStore data = dataService.getData();
        for (User user : data.getUsers()) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Validate email format

     */
    /**
     * Executes the isValidEmail operation.
     * @param email the email argument
     * @return the resulting object or status
     */
    /**
     * Executes the isValidEmail operation.
     * @param email the email argument
     * @return the resulting object or status
     */
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * Validate role value

     */
    /**
     * Executes the isValidRole operation.
     * @param role the role argument
     * @return the resulting object or status
     */
    /**
     * Executes the isValidRole operation.
     * @param role the role argument
     * @return the resulting object or status
     */
    public static boolean isValidRole(String role) {
        return role != null && VALID_ROLES.contains(role);
    }
}
