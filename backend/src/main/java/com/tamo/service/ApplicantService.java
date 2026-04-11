package com.tamo.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tamo.model.Application;
import com.tamo.model.DataStore;
import com.tamo.model.Job;
import com.tamo.model.Profile;
import com.tamo.model.User;
import com.tamo.util.JsonUtil;

import com.tamo.model.Interview;
import com.tamo.model.Notification;
import com.tamo.model.WorkloadAssignment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ApplicantService {

    private final JsonDataService dataService;

    public ApplicantService(JsonDataService dataService) {
        this.dataService = dataService;
    }

    public JsonObject getDashboard(String userId) {
        DataStore data = dataService.getData();
        Profile profile = findProfileByUserId(data, userId);

        List<Application> myApps = data.getApplications().stream()
                .filter(a -> a.getApplicantId().equals(userId))
                .collect(Collectors.toList());

        long activeCount = myApps.stream()
                .filter(a -> !"rejected".equals(a.getStatus()) && !"offer".equals(a.getStatus()))
                .count();

        long interviewCount = myApps.stream()
                .filter(a -> "interview".equals(a.getStatus()))
                .count();

        int profileCompletion = profile != null ? profile.getProfileCompletion() : 0;

        JsonObject result = new JsonObject();
        result.addProperty("activeApplications", activeCount);
        result.addProperty("interviewCount", interviewCount);
        result.addProperty("profileCompletion", profileCompletion);

        JsonArray recentApps = new JsonArray();
        myApps.stream()
                .sorted((a, b) -> b.getAppliedAt().compareTo(a.getAppliedAt()))
                .limit(5)
                .forEach(app -> {
                    Job job = findJobById(data, app.getJobId());
                    JsonObject appObj = new JsonObject();
                    appObj.addProperty("id", app.getId());
                    appObj.addProperty("jobId", app.getJobId());
                    appObj.addProperty("status", app.getStatus());
                    appObj.addProperty("appliedAt", app.getAppliedAt());
                    enrichApplicationForApplicantApi(appObj, job, app.getJobId(), app.getAppliedAt());
                    recentApps.add(appObj);
                });
        result.add("recentApplications", recentApps);

        // ── Workload: computed from real WorkloadAssignment data ──
        {
            List<WorkloadAssignment> assignments = (data.getWorkloadAssignments() != null
                    ? data.getWorkloadAssignments() : Collections.<WorkloadAssignment>emptyList())
                    .stream()
                    .filter(w -> w.getTaUserId().equals(userId) && "active".equals(w.getStatus()))
                    .collect(Collectors.toList());

            int totalHours = assignments.stream().mapToInt(WorkloadAssignment::getHoursPerWeek).sum();
            int maxHours = profile != null ? profile.getMaxWeeklyHours() : 20;
            int remaining = maxHours - totalHours;

            String loadStatus;
            if (totalHours >= maxHours) loadStatus = "Overloaded";
            else if (totalHours >= maxHours * 0.8) loadStatus = "Near Capacity";
            else loadStatus = "Healthy Load";

            JsonObject workload = new JsonObject();
            workload.addProperty("current", totalHours);
            workload.addProperty("max", maxHours);
            workload.addProperty("remaining", Math.max(remaining, 0));
            workload.addProperty("status", loadStatus);

            JsonArray assignmentList = new JsonArray();
            assignments.forEach(w -> {
                JsonObject wo = new JsonObject();
                wo.addProperty("moduleCode", w.getModuleCode());
                wo.addProperty("hoursPerWeek", w.getHoursPerWeek());
                wo.addProperty("term", w.getTerm());
                assignmentList.add(wo);
            });
            workload.add("assignments", assignmentList);
            result.add("workload", workload);
        }

        // ── Schedule: upcoming interviews for this TA ──
        {
            List<Interview> interviews = (data.getInterviews() != null
                    ? data.getInterviews() : Collections.<Interview>emptyList())
                    .stream()
                    .filter(iv -> userId.equals(iv.getCandidateId()))
                    .filter(iv -> !"cancelled".equals(iv.getStatus()) && !"completed".equals(iv.getStatus()))
                    .sorted(Comparator.comparing(Interview::getDate, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());

            JsonArray schedule = new JsonArray();
            interviews.forEach(iv -> {
                JsonObject entry = new JsonObject();
                entry.addProperty("type", "interview");
                entry.addProperty("moduleCode", iv.getModuleCode());
                entry.addProperty("moduleName", iv.getModuleName());
                entry.addProperty("date", iv.getDate());
                entry.addProperty("startTime", iv.getStartTime());
                entry.addProperty("endTime", iv.getEndTime());
                entry.addProperty("interviewType", iv.getType());
                entry.addProperty("location", iv.getLocation());
                schedule.add(entry);
            });

            // Also add TA duty slots from workload assignments as recurring schedule items
            List<WorkloadAssignment> activeAssignments = (data.getWorkloadAssignments() != null
                    ? data.getWorkloadAssignments() : Collections.<WorkloadAssignment>emptyList())
                    .stream()
                    .filter(w -> w.getTaUserId().equals(userId) && "active".equals(w.getStatus()))
                    .collect(Collectors.toList());
            activeAssignments.forEach(w -> {
                JsonObject entry = new JsonObject();
                entry.addProperty("type", "duty");
                entry.addProperty("moduleCode", w.getModuleCode());
                entry.addProperty("hoursPerWeek", w.getHoursPerWeek());
                entry.addProperty("term", w.getTerm());
                schedule.add(entry);
            });

            result.add("schedule", schedule);
        }

        JsonArray deadlines = new JsonArray();
        data.getJobs().stream()
                .filter(j -> "active".equals(j.getStatus()))
                .sorted((a, b) -> a.getDeadline().compareTo(b.getDeadline()))
                .limit(3)
                .forEach(j -> {
                    JsonObject d = new JsonObject();
                    d.addProperty("jobId", j.getId());
                    d.addProperty("title", j.getTitle());
                    d.addProperty("moduleCode", j.getModuleCode() != null ? j.getModuleCode() : "");
                    d.addProperty("deadline", j.getDeadline());
                    deadlines.add(d);
                });
        result.add("upcomingDeadlines", deadlines);

        // ── Notifications ──
        {
            List<Notification> notifications = (data.getNotifications() != null
                    ? data.getNotifications() : Collections.<Notification>emptyList())
                    .stream()
                    .filter(n -> userId.equals(n.getUserId()))
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .limit(10)
                    .collect(Collectors.toList());

            JsonArray notifArr = new JsonArray();
            notifications.forEach(n -> {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", n.getId());
                obj.addProperty("type", n.getType());
                obj.addProperty("title", n.getTitle());
                obj.addProperty("message", n.getMessage());
                obj.addProperty("relatedId", n.getRelatedId());
                obj.addProperty("createdAt", n.getCreatedAt());
                obj.addProperty("read", n.isRead());
                notifArr.add(obj);
            });
            result.add("notifications", notifArr);

            long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
            result.addProperty("unreadNotifications", unreadCount);
        }

        return result;
    }

    /**
     * 获取职位列表（仅活跃岗位），支持搜索和筛选
     */
    public List<JsonObject> getJobs(String keyword, String department, String type) {
        DataStore data = dataService.getData();
        return data.getJobs().stream()
                .filter(j -> "active".equals(j.getStatus()))
                .filter(j -> keyword == null || keyword.isEmpty() ||
                        j.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                        (j.getDescription() != null && j.getDescription().toLowerCase().contains(keyword.toLowerCase())))
                .filter(j -> department == null || department.isEmpty() ||
                        j.getDepartment().equalsIgnoreCase(department))
                .filter(j -> type == null || type.isEmpty() ||
                        j.getType().equalsIgnoreCase(type))
                .map(j -> {
                    JsonObject obj = (JsonObject) JsonUtil.getGson().toJsonTree(j);
                    long applicantCount = data.getApplications().stream()
                            .filter(a -> a.getJobId().equals(j.getId()))
                            .count();
                    obj.addProperty("applicantCount", applicantCount);
                    return obj;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取单个职位详情（任何状态都可查看）
     */
    public Job getJobById(String jobId) {
        DataStore data = dataService.getData();
        return findJobById(data, jobId);
    }

    /**
     * 提交申请，返回结果封装在 Map 中：
     * - "error" + "code" 表示失败
     * - "application" 表示成功
     */
    public Map<String, Object> applyForJob(String userId, String jobId, String coverLetter, List<String> documentIds) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        Job job = findJobById(data, jobId);
        if (job == null) {
            result.put("error", "职位不存在");
            result.put("code", 404);
            return result;
        }

        if (!"active".equals(job.getStatus())) {
            result.put("error", "该岗位已关闭，无法申请");
            result.put("code", 400);
            return result;
        }

        if (coverLetter == null || coverLetter.isBlank()) {
            result.put("error", "缺少必填字段");
            result.put("code", 400);
            return result;
        }

        boolean alreadyApplied = data.getApplications().stream()
                .anyMatch(a -> a.getApplicantId().equals(userId) && a.getJobId().equals(jobId));
        if (alreadyApplied) {
            result.put("error", "已申请过该岗位");
            result.put("code", 409);
            return result;
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String today = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        Application app = new Application();
        app.setId(dataService.generateId("a", data.getApplications()));
        app.setJobId(jobId);
        app.setApplicantId(userId);
        app.setStatus("applied");
        app.setCoverLetter(coverLetter);
        app.setDocumentIds(documentIds != null ? documentIds : new ArrayList<>());
        app.setMatchScore((int) (Math.random() * 30) + 70);
        app.setRiskFlag(null);
        app.setAiSummary("Application auto-analyzed by system.");
        app.setAppliedAt(now);
        app.setUpdatedAt(now);

        List<Application.TimelineEntry> timeline = new ArrayList<>();
        Application.TimelineEntry t1 = new Application.TimelineEntry();
        t1.setStatus("applied"); t1.setDate(today); t1.setCompleted(true);
        timeline.add(t1);
        for (String s : new String[]{"review", "shortlisted", "interview", "offer"}) {
            Application.TimelineEntry t = new Application.TimelineEntry();
            t.setStatus(s); t.setDate(null); t.setCompleted(false);
            timeline.add(t);
        }
        app.setTimeline(timeline);

        data.getApplications().add(app);
        dataService.save();

        result.put("application", app);
        return result;
    }

    public List<JsonObject> getMyApplications(String userId) {
        DataStore data = dataService.getData();
        return data.getApplications().stream()
                .filter(a -> a.getApplicantId().equals(userId))
                .sorted((a, b) -> b.getAppliedAt().compareTo(a.getAppliedAt()))
                .map(a -> {
                    JsonObject obj = (JsonObject) JsonUtil.getGson().toJsonTree(a);
                    Job job = findJobById(data, a.getJobId());
                    enrichApplicationForApplicantApi(obj, job, a.getJobId(), a.getAppliedAt());
                    return obj;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取申请详情，返回 Map:
     * - "notFound" = true : 申请不存在
     * - "forbidden" = true : 无权访问
     * - "data" : 数据
     */
    public Map<String, Object> getApplicationDetail(String userId, String appId) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        Application app = data.getApplications().stream()
                .filter(a -> a.getId().equals(appId))
                .findFirst().orElse(null);

        if (app == null) {
            result.put("notFound", true);
            return result;
        }

        if (!app.getApplicantId().equals(userId)) {
            result.put("forbidden", true);
            return result;
        }

        JsonObject obj = (JsonObject) JsonUtil.getGson().toJsonTree(app);
        Job job = findJobById(data, app.getJobId());
        enrichApplicationForApplicantApi(obj, job, app.getJobId(), app.getAppliedAt());
        result.put("data", obj);
        return result;
    }

    public Map<String, Object> withdrawApplication(String userId, String appId) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        Application app = data.getApplications().stream()
                .filter(a -> a.getId().equals(appId))
                .findFirst().orElse(null);

        if (app == null) {
            result.put("error", "申请不存在");
            result.put("code", 404);
            return result;
        }

        if (!app.getApplicantId().equals(userId)) {
            result.put("error", "无权限撤销该申请");
            result.put("code", 403);
            return result;
        }

        if ("offer".equals(app.getStatus()) || "rejected".equals(app.getStatus())) {
            result.put("error", "当前申请状态不支持撤销");
            result.put("code", 400);
            return result;
        }

        data.getApplications().remove(app);
        if (data.getInterviews() != null) {
            data.getInterviews().removeIf(i -> appId.equals(i.getApplicationId()));
        }
        dataService.save();

        result.put("success", true);
        return result;
    }

    public JsonObject getProfile(String userId) {
        DataStore data = dataService.getData();
        User user = data.getUsers().stream()
                .filter(u -> u.getId().equals(userId)).findFirst().orElse(null);
        Profile profile = findProfileByUserId(data, userId);

        if (user == null) return null;

        JsonObject result = new JsonObject();

        JsonObject safeUser = new JsonObject();
        safeUser.addProperty("id", user.getId());
        safeUser.addProperty("name", user.getName());
        safeUser.addProperty("email", user.getEmail());
        safeUser.addProperty("phone", user.getPhone());
        safeUser.addProperty("role", user.getRole());
        safeUser.addProperty("department", user.getDepartment());
        safeUser.addProperty("avatar", user.getAvatar());
        result.add("user", safeUser);

        if (profile != null) {
            result.add("profile", JsonUtil.getGson().toJsonTree(profile));
        } else {
            JsonObject emptyProfile = new JsonObject();
            emptyProfile.addProperty("profileCompletion", 0);
            emptyProfile.add("skills", new JsonArray());
            emptyProfile.add("coursework", new JsonArray());
            emptyProfile.add("experience", new JsonArray());
            emptyProfile.add("documents", new JsonArray());
            result.add("profile", emptyProfile);
        }
        return result;
    }

    /**
     * 更新个人资料，返回错误信息或 null（成功）
     */
    public String updateProfile(String userId, JsonObject updates) {
        DataStore data = dataService.getData();
        Profile profile = getOrCreateProfile(data, userId);
        User user = data.getUsers().stream()
                .filter(u -> u.getId().equals(userId)).findFirst().orElse(null);
        if (profile == null || user == null) return "用户资料不存在";

        if (updates.has("gpa") && updates.has("maxGpa")) {
            double gpa = updates.get("gpa").getAsDouble();
            double maxGpa = updates.get("maxGpa").getAsDouble();
            if (gpa > maxGpa) {
                return "GPA 不能超过最大值";
            }
        } else if (updates.has("gpa")) {
            double gpa = updates.get("gpa").getAsDouble();
            if (gpa > profile.getMaxGpa()) {
                return "GPA 不能超过最大值";
            }
        }

        if (updates.has("name")) user.setName(updates.get("name").getAsString());
        if (updates.has("phone")) user.setPhone(updates.get("phone").getAsString());
        if (updates.has("degree")) profile.setDegree(updates.get("degree").getAsString());
        if (updates.has("year")) profile.setYear(updates.get("year").getAsInt());
        if (updates.has("gpa")) profile.setGpa(updates.get("gpa").getAsDouble());
        if (updates.has("maxGpa")) profile.setMaxGpa(updates.get("maxGpa").getAsDouble());
        if (updates.has("skills")) {
            List<String> skills = new ArrayList<>();
            updates.getAsJsonArray("skills").forEach(e -> skills.add(e.getAsString()));
            profile.setSkills(skills);
        }

        if (updates.has("coursework")) {
            List<Profile.Coursework> courseworkList = new ArrayList<>();
            updates.getAsJsonArray("coursework").forEach(e -> {
                Profile.Coursework cw = JsonUtil.getGson().fromJson(e, Profile.Coursework.class);
                courseworkList.add(cw);
            });
            profile.setCoursework(courseworkList);
        }

        if (updates.has("experience")) {
            List<Profile.Experience> expList = new ArrayList<>();
            updates.getAsJsonArray("experience").forEach(e -> {
                Profile.Experience exp = JsonUtil.getGson().fromJson(e, Profile.Experience.class);
                expList.add(exp);
            });
            profile.setExperience(expList);
        }

        dataService.save();
        return null;
    }

    private static final Set<String> ALLOWED_FILE_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "txt", "rtf"
    );

    /**
     * 上传文档（模拟），返回 Map:
     * - "error" + "code" 表示失败
     * - "document" 表示成功
     */
    public Map<String, Object> uploadDocument(String userId, String fileName, String fileType, String fileSize) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        if (fileName == null || fileName.isBlank()) {
            result.put("error", "缺少上传文件");
            result.put("code", 400);
            return result;
        }

        String ext = "";
        int dotIdx = fileName.lastIndexOf('.');
        if (dotIdx >= 0) {
            ext = fileName.substring(dotIdx + 1).toLowerCase();
        }
        if (!ALLOWED_FILE_EXTENSIONS.contains(ext)) {
            result.put("error", "不支持的文件类型");
            result.put("code", 400);
            return result;
        }

        Profile profile = getOrCreateProfile(data, userId);
        if (profile == null) {
            result.put("error", "用户资料不存在");
            result.put("code", 404);
            return result;
        }

        Profile.Document doc = new Profile.Document();
        doc.setId("doc" + String.format("%03d", profile.getDocuments().size() + 100));
        doc.setName(fileName);
        doc.setType(fileType);
        doc.setSize(fileSize);
        doc.setUploadedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        profile.getDocuments().add(doc);
        dataService.save();

        result.put("document", doc);
        return result;
    }

    /**
     * 删除文档，返回 Map:
     * - "error" + "code" 表示失败
     * - "success" = true 表示成功
     */
    public Map<String, Object> deleteDocument(String userId, String docId) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        Profile ownerProfile = null;
        for (Profile p : data.getProfiles()) {
            for (Profile.Document d : p.getDocuments()) {
                if (d.getId().equals(docId)) {
                    ownerProfile = p;
                    break;
                }
            }
            if (ownerProfile != null) break;
        }

        if (ownerProfile == null) {
            result.put("error", "文档不存在");
            result.put("code", 404);
            return result;
        }

        if (!ownerProfile.getUserId().equals(userId)) {
            result.put("error", "无权限删除该文档");
            result.put("code", 403);
            return result;
        }

        ownerProfile.getDocuments().removeIf(d -> d.getId().equals(docId));
        dataService.save();

        result.put("success", true);
        return result;
    }

    /**
     * 与前端约定：jobTitle、moduleCode、appliedDate；并保留 role、module 供旧调用方使用。
     */
    private void enrichApplicationForApplicantApi(JsonObject obj, Job job, String jobId, String appliedAt) {
        if (job != null) {
            obj.addProperty("jobTitle", job.getTitle());
            obj.addProperty("moduleCode", job.getModuleCode() != null ? job.getModuleCode() : "");
            obj.addProperty("role", job.getTitle());
            obj.addProperty("module", job.getModuleCode() != null ? job.getModuleCode() : "");
        } else {
            String fallback = (jobId != null && !jobId.isEmpty()) ? ("Job " + jobId) : "Unknown position";
            obj.addProperty("jobTitle", fallback);
            obj.addProperty("moduleCode", "");
            obj.addProperty("role", fallback);
            obj.addProperty("module", "");
        }
        if (appliedAt != null && !appliedAt.isEmpty()) {
            obj.addProperty("appliedDate", formatDisplayDate(appliedAt));
        }
    }

    private static String formatDisplayDate(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) {
            return "";
        }
        int t = isoDateTime.indexOf('T');
        if (t > 0) {
            return isoDateTime.substring(0, t);
        }
        return isoDateTime.length() >= 10 ? isoDateTime.substring(0, 10) : isoDateTime;
    }

    private Profile findProfileByUserId(DataStore data, String userId) {
        return data.getProfiles().stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst().orElse(null);
    }

    private Profile getOrCreateProfile(DataStore data, String userId) {
        Profile profile = findProfileByUserId(data, userId);
        if (profile != null) {
            return profile;
        }

        Profile created = new Profile();
        created.setId(dataService.generateId("p", data.getProfiles()));
        created.setUserId(userId);
        created.setStudentId("");
        created.setDegree("");
        created.setYear(0);
        created.setGpa(0);
        created.setMaxGpa(4.0);
        created.setProfileCompletion(0);
        created.setSkills(new ArrayList<>());
        created.setCoursework(new ArrayList<>());
        created.setExperience(new ArrayList<>());
        created.setDocuments(new ArrayList<>());
        created.setMaxWeeklyHours(20);
        created.setCurrentWeeklyHours(0);
        data.getProfiles().add(created);
        return created;
    }

    private Job findJobById(DataStore data, String jobId) {
        return data.getJobs().stream()
                .filter(j -> j.getId().equals(jobId))
                .findFirst().orElse(null);
    }

    public List<JsonObject> getNotifications(String userId) {
        DataStore data = dataService.getData();
        List<Notification> notifications = (data.getNotifications() != null
                ? data.getNotifications() : Collections.<Notification>emptyList())
                .stream()
                .filter(n -> userId.equals(n.getUserId()))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(20)
                .collect(Collectors.toList());

        List<JsonObject> result = new ArrayList<>();
        notifications.forEach(n -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", n.getId());
            obj.addProperty("type", n.getType());
            obj.addProperty("title", n.getTitle());
            obj.addProperty("message", n.getMessage());
            obj.addProperty("relatedId", n.getRelatedId());
            obj.addProperty("createdAt", n.getCreatedAt());
            obj.addProperty("read", n.isRead());
            result.add(obj);
        });
        return result;
    }

    public void markNotificationRead(String userId, String notifId) {
        DataStore data = dataService.getData();
        if (data.getNotifications() == null) return;
        data.getNotifications().stream()
                .filter(n -> n.getId().equals(notifId) && n.getUserId().equals(userId))
                .findFirst()
                .ifPresent(n -> {
                    n.setRead(true);
                    dataService.save();
                });
    }

    public void markAllNotificationsRead(String userId) {
        DataStore data = dataService.getData();
        if (data.getNotifications() == null) return;
        data.getNotifications().stream()
                .filter(n -> n.getUserId().equals(userId) && !n.isRead())
                .forEach(n -> n.setRead(true));
        dataService.save();
    }
}
