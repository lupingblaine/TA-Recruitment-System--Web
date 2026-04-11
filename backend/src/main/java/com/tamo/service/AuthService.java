package com.tamo.service;

import com.tamo.model.DataStore;
import com.tamo.model.Profile;
import com.tamo.model.User;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class AuthService {

    private static final List<String> VALID_ROLES = Arrays.asList("applicant", "organiser", "admin");

    private final JsonDataService dataService;

    public AuthService(JsonDataService dataService) {
        this.dataService = dataService;
    }

    /**
     * 根据邮箱和密码查找用户（不验证角色和状态）
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
     * 执行登录并更新最后登录时间
     */
    public void recordLogin(User user) {
        user.setLastLogin(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        dataService.save();
    }

    /**
     * 用户注册
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
     * 根据 ID 获取用户
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
     * 验证邮箱格式
     */
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * 验证角色值
     */
    public static boolean isValidRole(String role) {
        return role != null && VALID_ROLES.contains(role);
    }
}
