package com.tamo.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tamo.model.DataStore;
import com.tamo.model.Department;
import com.tamo.model.Job;
import com.tamo.model.LogEntry;
import com.tamo.model.Module;
import com.tamo.model.Profile;
import com.tamo.model.Term;
import com.tamo.model.User;
import com.tamo.model.WorkloadAssignment;
import com.tamo.util.JsonUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AdminService {

    private static final List<String> VALID_ROLES = Arrays.asList("applicant", "organiser", "admin");

    private final JsonDataService dataService;

    public AdminService(JsonDataService dataService) {
        this.dataService = dataService;
    }

    // ==================== Dashboard ====================

    public JsonObject getDashboard() {
        DataStore data = dataService.getData();

        long totalApplicants = data.getUsers().stream().filter(u -> "applicant".equals(u.getRole())).count();
        long activePostings = data.getJobs().stream().filter(j -> "active".equals(j.getStatus())).count();
        long totalOffers = data.getApplications().stream().filter(a -> "offer".equals(a.getStatus())).count();
        long totalApps = data.getApplications().size();
        String acceptanceRate = totalApps > 0 ? Math.round((double) totalOffers / totalApps * 100) + "%" : "0%";

        JsonObject result = new JsonObject();
        result.addProperty("totalApplicants", totalApplicants);
        result.addProperty("activePostings", activePostings);
        result.addProperty("acceptanceRate", acceptanceRate);

        JsonArray trendData = new JsonArray();
        int[] weeklyApplicants = {45, 82, 120, 250, 310, (int) totalApplicants};
        for (int i = 0; i < weeklyApplicants.length; i++) {
            JsonObject point = new JsonObject();
            point.addProperty("name", "Week " + (i + 1));
            point.addProperty("applicants", weeklyApplicants[i]);
            trendData.add(point);
        }
        result.add("trendData", trendData);

        JsonArray moduleStats = new JsonArray();
        for (Job j : data.getJobs()) {
            long appCount = data.getApplications().stream().filter(a -> a.getJobId().equals(j.getId())).count();
            JsonObject obj = new JsonObject();
            obj.addProperty("name", j.getModuleCode());
            obj.addProperty("positions", j.getPositions());
            obj.addProperty("applicants", appCount);
            moduleStats.add(obj);
        }
        result.add("moduleStats", moduleStats);

        JsonArray alerts = new JsonArray();
        for (Job j : data.getJobs()) {
            if ("active".equals(j.getStatus())) {
                long filled = data.getApplications().stream()
                        .filter(a -> a.getJobId().equals(j.getId()) && "offer".equals(a.getStatus()))
                        .count();
                if (filled < j.getPositions()) {
                    JsonObject alert = new JsonObject();
                    alert.addProperty("module", j.getModuleCode());
                    alert.addProperty("unfilled", j.getPositions() - filled);
                    alerts.add(alert);
                }
            }
        }
        result.add("systemAlerts", alerts);

        return result;
    }

    // ==================== Users ====================

    /**
     * 获取用户列表（不含密码）
     */
    public List<JsonObject> getUsers(String keyword, String role, String status) {
        DataStore data = dataService.getData();
        return data.getUsers().stream()
                .filter(u -> keyword == null || keyword.isEmpty() ||
                        u.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        u.getEmail().toLowerCase().contains(keyword.toLowerCase()))
                .filter(u -> role == null || role.isEmpty() || u.getRole().equalsIgnoreCase(role))
                .filter(u -> status == null || status.isEmpty() || u.getStatus().equalsIgnoreCase(status))
                .map(this::buildSafeUser)
                .collect(Collectors.toList());
    }

    public Map<String, Object> createUser(JsonObject body) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        String name = body.has("name") ? body.get("name").getAsString() : null;
        String email = body.has("email") ? body.get("email").getAsString() : null;
        String role = body.has("role") ? body.get("role").getAsString() : null;

        if (name == null || name.isBlank() || email == null || email.isBlank()) {
            result.put("error", "缺少必填字段");
            result.put("code", 400);
            return result;
        }

        if (role != null && !VALID_ROLES.contains(role)) {
            result.put("error", "角色值不合法");
            result.put("code", 400);
            return result;
        }

        boolean exists = data.getUsers().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
        if (exists) {
            result.put("error", "邮箱已被注册");
            result.put("code", 409);
            return result;
        }

        User user = new User();
        user.setId(dataService.generateId("u", data.getUsers()));
        user.setName(name);
        user.setEmail(email);
        user.setPassword(body.has("password") ? body.get("password").getAsString() : "password123");
        user.setRole(role != null ? role : "applicant");
        user.setDepartment(body.has("department") ? body.get("department").getAsString() : "");
        user.setStatus("active");
        user.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        data.getUsers().add(user);
        dataService.save();

        result.put("user", buildSafeUser(user));
        return result;
    }

    public Map<String, Object> updateUser(String adminId, String userId, JsonObject body) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        User user = data.getUsers().stream().filter(u -> u.getId().equals(userId)).findFirst().orElse(null);
        if (user == null) {
            result.put("error", "用户不存在");
            result.put("code", 404);
            return result;
        }

        if (userId.equals(adminId) && body.has("role")) {
            result.put("error", "不能修改自己的角色");
            result.put("code", 400);
            return result;
        }

        if (body.has("name")) user.setName(body.get("name").getAsString());
        if (body.has("role")) user.setRole(body.get("role").getAsString());
        if (body.has("department")) user.setDepartment(body.get("department").getAsString());
        if (body.has("status")) user.setStatus(body.get("status").getAsString());

        dataService.save();
        result.put("user", buildSafeUser(user));
        return result;
    }

    public Map<String, Object> deleteUser(String adminId, String userId) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        if (userId.equals(adminId)) {
            result.put("error", "不能删除自己的账户");
            result.put("code", 400);
            return result;
        }

        User user = data.getUsers().stream().filter(u -> u.getId().equals(userId)).findFirst().orElse(null);
        if (user == null) {
            result.put("error", "用户不存在");
            result.put("code", 404);
            return result;
        }

        data.getUsers().remove(user);
        dataService.save();

        result.put("success", true);
        return result;
    }

    // ==================== Modules ====================

    public List<JsonObject> getModules(String keyword) {
        DataStore data = dataService.getData();
        return data.getModules().stream()
                .filter(m -> keyword == null || keyword.isEmpty() ||
                        m.getCode().toLowerCase().contains(keyword.toLowerCase()) ||
                        m.getName().toLowerCase().contains(keyword.toLowerCase()))
                .map(m -> {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", m.getId());
                    obj.addProperty("code", m.getCode());
                    obj.addProperty("name", m.getName());
                    obj.addProperty("department", m.getDepartment());
                    obj.addProperty("organiserId", m.getOrganiserId());
                    obj.addProperty("term", m.getTerm());
                    obj.addProperty("status", m.getStatus());
                    // Enrich organiserName from users if organiserId exists
                    if (m.getOrganiserId() != null && !m.getOrganiserId().isEmpty()) {
                        User organiser = data.getUsers().stream()
                                .filter(u -> u.getId().equals(m.getOrganiserId()))
                                .findFirst().orElse(null);
                        obj.addProperty("organiserName", organiser != null ? organiser.getName() : m.getOrganiserId());
                    } else {
                        obj.addProperty("organiserName", "-");
                    }
                    return obj;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> createModule(JsonObject body) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        String code = body.has("code") ? body.get("code").getAsString() : null;
        String name = body.has("name") ? body.get("name").getAsString() : null;

        if (code == null || code.isBlank() || name == null || name.isBlank()) {
            result.put("error", "缺少必填字段");
            result.put("code_http", 400);
            return result;
        }

        boolean codeExists = data.getModules().stream().anyMatch(m -> m.getCode().equalsIgnoreCase(code));
        if (codeExists) {
            result.put("error", "模块代码已存在");
            result.put("code_http", 409);
            return result;
        }

        Module module = new Module();
        module.setId(dataService.generateId("m", data.getModules()));
        module.setCode(code);
        module.setName(name);
        module.setDepartment(body.has("department") ? body.get("department").getAsString() : "");
        module.setOrganiserId(body.has("organiserId") ? body.get("organiserId").getAsString() : "");
        module.setOrganiserName(body.has("organiserName") ? body.get("organiserName").getAsString() : "");
        module.setTerm(body.has("term") ? body.get("term").getAsString() : "");
        module.setStatus("active");

        data.getModules().add(module);
        dataService.save();

        result.put("module", module);
        return result;
    }

    public Module updateModule(String moduleId, JsonObject body) {
        DataStore data = dataService.getData();
        Module module = data.getModules().stream().filter(m -> m.getId().equals(moduleId)).findFirst().orElse(null);
        if (module == null) return null;

        if (body.has("code")) module.setCode(body.get("code").getAsString());
        if (body.has("name")) module.setName(body.get("name").getAsString());
        if (body.has("department")) module.setDepartment(body.get("department").getAsString());
        if (body.has("organiserId")) module.setOrganiserId(body.get("organiserId").getAsString());
        if (body.has("organiserName")) module.setOrganiserName(body.get("organiserName").getAsString());
        if (body.has("term")) module.setTerm(body.get("term").getAsString());
        if (body.has("status")) module.setStatus(body.get("status").getAsString());

        dataService.save();
        return module;
    }

    public Map<String, Object> deleteModule(String moduleId) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        Module module = data.getModules().stream().filter(m -> m.getId().equals(moduleId)).findFirst().orElse(null);
        if (module == null) {
            result.put("error", "模块不存在");
            result.put("code", 404);
            return result;
        }

        boolean hasJobs = data.getJobs().stream().anyMatch(j -> moduleId.equals(j.getModuleId()));
        if (hasJobs) {
            result.put("error", "该模块下存在关联岗位，无法直接删除");
            result.put("code", 400);
            return result;
        }

        data.getModules().remove(module);
        dataService.save();
        result.put("success", true);
        return result;
    }

    // ==================== Properties: Terms ====================

    public List<Term> getTerms() {
        return dataService.getData().getTerms();
    }

    public Map<String, Object> createTerm(JsonObject body) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        String name = body.has("name") ? body.get("name").getAsString() : null;

        if (name == null || name.isBlank()) {
            result.put("error", "缺少必填字段");
            result.put("code", 400);
            return result;
        }

        boolean nameExists = data.getTerms().stream().anyMatch(t -> t.getName().equalsIgnoreCase(name));
        if (nameExists) {
            result.put("error", "学期名称已存在");
            result.put("code", 409);
            return result;
        }

        String startDate = body.has("startDate") ? body.get("startDate").getAsString() : "";
        String endDate = body.has("endDate") ? body.get("endDate").getAsString() : "";

        if (!startDate.isBlank() && !endDate.isBlank()) {
            try {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                if (start.isAfter(end)) {
                    result.put("error", "开始日期不能晚于结束日期");
                    result.put("code", 400);
                    return result;
                }
            } catch (Exception ignored) {}
        }

        Term term = new Term();
        term.setId(dataService.generateId("t", data.getTerms()));
        term.setName(name);
        term.setStartDate(startDate);
        term.setEndDate(endDate);
        term.setStatus("active");

        data.getTerms().add(term);
        dataService.save();

        result.put("term", term);
        return result;
    }

    // ==================== Properties: Departments ====================

    public List<Department> getDepartments() {
        return dataService.getData().getDepartments();
    }

    public Map<String, Object> createDepartment(JsonObject body) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        String name = body.has("name") ? body.get("name").getAsString() : null;

        if (name == null || name.isBlank()) {
            result.put("error", "缺少必填字段");
            result.put("code", 400);
            return result;
        }

        boolean nameExists = data.getDepartments().stream().anyMatch(d -> d.getName().equalsIgnoreCase(name));
        if (nameExists) {
            result.put("error", "院系名称已存在");
            result.put("code", 409);
            return result;
        }

        Department dept = new Department();
        dept.setId(dataService.generateId("d", data.getDepartments()));
        dept.setName(name);
        dept.setModuleCount(0);
        dept.setStatus("active");

        data.getDepartments().add(dept);
        dataService.save();

        result.put("department", dept);
        return result;
    }

    // ==================== Logs ====================

    public List<LogEntry> getLogs(String keyword, String type) {
        DataStore data = dataService.getData();
        return data.getLogs().stream()
                .filter(l -> keyword == null || keyword.isEmpty() ||
                        l.getAction().toLowerCase().contains(keyword.toLowerCase()) ||
                        (l.getDetails() != null && l.getDetails().toLowerCase().contains(keyword.toLowerCase())))
                .filter(l -> type == null || type.isEmpty() || l.getType().equalsIgnoreCase(type))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }

    // ==================== Workload ====================

    public List<JsonObject> getWorkload(String keyword, String statusFilter) {
        DataStore data = dataService.getData();

        Map<String, List<WorkloadAssignment>> byUser = data.getWorkloadAssignments().stream()
                .filter(w -> "active".equals(w.getStatus()))
                .collect(Collectors.groupingBy(WorkloadAssignment::getTaUserId));

        List<JsonObject> results = new ArrayList<>();

        for (Map.Entry<String, List<WorkloadAssignment>> entry : byUser.entrySet()) {
            User user = data.getUsers().stream()
                    .filter(u -> u.getId().equals(entry.getKey()))
                    .findFirst().orElse(null);
            if (user == null) continue;

            Profile profile = data.getProfiles().stream()
                    .filter(p -> p.getUserId().equals(entry.getKey()))
                    .findFirst().orElse(null);

            int totalHours = entry.getValue().stream().mapToInt(WorkloadAssignment::getHoursPerWeek).sum();
            int maxHours = profile != null ? profile.getMaxWeeklyHours() : 20;

            String status;
            if (totalHours > maxHours) status = "overloaded";
            else if (totalHours == maxHours) status = "at-capacity";
            else if (totalHours >= maxHours * 0.6) status = "optimal";
            else status = "light";

            if (keyword != null && !keyword.isEmpty()) {
                if (!user.getName().toLowerCase().contains(keyword.toLowerCase())) {
                    continue;
                }
            }
            if (statusFilter != null && !statusFilter.isEmpty() && !status.equals(statusFilter)) {
                continue;
            }

            JsonArray assignments = new JsonArray();
            for (WorkloadAssignment wa : entry.getValue()) {
                JsonObject aObj = new JsonObject();
                aObj.addProperty("moduleCode", wa.getModuleCode());
                aObj.addProperty("hoursPerWeek", wa.getHoursPerWeek());
                assignments.add(aObj);
            }

            JsonObject obj = new JsonObject();
            obj.addProperty("taUserId", user.getId());
            obj.addProperty("taName", user.getName());
            obj.add("assignments", assignments);
            obj.addProperty("totalHours", totalHours);
            obj.addProperty("maxHours", maxHours);
            obj.addProperty("status", status);
            results.add(obj);
        }

        return results;
    }

    public Map<String, Object> reassignWorkload(String taUserId, JsonObject body) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        User taUser = data.getUsers().stream()
                .filter(u -> u.getId().equals(taUserId))
                .findFirst().orElse(null);
        if (taUser == null) {
            result.put("error", "TA 用户不存在");
            result.put("code", 404);
            return result;
        }

        Profile profile = data.getProfiles().stream()
                .filter(p -> p.getUserId().equals(taUserId))
                .findFirst().orElse(null);
        int maxHours = profile != null ? profile.getMaxWeeklyHours() : 20;

        if (body.has("removeModules")) {
            body.getAsJsonArray("removeModules").forEach(e -> {
                String moduleCode = e.getAsString();
                data.getWorkloadAssignments().removeIf(
                        w -> w.getTaUserId().equals(taUserId) && w.getModuleCode().equals(moduleCode));
            });
        }

        if (body.has("addModules")) {
            List<WorkloadAssignment> newAssignments = new ArrayList<>();
            body.getAsJsonArray("addModules").forEach(e -> {
                JsonObject addObj = e.getAsJsonObject();
                WorkloadAssignment wa = new WorkloadAssignment();
                wa.setId(dataService.generateId("w", data.getWorkloadAssignments()));
                wa.setTaUserId(taUserId);
                wa.setModuleCode(addObj.get("moduleCode").getAsString());
                wa.setHoursPerWeek(addObj.has("hoursPerWeek") ? addObj.get("hoursPerWeek").getAsInt() : 0);
                wa.setTerm("Fall 2026");
                wa.setStatus("active");
                newAssignments.add(wa);
            });

            int currentTotal = data.getWorkloadAssignments().stream()
                    .filter(w -> w.getTaUserId().equals(taUserId) && "active".equals(w.getStatus()))
                    .mapToInt(WorkloadAssignment::getHoursPerWeek).sum();
            int addTotal = newAssignments.stream().mapToInt(WorkloadAssignment::getHoursPerWeek).sum();

            if (currentTotal + addTotal > maxHours) {
                result.put("error", "分配后总工时超过最大工时限制");
                result.put("code", 400);
                return result;
            }

            data.getWorkloadAssignments().addAll(newAssignments);
        }

        dataService.save();
        result.put("success", true);
        return result;
    }

    private JsonObject buildSafeUser(User user) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", user.getId());
        obj.addProperty("name", user.getName());
        obj.addProperty("email", user.getEmail());
        obj.addProperty("role", user.getRole());
        obj.addProperty("department", user.getDepartment());
        obj.addProperty("status", user.getStatus());
        obj.addProperty("lastLogin", user.getLastLogin());
        obj.addProperty("createdAt", user.getCreatedAt());
        return obj;
    }
}
