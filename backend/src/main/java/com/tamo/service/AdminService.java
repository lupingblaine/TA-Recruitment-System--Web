package com.tamo.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tamo.model.Application;
import com.tamo.model.DataStore;
import com.tamo.model.Department;
import com.tamo.model.Interview;
import com.tamo.model.Job;
import com.tamo.model.LogEntry;
import com.tamo.model.Message;
import com.tamo.model.Module;
import com.tamo.model.Notification;
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

        JsonArray trendData = buildWeeklyApplicationTrend(data);
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
     * Get user list (without password)
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
            result.put("error", "Missing required fields");
            result.put("code", 400);
            return result;
        }

        if (role != null && !VALID_ROLES.contains(role)) {
            result.put("error", "Invalid role value");
            result.put("code", 400);
            return result;
        }

        boolean exists = data.getUsers().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
        if (exists) {
            result.put("error", "Email already registered");
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

        if ("applicant".equals(user.getRole())) {
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

        result.put("user", buildSafeUser(user));
        return result;
    }

    public Map<String, Object> updateUser(String adminId, String userId, JsonObject body) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        User user = data.getUsers().stream().filter(u -> u.getId().equals(userId)).findFirst().orElse(null);
        if (user == null) {
            result.put("error", "User not found");
            result.put("code", 404);
            return result;
        }

        if (userId.equals(adminId) && body.has("role")) {
            result.put("error", "Cannot modify your own role");
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
            result.put("error", "Cannot delete your own account");
            result.put("code", 400);
            return result;
        }

        User user = data.getUsers().stream().filter(u -> u.getId().equals(userId)).findFirst().orElse(null);
        if (user == null) {
            result.put("error", "User not found");
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

                    // Attach jobs published under this module (matched by moduleId or moduleCode)
                    JsonArray jobsArr = new JsonArray();
                    int activeJobs = 0;
                    for (Job j : data.getJobs()) {
                        boolean belongs = (j.getModuleId() != null && j.getModuleId().equals(m.getId()))
                                || (j.getModuleCode() != null && j.getModuleCode().equalsIgnoreCase(m.getCode()));
                        if (!belongs) continue;
                        long appCount = data.getApplications().stream()
                                .filter(a -> j.getId().equals(a.getJobId())).count();
                        JsonObject jObj = new JsonObject();
                        jObj.addProperty("id", j.getId());
                        jObj.addProperty("title", j.getTitle());
                        jObj.addProperty("moduleCode", j.getModuleCode());
                        jObj.addProperty("status", j.getStatus());
                        jObj.addProperty("type", j.getType());
                        jObj.addProperty("hoursPerWeek", j.getHoursPerWeek());
                        jObj.addProperty("positions", j.getPositions());
                        jObj.addProperty("deadline", j.getDeadline());
                        jObj.addProperty("organiserId", j.getOrganiserId());
                        jObj.addProperty("applicationCount", appCount);
                        jObj.addProperty("createdAt", j.getCreatedAt());
                        jobsArr.add(jObj);
                        if ("active".equalsIgnoreCase(j.getStatus())) activeJobs++;
                    }
                    obj.add("jobs", jobsArr);
                    obj.addProperty("jobCount", jobsArr.size());
                    obj.addProperty("activeJobCount", activeJobs);
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
            result.put("error", "Missing required fields");
            result.put("code_http", 400);
            return result;
        }

        boolean codeExists = data.getModules().stream().anyMatch(m -> m.getCode().equalsIgnoreCase(code));
        if (codeExists) {
            result.put("error", "Module code already exists");
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
            result.put("error", "Module not found");
            result.put("code", 404);
            return result;
        }

        boolean hasJobs = data.getJobs().stream().anyMatch(j -> moduleId.equals(j.getModuleId()));
        if (hasJobs) {
            result.put("error", "Cannot delete module with associated jobs");
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
            result.put("error", "Missing required fields");
            result.put("code", 400);
            return result;
        }

        boolean nameExists = data.getTerms().stream().anyMatch(t -> t.getName().equalsIgnoreCase(name));
        if (nameExists) {
            result.put("error", "Term name already exists");
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
                    result.put("error", "Start date cannot be after end date");
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
            result.put("error", "Missing required fields");
            result.put("code", 400);
            return result;
        }

        boolean nameExists = data.getDepartments().stream().anyMatch(d -> d.getName().equalsIgnoreCase(name));
        if (nameExists) {
            result.put("error", "Department name already exists");
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
                .filter(l -> {
                    if (keyword == null || keyword.isEmpty()) return true;
                    String kw = keyword.toLowerCase();
                    return l.getAction().toLowerCase().contains(kw)
                            || (l.getDetails() != null && l.getDetails().toLowerCase().contains(kw))
                            || (l.getActor() != null && l.getActor().toLowerCase().contains(kw))
                            || (l.getId() != null && l.getId().toLowerCase().contains(kw));
                })
                .filter(l -> type == null || type.isEmpty() || l.getType().equalsIgnoreCase(type))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }

    // ==================== Workload ====================

    /**
     * Workload is derived from confirmed offers: each application with status="offer" is
     * counted as an active job for its applicant, contributing the job's hoursPerWeek.
     */
    public List<JsonObject> getWorkload(String keyword, String statusFilter) {
        DataStore data = dataService.getData();

        Map<String, List<Application>> byUser = data.getApplications().stream()
                .filter(a -> "offer".equals(a.getStatus()))
                .collect(Collectors.groupingBy(Application::getApplicantId));

        List<JsonObject> results = new ArrayList<>();

        for (Map.Entry<String, List<Application>> entry : byUser.entrySet()) {
            User user = data.getUsers().stream()
                    .filter(u -> u.getId().equals(entry.getKey()))
                    .findFirst().orElse(null);
            if (user == null) continue;

            Profile profile = data.getProfiles().stream()
                    .filter(p -> p.getUserId().equals(entry.getKey()))
                    .findFirst().orElse(null);

            JsonArray assignments = new JsonArray();
            int totalHours = 0;
            for (Application app : entry.getValue()) {
                Job job = data.getJobs().stream()
                        .filter(j -> j.getId().equals(app.getJobId()))
                        .findFirst().orElse(null);
                if (job == null) continue;
                totalHours += job.getHoursPerWeek();
                JsonObject aObj = new JsonObject();
                aObj.addProperty("applicationId", app.getId());
                aObj.addProperty("jobId", job.getId());
                aObj.addProperty("jobTitle", job.getTitle());
                aObj.addProperty("moduleCode", job.getModuleCode() != null ? job.getModuleCode() : "");
                aObj.addProperty("hoursPerWeek", job.getHoursPerWeek());
                assignments.add(aObj);
            }

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

            JsonObject obj = new JsonObject();
            obj.addProperty("taUserId", user.getId());
            obj.addProperty("taName", user.getName());
            obj.addProperty("department", user.getDepartment() != null ? user.getDepartment() : "");
            obj.add("assignments", assignments);
            obj.addProperty("totalHours", totalHours);
            obj.addProperty("maxHours", maxHours);
            obj.addProperty("status", status);
            results.add(obj);
        }

        return results;
    }

    /**
     * Reassign a confirmed offer.
     *
     * Body schema:
     *   { "action": "reopen",   "applicationId": "..." }
     *   { "action": "transfer", "applicationId": "...", "targetUserId": "..." }
     *
     * - reopen   : revoke the current offer (set application status to "rejected") and
     *              reopen the job (set job status to "active") so it can be applied to again.
     * - transfer : reassign the existing offer to another applicant (only changes applicantId).
     */
    public Map<String, Object> reassignWorkload(String taUserId, JsonObject body) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        String action = body.has("action") ? body.get("action").getAsString() : "";
        String applicationId = body.has("applicationId") ? body.get("applicationId").getAsString() : null;

        if (applicationId == null || applicationId.isBlank()) {
            result.put("error", "Missing applicationId");
            result.put("code", 400);
            return result;
        }

        Application app = data.getApplications().stream()
                .filter(a -> applicationId.equals(a.getId()) && taUserId.equals(a.getApplicantId()))
                .findFirst().orElse(null);
        if (app == null) {
            result.put("error", "Offer record not found for this TA");
            result.put("code", 404);
            return result;
        }
        if (!"offer".equals(app.getStatus())) {
            result.put("error", "Selected record is not an active offer");
            result.put("code", 400);
            return result;
        }

        Job job = data.getJobs().stream()
                .filter(j -> j.getId().equals(app.getJobId()))
                .findFirst().orElse(null);
        if (job == null) {
            result.put("error", "Associated job not found");
            result.put("code", 404);
            return result;
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        switch (action) {
            case "reopen" -> {
                app.setStatus("rejected");
                app.setUpdatedAt(now);
                markTimelineRejected(app, today);
                job.setStatus("active");
                dataService.save();
                result.put("success", true);
                result.put("message", "Offer revoked and job reopened for recruitment");
                return result;
            }
            case "transfer" -> {
                String targetUserId = body.has("targetUserId") ? body.get("targetUserId").getAsString() : null;
                if (targetUserId == null || targetUserId.isBlank()) {
                    result.put("error", "Missing targetUserId");
                    result.put("code", 400);
                    return result;
                }
                if (targetUserId.equals(taUserId)) {
                    result.put("error", "Target TA is the same as the current TA");
                    result.put("code", 400);
                    return result;
                }

                User target = data.getUsers().stream()
                        .filter(u -> u.getId().equals(targetUserId))
                        .findFirst().orElse(null);
                if (target == null || !"applicant".equals(target.getRole())) {
                    result.put("error", "Target user is not a valid applicant");
                    result.put("code", 400);
                    return result;
                }
                if ("suspended".equals(target.getStatus())) {
                    result.put("error", "Target user account is suspended");
                    result.put("code", 400);
                    return result;
                }

                boolean duplicate = data.getApplications().stream()
                        .anyMatch(a -> !a.getId().equals(app.getId())
                                && job.getId().equals(a.getJobId())
                                && targetUserId.equals(a.getApplicantId()));
                if (duplicate) {
                    result.put("error", "Target TA already has an application for this job");
                    result.put("code", 409);
                    return result;
                }

                Profile targetProfile = data.getProfiles().stream()
                        .filter(p -> p.getUserId().equals(targetUserId))
                        .findFirst().orElse(null);
                int targetMax = targetProfile != null ? targetProfile.getMaxWeeklyHours() : 20;
                int targetCurrent = data.getApplications().stream()
                        .filter(a -> "offer".equals(a.getStatus()) && targetUserId.equals(a.getApplicantId()))
                        .mapToInt(a -> {
                            Job j = data.getJobs().stream()
                                    .filter(x -> x.getId().equals(a.getJobId()))
                                    .findFirst().orElse(null);
                            return j != null ? j.getHoursPerWeek() : 0;
                        }).sum();
                if (targetCurrent + job.getHoursPerWeek() > targetMax) {
                    result.put("error", "Transfer would exceed target TA's maximum weekly hours");
                    result.put("code", 400);
                    return result;
                }

                app.setApplicantId(targetUserId);
                app.setUpdatedAt(now);
                dataService.save();
                result.put("success", true);
                result.put("message", "Offer transferred to " + target.getName());
                return result;
            }
            default -> {
                result.put("error", "Invalid action; expected 'reopen' or 'transfer'");
                result.put("code", 400);
                return result;
            }
        }
    }

    private static void markTimelineRejected(Application app, String today) {
        if (app.getTimeline() == null) return;
        for (Application.TimelineEntry t : app.getTimeline()) {
            if ("offer".equals(t.getStatus())) {
                t.setCompleted(false);
                t.setPending(null);
                t.setError(Boolean.TRUE);
                t.setDate(today);
            }
        }
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

    // ==================== Data Editor (Generic CRUD) ====================

    private static final List<String> COLLECTION_NAMES = Arrays.asList(
            "users", "profiles", "modules", "jobs", "applications",
            "interviews", "notifications", "terms", "departments",
            "logs", "workloadAssignments", "messages"
    );

    private static String buildConversationId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0
                ? userId1 + "_" + userId2
                : userId2 + "_" + userId1;
    }

    public JsonArray getCollectionNames() {
        JsonArray arr = new JsonArray();
        COLLECTION_NAMES.forEach(arr::add);
        return arr;
    }

    public JsonElement getCollectionData(String collection) {
        DataStore data = dataService.getData();
        Gson gson = JsonUtil.getGson();
        return switch (collection) {
            case "users" -> gson.toJsonTree(data.getUsers());
            case "profiles" -> gson.toJsonTree(data.getProfiles());
            case "modules" -> gson.toJsonTree(data.getModules());
            case "jobs" -> gson.toJsonTree(data.getJobs());
            case "applications" -> gson.toJsonTree(data.getApplications());
            case "interviews" -> gson.toJsonTree(data.getInterviews());
            case "notifications" -> gson.toJsonTree(data.getNotifications());
            case "terms" -> gson.toJsonTree(data.getTerms());
            case "departments" -> gson.toJsonTree(data.getDepartments());
            case "logs" -> gson.toJsonTree(data.getLogs());
            case "workloadAssignments" -> gson.toJsonTree(data.getWorkloadAssignments());
            case "messages" -> gson.toJsonTree(data.getMessages() != null ? data.getMessages() : List.of());
            default -> new JsonArray();
        };
    }

    public Map<String, Object> addToCollection(String collection, JsonObject body) {
        DataStore data = dataService.getData();
        Gson gson = JsonUtil.getGson();
        Map<String, Object> result = new HashMap<>();

        // Referential integrity checks before insert
        String validationError = validateCreate(collection, body, data);
        if (validationError != null) {
            result.put("error", validationError);
            result.put("code", 400);
            return result;
        }

        try {
            Object item = switch (collection) {
                case "users" -> {
                    User u = gson.fromJson(body, User.class);
                    if (u.getId() == null || u.getId().isBlank())
                        u.setId(dataService.generateId("u", data.getUsers()));
                    if (u.getCreatedAt() == null) u.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    if (u.getStatus() == null) u.setStatus("active");
                    boolean emailExists = data.getUsers().stream().anyMatch(x -> x.getEmail().equalsIgnoreCase(u.getEmail()));
                    if (emailExists) { result.put("error", "Email already exists"); result.put("code", 409); yield null; }
                    data.getUsers().add(u);
                    yield u;
                }
                case "profiles" -> {
                    Profile p = gson.fromJson(body, Profile.class);
                    if (p.getId() == null || p.getId().isBlank())
                        p.setId(dataService.generateId("p", data.getProfiles()));
                    boolean profileExists = data.getProfiles().stream().anyMatch(x -> x.getUserId().equals(p.getUserId()));
                    if (profileExists) { result.put("error", "Profile already exists for this user"); result.put("code", 409); yield null; }
                    data.getProfiles().add(p);
                    yield p;
                }
                case "modules" -> {
                    Module m = gson.fromJson(body, Module.class);
                    if (m.getId() == null || m.getId().isBlank())
                        m.setId(dataService.generateId("m", data.getModules()));
                    boolean codeExists = data.getModules().stream().anyMatch(x -> x.getCode().equalsIgnoreCase(m.getCode()));
                    if (codeExists) { result.put("error", "Module code already exists"); result.put("code", 409); yield null; }
                    data.getModules().add(m);
                    yield m;
                }
                case "jobs" -> {
                    Job j = gson.fromJson(body, Job.class);
                    if (j.getId() == null || j.getId().isBlank())
                        j.setId(dataService.generateId("j", data.getJobs()));
                    if (j.getCreatedAt() == null) j.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    data.getJobs().add(j);
                    yield j;
                }
                case "applications" -> {
                    Application a = gson.fromJson(body, Application.class);
                    if (a.getId() == null || a.getId().isBlank())
                        a.setId(dataService.generateId("a", data.getApplications()));
                    if (a.getAppliedAt() == null) a.setAppliedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    data.getApplications().add(a);
                    yield a;
                }
                case "interviews" -> {
                    Interview i = gson.fromJson(body, Interview.class);
                    if (i.getId() == null || i.getId().isBlank())
                        i.setId(dataService.generateId("i", data.getInterviews()));
                    data.getInterviews().add(i);
                    yield i;
                }
                case "notifications" -> {
                    Notification n = gson.fromJson(body, Notification.class);
                    if (n.getId() == null || n.getId().isBlank())
                        n.setId(dataService.generateId("n", data.getNotifications()));
                    if (n.getCreatedAt() == null) n.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    data.getNotifications().add(n);
                    yield n;
                }
                case "terms" -> {
                    Term t = gson.fromJson(body, Term.class);
                    if (t.getId() == null || t.getId().isBlank())
                        t.setId(dataService.generateId("t", data.getTerms()));
                    boolean nameExists = data.getTerms().stream().anyMatch(x -> x.getName().equalsIgnoreCase(t.getName()));
                    if (nameExists) { result.put("error", "Term name already exists"); result.put("code", 409); yield null; }
                    data.getTerms().add(t);
                    yield t;
                }
                case "departments" -> {
                    Department d = gson.fromJson(body, Department.class);
                    if (d.getId() == null || d.getId().isBlank())
                        d.setId(dataService.generateId("d", data.getDepartments()));
                    boolean deptExists = data.getDepartments().stream().anyMatch(x -> x.getName().equalsIgnoreCase(d.getName()));
                    if (deptExists) { result.put("error", "Department name already exists"); result.put("code", 409); yield null; }
                    data.getDepartments().add(d);
                    yield d;
                }
                case "workloadAssignments" -> {
                    WorkloadAssignment w = gson.fromJson(body, WorkloadAssignment.class);
                    if (w.getId() == null || w.getId().isBlank())
                        w.setId(dataService.generateId("w", data.getWorkloadAssignments()));
                    data.getWorkloadAssignments().add(w);
                    yield w;
                }
                case "logs" -> {
                    LogEntry l = gson.fromJson(body, LogEntry.class);
                    if (l.getId() == null || l.getId().isBlank())
                        l.setId(dataService.generateId("log", data.getLogs()));
                    if (l.getTimestamp() == null)
                        l.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    data.getLogs().add(l);
                    yield l;
                }
                case "messages" -> {
                    Message m = gson.fromJson(body, Message.class);
                    if (data.getMessages() == null) data.setMessages(new ArrayList<>());
                    if (m.getId() == null || m.getId().isBlank())
                        m.setId(dataService.generateId("msg", data.getMessages()));
                    if (m.getConversationId() == null || m.getConversationId().isBlank()) {
                        if (m.getSenderId() != null && m.getReceiverId() != null)
                            m.setConversationId(buildConversationId(m.getSenderId(), m.getReceiverId()));
                    }
                    if (m.getTimestamp() == null)
                        m.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    data.getMessages().add(m);
                    yield m;
                }
                default -> null;
            };

            if (item == null) {
                if (!result.containsKey("error")) {
                    result.put("error", "Unsupported collection: " + collection);
                    result.put("code", 400);
                }
                return result;
            }
            dataService.save();
            result.put("item", item);
        } catch (Exception e) {
            result.put("error", "Invalid data format: " + e.getMessage());
            result.put("code", 400);
        }
        return result;
    }

    private String validateCreate(String collection, JsonObject body, DataStore data) {
        switch (collection) {
            case "jobs" -> {
                String moduleId = body.has("moduleId") ? body.get("moduleId").getAsString() : null;
                if (moduleId != null && !moduleId.isBlank()) {
                    boolean moduleExists = data.getModules().stream().anyMatch(m -> m.getId().equals(moduleId));
                    if (!moduleExists) return "Referenced module (moduleId=" + moduleId + ") does not exist";
                }
                String organiserId = body.has("organiserId") ? body.get("organiserId").getAsString() : null;
                if (organiserId != null && !organiserId.isBlank()) {
                    boolean organiserExists = data.getUsers().stream()
                            .anyMatch(u -> u.getId().equals(organiserId) && "organiser".equals(u.getRole()));
                    if (!organiserExists) return "Referenced organiser (organiserId=" + organiserId + ") does not exist or is not an organiser";
                }
            }
            case "applications" -> {
                String jobId = body.has("jobId") ? body.get("jobId").getAsString() : null;
                if (jobId == null || jobId.isBlank()) return "jobId is required";
                boolean jobExists = data.getJobs().stream().anyMatch(j -> j.getId().equals(jobId));
                if (!jobExists) return "Referenced job (jobId=" + jobId + ") does not exist";

                String applicantId = body.has("applicantId") ? body.get("applicantId").getAsString() : null;
                if (applicantId == null || applicantId.isBlank()) return "applicantId is required";
                boolean applicantExists = data.getUsers().stream()
                        .anyMatch(u -> u.getId().equals(applicantId) && "applicant".equals(u.getRole()));
                if (!applicantExists) return "Referenced applicant (applicantId=" + applicantId + ") does not exist or is not an applicant";
            }
            case "interviews" -> {
                String applicationId = body.has("applicationId") ? body.get("applicationId").getAsString() : null;
                if (applicationId != null && !applicationId.isBlank()) {
                    boolean appExists = data.getApplications().stream().anyMatch(a -> a.getId().equals(applicationId));
                    if (!appExists) return "Referenced application (applicationId=" + applicationId + ") does not exist";
                }
                String candidateId = body.has("candidateId") ? body.get("candidateId").getAsString() : null;
                if (candidateId != null && !candidateId.isBlank()) {
                    boolean userExists = data.getUsers().stream().anyMatch(u -> u.getId().equals(candidateId));
                    if (!userExists) return "Referenced candidate (candidateId=" + candidateId + ") does not exist";
                }
            }
            case "profiles" -> {
                String userId = body.has("userId") ? body.get("userId").getAsString() : null;
                if (userId == null || userId.isBlank()) return "userId is required";
                boolean userExists = data.getUsers().stream().anyMatch(u -> u.getId().equals(userId));
                if (!userExists) return "Referenced user (userId=" + userId + ") does not exist";
            }
            case "notifications" -> {
                String userId = body.has("userId") ? body.get("userId").getAsString() : null;
                if (userId != null && !userId.isBlank()) {
                    boolean userExists = data.getUsers().stream().anyMatch(u -> u.getId().equals(userId));
                    if (!userExists) return "Referenced user (userId=" + userId + ") does not exist";
                }
            }
            case "workloadAssignments" -> {
                String taUserId = body.has("taUserId") ? body.get("taUserId").getAsString() : null;
                if (taUserId != null && !taUserId.isBlank()) {
                    boolean userExists = data.getUsers().stream().anyMatch(u -> u.getId().equals(taUserId));
                    if (!userExists) return "Referenced TA user (taUserId=" + taUserId + ") does not exist";
                }
                String moduleCode = body.has("moduleCode") ? body.get("moduleCode").getAsString() : null;
                if (moduleCode != null && !moduleCode.isBlank()) {
                    boolean modExists = data.getModules().stream().anyMatch(m -> m.getCode().equals(moduleCode));
                    if (!modExists) return "Referenced module (moduleCode=" + moduleCode + ") does not exist";
                }
            }
            case "modules" -> {
                String organiserId = body.has("organiserId") ? body.get("organiserId").getAsString() : null;
                if (organiserId != null && !organiserId.isBlank()) {
                    boolean organiserExists = data.getUsers().stream()
                            .anyMatch(u -> u.getId().equals(organiserId) && "organiser".equals(u.getRole()));
                    if (!organiserExists) return "Referenced organiser (organiserId=" + organiserId + ") does not exist or is not an organiser";
                }
            }
            case "messages" -> {
                String senderId = body.has("senderId") ? body.get("senderId").getAsString() : null;
                if (senderId == null || senderId.isBlank()) return "senderId is required";
                if (data.getUsers().stream().noneMatch(u -> u.getId().equals(senderId)))
                    return "Referenced sender (senderId=" + senderId + ") does not exist";

                String receiverId = body.has("receiverId") ? body.get("receiverId").getAsString() : null;
                if (receiverId == null || receiverId.isBlank()) return "receiverId is required";
                if (data.getUsers().stream().noneMatch(u -> u.getId().equals(receiverId)))
                    return "Referenced receiver (receiverId=" + receiverId + ") does not exist";

                if (senderId.equals(receiverId)) return "senderId and receiverId must be different";

                String content = body.has("content") ? body.get("content").getAsString() : null;
                if (content == null || content.isBlank()) return "content is required";
            }
            case "logs" -> {
                String actorId = body.has("actorId") ? body.get("actorId").getAsString() : null;
                if (actorId != null && !actorId.isBlank()
                        && data.getUsers().stream().noneMatch(u -> u.getId().equals(actorId)))
                    return "Referenced actor (actorId=" + actorId + ") does not exist";
            }
            default -> {}
        }
        return null;
    }

    private String validateUpdate(String collection, JsonObject body, DataStore data, String itemId) {
        switch (collection) {
            case "jobs" -> {
                if (body.has("moduleId")) {
                    String moduleId = body.get("moduleId").getAsString();
                    if (!moduleId.isBlank() && data.getModules().stream().noneMatch(m -> m.getId().equals(moduleId)))
                        return "Referenced module (moduleId=" + moduleId + ") does not exist";
                }
                if (body.has("organiserId")) {
                    String organiserId = body.get("organiserId").getAsString();
                    if (!organiserId.isBlank() && data.getUsers().stream()
                            .noneMatch(u -> u.getId().equals(organiserId) && "organiser".equals(u.getRole())))
                        return "Referenced organiser (organiserId=" + organiserId + ") does not exist or is not an organiser";
                }
            }
            case "applications" -> {
                if (body.has("jobId")) {
                    String jobId = body.get("jobId").getAsString();
                    if (!jobId.isBlank() && data.getJobs().stream().noneMatch(j -> j.getId().equals(jobId)))
                        return "Referenced job (jobId=" + jobId + ") does not exist";
                }
                if (body.has("applicantId")) {
                    String applicantId = body.get("applicantId").getAsString();
                    if (!applicantId.isBlank() && data.getUsers().stream()
                            .noneMatch(u -> u.getId().equals(applicantId) && "applicant".equals(u.getRole())))
                        return "Referenced applicant (applicantId=" + applicantId + ") does not exist or is not an applicant";
                }
            }
            case "interviews" -> {
                if (body.has("applicationId")) {
                    String appId = body.get("applicationId").getAsString();
                    if (!appId.isBlank() && data.getApplications().stream().noneMatch(a -> a.getId().equals(appId)))
                        return "Referenced application (applicationId=" + appId + ") does not exist";
                }
                if (body.has("candidateId")) {
                    String candidateId = body.get("candidateId").getAsString();
                    if (!candidateId.isBlank() && data.getUsers().stream().noneMatch(u -> u.getId().equals(candidateId)))
                        return "Referenced candidate (candidateId=" + candidateId + ") does not exist";
                }
            }
            case "profiles" -> {
                if (body.has("userId")) {
                    String userId = body.get("userId").getAsString();
                    if (!userId.isBlank() && data.getUsers().stream().noneMatch(u -> u.getId().equals(userId)))
                        return "Referenced user (userId=" + userId + ") does not exist";
                }
            }
            case "notifications" -> {
                if (body.has("userId")) {
                    String userId = body.get("userId").getAsString();
                    if (!userId.isBlank() && data.getUsers().stream().noneMatch(u -> u.getId().equals(userId)))
                        return "Referenced user (userId=" + userId + ") does not exist";
                }
            }
            case "workloadAssignments" -> {
                if (body.has("taUserId")) {
                    String taUserId = body.get("taUserId").getAsString();
                    if (!taUserId.isBlank() && data.getUsers().stream().noneMatch(u -> u.getId().equals(taUserId)))
                        return "Referenced TA user (taUserId=" + taUserId + ") does not exist";
                }
                if (body.has("moduleCode")) {
                    String moduleCode = body.get("moduleCode").getAsString();
                    if (!moduleCode.isBlank() && data.getModules().stream().noneMatch(m -> m.getCode().equals(moduleCode)))
                        return "Referenced module (moduleCode=" + moduleCode + ") does not exist";
                }
            }
            case "modules" -> {
                if (body.has("organiserId")) {
                    String organiserId = body.get("organiserId").getAsString();
                    if (!organiserId.isBlank() && data.getUsers().stream()
                            .noneMatch(u -> u.getId().equals(organiserId) && "organiser".equals(u.getRole())))
                        return "Referenced organiser (organiserId=" + organiserId + ") does not exist or is not an organiser";
                }
            }
            case "messages" -> {
                if (body.has("senderId")) {
                    String senderId = body.get("senderId").getAsString();
                    if (!senderId.isBlank() && data.getUsers().stream().noneMatch(u -> u.getId().equals(senderId)))
                        return "Referenced sender (senderId=" + senderId + ") does not exist";
                }
                if (body.has("receiverId")) {
                    String receiverId = body.get("receiverId").getAsString();
                    if (!receiverId.isBlank() && data.getUsers().stream().noneMatch(u -> u.getId().equals(receiverId)))
                        return "Referenced receiver (receiverId=" + receiverId + ") does not exist";
                }
            }
            case "logs" -> {
                if (body.has("actorId")) {
                    String actorId = body.get("actorId").getAsString();
                    if (!actorId.isBlank() && data.getUsers().stream().noneMatch(u -> u.getId().equals(actorId)))
                        return "Referenced actor (actorId=" + actorId + ") does not exist";
                }
            }
            default -> {}
        }
        return null;
    }

    public Map<String, Object> updateCollectionItem(String collection, String itemId, JsonObject body) {
        DataStore data = dataService.getData();
        Gson gson = JsonUtil.getGson();
        Map<String, Object> result = new HashMap<>();

        // Validate foreign key references on update
        String validationError = validateUpdate(collection, body, data, itemId);
        if (validationError != null) {
            result.put("error", validationError);
            result.put("code", 400);
            return result;
        }

        try {
            Object updated = switch (collection) {
                case "users" -> {
                    User u = data.getUsers().stream().filter(x -> x.getId().equals(itemId)).findFirst().orElse(null);
                    if (u == null) { result.put("error", "Record not found"); result.put("code", 404); yield null; }
                    if (body.has("name")) u.setName(body.get("name").getAsString());
                    if (body.has("email")) u.setEmail(body.get("email").getAsString());
                    if (body.has("password")) u.setPassword(body.get("password").getAsString());
                    if (body.has("phone")) u.setPhone(body.get("phone").getAsString());
                    if (body.has("role")) u.setRole(body.get("role").getAsString());
                    if (body.has("department")) u.setDepartment(body.get("department").getAsString());
                    if (body.has("status")) u.setStatus(body.get("status").getAsString());
                    yield u;
                }
                case "profiles" -> {
                    Profile p = data.getProfiles().stream().filter(x -> x.getId().equals(itemId)).findFirst().orElse(null);
                    if (p == null) { result.put("error", "Record not found"); result.put("code", 404); yield null; }
                    if (body.has("userId")) p.setUserId(body.get("userId").getAsString());
                    if (body.has("studentId")) p.setStudentId(body.get("studentId").getAsString());
                    if (body.has("degree")) p.setDegree(body.get("degree").getAsString());
                    if (body.has("year")) p.setYear(body.get("year").getAsInt());
                    if (body.has("gpa")) p.setGpa(body.get("gpa").getAsDouble());
                    if (body.has("maxGpa")) p.setMaxGpa(body.get("maxGpa").getAsDouble());
                    if (body.has("maxWeeklyHours")) p.setMaxWeeklyHours(body.get("maxWeeklyHours").getAsInt());
                    if (body.has("currentWeeklyHours")) p.setCurrentWeeklyHours(body.get("currentWeeklyHours").getAsInt());
                    if (body.has("skills")) p.setSkills(gson.fromJson(body.get("skills"), List.class));
                    yield p;
                }
                case "modules" -> {
                    Module m = data.getModules().stream().filter(x -> x.getId().equals(itemId)).findFirst().orElse(null);
                    if (m == null) { result.put("error", "Record not found"); result.put("code", 404); yield null; }
                    if (body.has("code")) m.setCode(body.get("code").getAsString());
                    if (body.has("name")) m.setName(body.get("name").getAsString());
                    if (body.has("department")) m.setDepartment(body.get("department").getAsString());
                    if (body.has("organiserId")) m.setOrganiserId(body.get("organiserId").getAsString());
                    if (body.has("organiserName")) m.setOrganiserName(body.get("organiserName").getAsString());
                    if (body.has("term")) m.setTerm(body.get("term").getAsString());
                    if (body.has("status")) m.setStatus(body.get("status").getAsString());
                    yield m;
                }
                case "jobs" -> {
                    Job j = data.getJobs().stream().filter(x -> x.getId().equals(itemId)).findFirst().orElse(null);
                    if (j == null) { result.put("error", "Record not found"); result.put("code", 404); yield null; }
                    if (body.has("moduleId")) j.setModuleId(body.get("moduleId").getAsString());
                    if (body.has("moduleCode")) j.setModuleCode(body.get("moduleCode").getAsString());
                    if (body.has("title")) j.setTitle(body.get("title").getAsString());
                    if (body.has("description")) j.setDescription(body.get("description").getAsString());
                    if (body.has("professor")) j.setProfessor(body.get("professor").getAsString());
                    if (body.has("department")) j.setDepartment(body.get("department").getAsString());
                    if (body.has("type")) j.setType(body.get("type").getAsString());
                    if (body.has("hoursPerWeek")) j.setHoursPerWeek(body.get("hoursPerWeek").getAsInt());
                    if (body.has("positions")) j.setPositions(body.get("positions").getAsInt());
                    if (body.has("deadline")) j.setDeadline(body.get("deadline").getAsString());
                    if (body.has("status")) j.setStatus(body.get("status").getAsString());
                    if (body.has("organiserId")) j.setOrganiserId(body.get("organiserId").getAsString());
                    if (body.has("tags")) j.setTags(gson.fromJson(body.get("tags"), List.class));
                    if (body.has("requirements")) j.setRequirements(gson.fromJson(body.get("requirements"), List.class));
                    if (body.has("responsibilities")) j.setResponsibilities(gson.fromJson(body.get("responsibilities"), List.class));
                    yield j;
                }
                case "applications" -> {
                    Application a = data.getApplications().stream().filter(x -> x.getId().equals(itemId)).findFirst().orElse(null);
                    if (a == null) { result.put("error", "Record not found"); result.put("code", 404); yield null; }
                    if (body.has("jobId")) a.setJobId(body.get("jobId").getAsString());
                    if (body.has("applicantId")) a.setApplicantId(body.get("applicantId").getAsString());
                    if (body.has("status")) a.setStatus(body.get("status").getAsString());
                    if (body.has("coverLetter")) a.setCoverLetter(body.get("coverLetter").getAsString());
                    if (body.has("matchScore")) a.setMatchScore(body.get("matchScore").getAsInt());
                    if (body.has("riskFlag")) a.setRiskFlag(body.get("riskFlag").isJsonNull() ? null : body.get("riskFlag").getAsString());
                    if (body.has("aiSummary")) a.setAiSummary(body.get("aiSummary").getAsString());
                    a.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    yield a;
                }
                case "interviews" -> {
                    Interview i = data.getInterviews().stream().filter(x -> x.getId().equals(itemId)).findFirst().orElse(null);
                    if (i == null) { result.put("error", "Record not found"); result.put("code", 404); yield null; }
                    if (body.has("applicationId")) i.setApplicationId(body.get("applicationId").getAsString());
                    if (body.has("candidateId")) i.setCandidateId(body.get("candidateId").getAsString());
                    if (body.has("candidateName")) i.setCandidateName(body.get("candidateName").getAsString());
                    if (body.has("moduleCode")) i.setModuleCode(body.get("moduleCode").getAsString());
                    if (body.has("moduleName")) i.setModuleName(body.get("moduleName").getAsString());
                    if (body.has("date")) i.setDate(body.get("date").getAsString());
                    if (body.has("startTime")) i.setStartTime(body.get("startTime").getAsString());
                    if (body.has("endTime")) i.setEndTime(body.get("endTime").getAsString());
                    if (body.has("type")) i.setType(body.get("type").getAsString());
                    if (body.has("location")) i.setLocation(body.get("location").getAsString());
                    if (body.has("status")) i.setStatus(body.get("status").getAsString());
                    if (body.has("outcome")) i.setOutcome(body.get("outcome").isJsonNull() ? null : body.get("outcome").getAsString());
                    if (body.has("organiserId")) i.setOrganiserId(body.get("organiserId").getAsString());
                    yield i;
                }
                case "notifications" -> {
                    Notification n = data.getNotifications().stream().filter(x -> x.getId().equals(itemId)).findFirst().orElse(null);
                    if (n == null) { result.put("error", "Record not found"); result.put("code", 404); yield null; }
                    if (body.has("userId")) n.setUserId(body.get("userId").getAsString());
                    if (body.has("type")) n.setType(body.get("type").getAsString());
                    if (body.has("title")) n.setTitle(body.get("title").getAsString());
                    if (body.has("message")) n.setMessage(body.get("message").getAsString());
                    if (body.has("relatedId")) n.setRelatedId(body.get("relatedId").getAsString());
                    if (body.has("read")) n.setRead(body.get("read").getAsBoolean());
                    yield n;
                }
                case "terms" -> {
                    Term t = data.getTerms().stream().filter(x -> x.getId().equals(itemId)).findFirst().orElse(null);
                    if (t == null) { result.put("error", "Record not found"); result.put("code", 404); yield null; }
                    if (body.has("name")) t.setName(body.get("name").getAsString());
                    if (body.has("startDate")) t.setStartDate(body.get("startDate").getAsString());
                    if (body.has("endDate")) t.setEndDate(body.get("endDate").getAsString());
                    if (body.has("status")) t.setStatus(body.get("status").getAsString());
                    yield t;
                }
                case "departments" -> {
                    Department d = data.getDepartments().stream().filter(x -> x.getId().equals(itemId)).findFirst().orElse(null);
                    if (d == null) { result.put("error", "Record not found"); result.put("code", 404); yield null; }
                    if (body.has("name")) d.setName(body.get("name").getAsString());
                    if (body.has("moduleCount")) d.setModuleCount(body.get("moduleCount").getAsInt());
                    if (body.has("status")) d.setStatus(body.get("status").getAsString());
                    yield d;
                }
                case "logs" -> {
                    LogEntry l = data.getLogs().stream().filter(x -> x.getId().equals(itemId)).findFirst().orElse(null);
                    if (l == null) { result.put("error", "Record not found"); result.put("code", 404); yield null; }
                    if (body.has("action")) l.setAction(body.get("action").getAsString());
                    if (body.has("type")) l.setType(body.get("type").getAsString());
                    if (body.has("actor")) l.setActor(body.get("actor").getAsString());
                    if (body.has("details")) l.setDetails(body.get("details").getAsString());
                    yield l;
                }
                case "workloadAssignments" -> {
                    WorkloadAssignment w = data.getWorkloadAssignments().stream().filter(x -> x.getId().equals(itemId)).findFirst().orElse(null);
                    if (w == null) { result.put("error", "Record not found"); result.put("code", 404); yield null; }
                    if (body.has("taUserId")) w.setTaUserId(body.get("taUserId").getAsString());
                    if (body.has("moduleCode")) w.setModuleCode(body.get("moduleCode").getAsString());
                    if (body.has("hoursPerWeek")) w.setHoursPerWeek(body.get("hoursPerWeek").getAsInt());
                    if (body.has("term")) w.setTerm(body.get("term").getAsString());
                    if (body.has("status")) w.setStatus(body.get("status").getAsString());
                    yield w;
                }
                case "messages" -> {
                    if (data.getMessages() == null) { result.put("error", "Record not found"); result.put("code", 404); yield null; }
                    Message m = data.getMessages().stream().filter(x -> x.getId().equals(itemId)).findFirst().orElse(null);
                    if (m == null) { result.put("error", "Record not found"); result.put("code", 404); yield null; }
                    if (body.has("conversationId")) m.setConversationId(body.get("conversationId").getAsString());
                    if (body.has("senderId")) m.setSenderId(body.get("senderId").getAsString());
                    if (body.has("receiverId")) m.setReceiverId(body.get("receiverId").getAsString());
                    if (body.has("content")) m.setContent(body.get("content").getAsString());
                    if (body.has("timestamp")) m.setTimestamp(body.get("timestamp").getAsString());
                    if (body.has("read")) m.setRead(body.get("read").getAsBoolean());
                    if (body.has("replyToId")) m.setReplyToId(body.get("replyToId").isJsonNull() ? null : body.get("replyToId").getAsString());
                    if (body.has("recalled")) m.setRecalled(body.get("recalled").getAsBoolean());
                    if (body.has("edited")) m.setEdited(body.get("edited").getAsBoolean());
                    if (body.has("editedAt")) m.setEditedAt(body.get("editedAt").isJsonNull() ? null : body.get("editedAt").getAsString());
                    if (body.has("messageType")) m.setMessageType(body.get("messageType").getAsString());
                    if (body.has("attachmentUrl")) m.setAttachmentUrl(body.get("attachmentUrl").isJsonNull() ? null : body.get("attachmentUrl").getAsString());
                    if (body.has("attachmentName")) m.setAttachmentName(body.get("attachmentName").isJsonNull() ? null : body.get("attachmentName").getAsString());
                    if (body.has("attachmentSize")) m.setAttachmentSize(body.get("attachmentSize").isJsonNull() ? null : body.get("attachmentSize").getAsString());
                    if ((m.getConversationId() == null || m.getConversationId().isBlank())
                            && m.getSenderId() != null && m.getReceiverId() != null) {
                        m.setConversationId(buildConversationId(m.getSenderId(), m.getReceiverId()));
                    }
                    yield m;
                }
                default -> null;
            };

            if (updated == null && !result.containsKey("error")) {
                result.put("error", "Unsupported collection: " + collection);
                result.put("code", 400);
                return result;
            }
            if (result.containsKey("error")) return result;
            dataService.save();
            result.put("item", updated);
        } catch (Exception e) {
            result.put("error", "Update failed: " + e.getMessage());
            result.put("code", 400);
        }
        return result;
    }

    public Map<String, Object> deleteFromCollection(String collection, String itemId) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        switch (collection) {
            case "users" -> {
                User user = data.getUsers().stream().filter(x -> x.getId().equals(itemId)).findFirst().orElse(null);
                if (user == null) { result.put("error", "Record not found"); result.put("code", 404); break; }
                data.getUsers().remove(user);
                // Cascade: remove profile, jobs (as organiser), applications, interviews, notifications, workload
                data.getProfiles().removeIf(p -> itemId.equals(p.getUserId()));
                List<String> userJobIds = data.getJobs().stream()
                        .filter(j -> itemId.equals(j.getOrganiserId())).map(Job::getId).collect(Collectors.toList());
                data.getJobs().removeIf(j -> itemId.equals(j.getOrganiserId()));
                data.getApplications().removeIf(a -> itemId.equals(a.getApplicantId()) || userJobIds.contains(a.getJobId()));
                data.getInterviews().removeIf(i -> itemId.equals(i.getCandidateId()) || itemId.equals(i.getOrganiserId()));
                data.getNotifications().removeIf(n -> itemId.equals(n.getUserId()));
                data.getWorkloadAssignments().removeIf(w -> itemId.equals(w.getTaUserId()));
                if (data.getMessages() != null) {
                    data.getMessages().removeIf(m -> itemId.equals(m.getSenderId()) || itemId.equals(m.getReceiverId()));
                }
                result.put("success", true);
            }
            case "modules" -> {
                Module module = data.getModules().stream().filter(x -> x.getId().equals(itemId)).findFirst().orElse(null);
                if (module == null) { result.put("error", "Record not found"); result.put("code", 404); break; }
                String moduleCode = module.getCode();
                data.getModules().remove(module);
                // Cascade: remove jobs under this module, their applications, and interviews
                List<String> jobIds = data.getJobs().stream()
                        .filter(j -> itemId.equals(j.getModuleId()) || moduleCode.equals(j.getModuleCode()))
                        .map(Job::getId).collect(Collectors.toList());
                data.getJobs().removeIf(j -> itemId.equals(j.getModuleId()) || moduleCode.equals(j.getModuleCode()));
                List<String> appIds = data.getApplications().stream()
                        .filter(a -> jobIds.contains(a.getJobId())).map(Application::getId).collect(Collectors.toList());
                data.getApplications().removeIf(a -> jobIds.contains(a.getJobId()));
                data.getInterviews().removeIf(i -> appIds.contains(i.getApplicationId()));
                data.getWorkloadAssignments().removeIf(w -> moduleCode.equals(w.getModuleCode()));
                result.put("success", true);
            }
            case "jobs" -> {
                Job job = data.getJobs().stream().filter(x -> x.getId().equals(itemId)).findFirst().orElse(null);
                if (job == null) { result.put("error", "Record not found"); result.put("code", 404); break; }
                data.getJobs().remove(job);
                // Cascade: remove applications for this job and their interviews
                List<String> appIds = data.getApplications().stream()
                        .filter(a -> itemId.equals(a.getJobId())).map(Application::getId).collect(Collectors.toList());
                data.getApplications().removeIf(a -> itemId.equals(a.getJobId()));
                data.getInterviews().removeIf(i -> appIds.contains(i.getApplicationId()));
                result.put("success", true);
            }
            case "applications" -> {
                boolean removed = data.getApplications().removeIf(x -> x.getId().equals(itemId));
                if (!removed) { result.put("error", "Record not found"); result.put("code", 404); break; }
                // Cascade: remove interviews for this application
                data.getInterviews().removeIf(i -> itemId.equals(i.getApplicationId()));
                result.put("success", true);
            }
            case "profiles" -> {
                boolean removed = data.getProfiles().removeIf(x -> x.getId().equals(itemId));
                if (!removed) { result.put("error", "Record not found"); result.put("code", 404); break; }
                result.put("success", true);
            }
            case "interviews" -> {
                boolean removed = data.getInterviews().removeIf(x -> x.getId().equals(itemId));
                if (!removed) { result.put("error", "Record not found"); result.put("code", 404); break; }
                result.put("success", true);
            }
            case "notifications" -> {
                boolean removed = data.getNotifications().removeIf(x -> x.getId().equals(itemId));
                if (!removed) { result.put("error", "Record not found"); result.put("code", 404); break; }
                result.put("success", true);
            }
            case "terms" -> {
                boolean removed = data.getTerms().removeIf(x -> x.getId().equals(itemId));
                if (!removed) { result.put("error", "Record not found"); result.put("code", 404); break; }
                result.put("success", true);
            }
            case "departments" -> {
                boolean removed = data.getDepartments().removeIf(x -> x.getId().equals(itemId));
                if (!removed) { result.put("error", "Record not found"); result.put("code", 404); break; }
                result.put("success", true);
            }
            case "logs" -> {
                boolean removed = data.getLogs().removeIf(x -> x.getId().equals(itemId));
                if (!removed) { result.put("error", "Record not found"); result.put("code", 404); break; }
                result.put("success", true);
            }
            case "workloadAssignments" -> {
                boolean removed = data.getWorkloadAssignments().removeIf(x -> x.getId().equals(itemId));
                if (!removed) { result.put("error", "Record not found"); result.put("code", 404); break; }
                result.put("success", true);
            }
            case "messages" -> {
                if (data.getMessages() == null) { result.put("error", "Record not found"); result.put("code", 404); break; }
                boolean removed = data.getMessages().removeIf(x -> x.getId().equals(itemId));
                if (!removed) { result.put("error", "Record not found"); result.put("code", 404); break; }
                result.put("success", true);
            }
            default -> { result.put("error", "Unsupported collection"); result.put("code", 400); }
        }

        if (result.containsKey("success")) {
            dataService.save();
        }
        return result;
    }

    /**
     * Build weekly application trend from real appliedAt dates.
     * Groups applications by ISO week and returns the most recent 6 weeks.
     */
    private JsonArray buildWeeklyApplicationTrend(DataStore data) {
        JsonArray trendData = new JsonArray();

        Map<LocalDate, Long> weekCounts = new TreeMap<>();

        for (com.tamo.model.Application app : data.getApplications()) {
            String appliedAt = app.getAppliedAt();
            if (appliedAt == null || appliedAt.isBlank()) continue;
            try {
                LocalDate date = LocalDate.parse(appliedAt.substring(0, 10));
                LocalDate weekStart = date.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                weekCounts.merge(weekStart, 1L, Long::sum);
            } catch (Exception ignored) {}
        }

        if (weekCounts.isEmpty()) {
            JsonObject point = new JsonObject();
            point.addProperty("name", "No data");
            point.addProperty("applicants", 0);
            trendData.add(point);
            return trendData;
        }

        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MM/dd");
        List<Map.Entry<LocalDate, Long>> entries = new ArrayList<>(weekCounts.entrySet());
        int start = Math.max(0, entries.size() - 6);
        for (int i = start; i < entries.size(); i++) {
            Map.Entry<LocalDate, Long> e = entries.get(i);
            JsonObject point = new JsonObject();
            point.addProperty("name", "Week " + e.getKey().format(labelFmt));
            point.addProperty("applicants", e.getValue());
            trendData.add(point);
        }

        return trendData;
    }
}
