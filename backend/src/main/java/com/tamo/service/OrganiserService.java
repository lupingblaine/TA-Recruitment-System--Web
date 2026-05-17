package com.tamo.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tamo.model.Application;
import com.tamo.model.DataStore;
import com.tamo.model.Interview;
import com.tamo.model.Job;
import com.tamo.model.Module;
import com.tamo.model.Notification;
import com.tamo.model.Profile;
import com.tamo.model.Notification;
import com.tamo.model.User;
import com.tamo.util.JsonUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class OrganiserService {

    private static final Set<String> VALID_APP_STATUSES = Set.of(
            "applied", "review", "shortlisted", "interview", "offer", "rejected"
    );
    private static final Set<String> VALID_ACTIONS = Set.of("shortlist", "reject", "offer");
    private static final Set<String> VALID_INTERVIEW_TYPES = Set.of("video", "in-person");

    private final JsonDataService dataService;

    public OrganiserService(JsonDataService dataService) {
        this.dataService = dataService;
    }

    public JsonObject getDashboard(String organiserId) {
        DataStore data = dataService.getData();

        List<Job> myJobs = data.getJobs().stream()
                .filter(j -> j.getOrganiserId().equals(organiserId))
                .collect(Collectors.toList());

        Set<String> myJobIds = myJobs.stream().map(Job::getId).collect(Collectors.toSet());

        List<Application> relatedApps = data.getApplications().stream()
                .filter(a -> myJobIds.contains(a.getJobId()))
                .collect(Collectors.toList());

        long activePostings = myJobs.stream().filter(j -> "active".equals(j.getStatus())).count();
        long totalApplicants = relatedApps.size();
        long currentApplicants = relatedApps.stream()
                .filter(a -> a.getStatus() == null || !"rejected".equals(a.getStatus()))
                .count();
        long pendingInterviews = relatedApps.stream()
                .filter(a -> "interview".equals(a.getStatus()))
                .count();

        JsonObject result = new JsonObject();
        result.addProperty("activePostings", activePostings);
        result.addProperty("currentApplicants", currentApplicants);
        result.addProperty("totalApplicants", totalApplicants);
        result.addProperty("pendingInterviews", pendingInterviews);

        JsonArray recentActivities = new JsonArray();
        relatedApps.stream()
                .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                .limit(5)
                .forEach(a -> {
                    User applicant = findUserById(data, a.getApplicantId());
                    Job job = findJobById(data, a.getJobId());
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", a.getId());
                    obj.addProperty("name", applicant != null ? applicant.getName() : "Unknown");
                    obj.addProperty("role", job != null ? job.getModuleCode() + " " + job.getTitle().replaceAll(".*-\\s*", "") : "");
                    obj.addProperty("status", a.getStatus());

                    String oldStatus = app.getStatus();
                    obj.addProperty("date", a.getUpdatedAt());
                    recentActivities.add(obj);
                });
        result.add("recentActivities", recentActivities);

        JsonArray activeJobsArr = new JsonArray();
        myJobs.stream().filter(j -> "active".equals(j.getStatus())).forEach(j -> {
            long totalAppCount = relatedApps.stream().filter(a -> a.getJobId().equals(j.getId())).count();
            long currentAppCount = relatedApps.stream()
                    .filter(a -> a.getJobId().equals(j.getId()))
                    .filter(a -> a.getStatus() == null || !"rejected".equals(a.getStatus()))
                    .count();
            JsonObject obj = (JsonObject) JsonUtil.getGson().toJsonTree(j);
            obj.addProperty("applicants", currentAppCount);
            obj.addProperty("applicationCount", currentAppCount);
            obj.addProperty("totalApplications", totalAppCount);
            activeJobsArr.add(obj);
        });
        result.add("activeJobs", activeJobsArr);
        result.add("jobs", activeJobsArr);

        return result;
    }

    public List<JsonObject> getNotifications(String organiserId) {
        DataStore data = dataService.getData();
        List<Notification> notifications = (data.getNotifications() != null
                ? data.getNotifications() : Collections.<Notification>emptyList())
                .stream()
                .filter(n -> organiserId.equals(n.getUserId()))
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

    public void markNotificationRead(String organiserId, String notifId) {
        DataStore data = dataService.getData();
        if (data.getNotifications() == null) return;
        data.getNotifications().stream()
                .filter(n -> n.getId().equals(notifId) && organiserId.equals(n.getUserId()))
                .findFirst()
                .ifPresent(n -> {
                    n.setRead(true);
                    dataService.save();
                });
    }

    public void markAllNotificationsRead(String organiserId) {
        DataStore data = dataService.getData();
        if (data.getNotifications() == null) return;
        data.getNotifications().stream()
                .filter(n -> organiserId.equals(n.getUserId()) && !n.isRead())
                .forEach(n -> n.setRead(true));
        dataService.save();
    }

    private void createNotification(DataStore data, String userId, String type, String title, String message, String relatedId) {
        if (userId == null) return;
        if (data.getNotifications() == null) data.setNotifications(new ArrayList<>());
        Notification n = new Notification();
        n.setId(dataService.generateId("n", data.getNotifications()));
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setRelatedId(relatedId);
        n.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        n.setRead(false);
        data.getNotifications().add(n);
        dataService.save();
    }

    public List<JsonObject> getJobs(String organiserId) {
        DataStore data = dataService.getData();
        return data.getJobs().stream()
                .filter(j -> j.getOrganiserId().equals(organiserId))
                .map(j -> {
                    JsonObject obj = (JsonObject) JsonUtil.getGson().toJsonTree(j);
                    long totalApplications = data.getApplications().stream()
                            .filter(a -> a.getJobId().equals(j.getId())).count();
                    long currentApplications = data.getApplications().stream()
                            .filter(a -> a.getJobId().equals(j.getId()))
                            .filter(a -> a.getStatus() == null || !"rejected".equals(a.getStatus()))
                            .count();
                    long shortlisted = data.getApplications().stream()
                            .filter(a -> a.getJobId().equals(j.getId()) &&
                                    ("shortlisted".equals(a.getStatus()) || "interview".equals(a.getStatus()) || "offer".equals(a.getStatus())))
                            .count();
                    obj.addProperty("applicants", currentApplications);
                    obj.addProperty("applicationCount", currentApplications);
                    obj.addProperty("totalApplications", totalApplications);
                    obj.addProperty("shortlisted", shortlisted);
                    return obj;
                })
                .collect(Collectors.toList());
    }

    /**
     * 创建岗位，返回 Map（"error"/"code" 或 "job"）
     */
    public Map<String, Object> createJob(String organiserId, JsonObject body) {
        Map<String, Object> result = new HashMap<>();

        String title = body.has("title") ? body.get("title").getAsString() : null;
        String moduleId = body.has("moduleId") ? body.get("moduleId").getAsString() : null;
        String moduleCode = body.has("moduleCode") ? body.get("moduleCode").getAsString() : null;

        if ((moduleId == null || moduleId.isBlank()) && moduleCode != null && !moduleCode.isBlank()) {
            DataStore lookupData = dataService.getData();
            Module module = lookupData.getModules().stream()
                    .filter(m -> moduleCode.equals(m.getCode()))
                    .findFirst().orElse(null);
            moduleId = module != null ? module.getId() : moduleCode;
        }

        if (title == null || title.isBlank() || moduleId == null || moduleId.isBlank()) {
            result.put("error", "缺少必填字段");
            result.put("code", 400);
            return result;
        }

        if (body.has("deadline") && !body.get("deadline").getAsString().isBlank()) {
            try {
                LocalDate deadline = LocalDate.parse(body.get("deadline").getAsString());
                if (deadline.isBefore(LocalDate.now())) {
                    result.put("error", "截止日期不能为过去时间");
                    result.put("code", 400);
                    return result;
                }
            } catch (Exception ignored) {}
        }

        DataStore data = dataService.getData();
        Job job = new Job();
        job.setId(dataService.generateId("j", data.getJobs()));
        job.setOrganiserId(organiserId);
        job.setModuleCode(body.has("moduleCode") ? body.get("moduleCode").getAsString() : "");
        job.setModuleId(moduleId);
        job.setTitle(title);
        job.setDescription(body.has("description") ? body.get("description").getAsString() : "");
        job.setProfessor(body.has("professor") ? body.get("professor").getAsString() : "");
        job.setDepartment(body.has("department") ? body.get("department").getAsString() : "");
        job.setType(body.has("type") ? body.get("type").getAsString() : "Part-time");
        job.setHoursPerWeek(body.has("hoursPerWeek") ? body.get("hoursPerWeek").getAsInt() : 10);
        job.setPositions(body.has("positions") ? body.get("positions").getAsInt() : 1);
        job.setDeadline(body.has("deadline") ? body.get("deadline").getAsString() : "");
        job.setStatus("active");
        job.setTags(new ArrayList<>());
        job.setRequirements(new ArrayList<>());
        job.setResponsibilities(new ArrayList<>());
        job.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        if (body.has("tags")) body.getAsJsonArray("tags").forEach(e -> job.getTags().add(e.getAsString()));
        if (body.has("requirements")) body.getAsJsonArray("requirements").forEach(e -> job.getRequirements().add(e.getAsString()));
        if (body.has("responsibilities")) body.getAsJsonArray("responsibilities").forEach(e -> job.getResponsibilities().add(e.getAsString()));

        data.getJobs().add(job);
        dataService.save();
        result.put("job", job);
        return result;
    }

    /**
     * 编辑岗位，返回 Map（"error"/"code" 或 "job"）
     */
    public Map<String, Object> updateJob(String organiserId, String jobId, JsonObject body) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        Job job = data.getJobs().stream()
                .filter(j -> j.getId().equals(jobId))
                .findFirst().orElse(null);

        if (job == null) {
            result.put("error", "岗位不存在");
            result.put("code", 404);
            return result;
        }

        if (!job.getOrganiserId().equals(organiserId)) {
            result.put("error", "无权限编辑该岗位");
            result.put("code", 403);
            return result;
        }

        if (body.has("title")) job.setTitle(body.get("title").getAsString());
        if (body.has("moduleCode")) {
            String newModuleCode = body.get("moduleCode").getAsString();
            job.setModuleCode(newModuleCode);
            if (newModuleCode != null && !newModuleCode.isBlank()) {
                Module module = data.getModules().stream()
                        .filter(m -> newModuleCode.equals(m.getCode()))
                        .findFirst().orElse(null);
                job.setModuleId(module != null ? module.getId() : newModuleCode);
            }
        }
        if (body.has("department")) job.setDepartment(body.get("department").getAsString());
        if (body.has("description")) job.setDescription(body.get("description").getAsString());
        if (body.has("type")) job.setType(body.get("type").getAsString());
        if (body.has("hoursPerWeek")) job.setHoursPerWeek(body.get("hoursPerWeek").getAsInt());
        if (body.has("positions")) job.setPositions(body.get("positions").getAsInt());
        if (body.has("deadline")) {
            String deadlineStr = body.get("deadline").getAsString();
            if (!deadlineStr.isBlank()) {
                try {
                    LocalDate deadline = LocalDate.parse(deadlineStr);
                    if (deadline.isBefore(LocalDate.now())) {
                        result.put("error", "截止日期不能为过去时间");
                        result.put("code", 400);
                        return result;
                    }
                } catch (Exception ignored) {
                }
            }
            job.setDeadline(deadlineStr);
        }
        if (body.has("status")) job.setStatus(body.get("status").getAsString());

        dataService.save();
        result.put("job", job);
        return result;
    }

    /**
     * 删除岗位，返回 Map（"error"/"code" 或 "success"=true）
     */
    public Map<String, Object> deleteJob(String organiserId, String jobId) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        Job job = data.getJobs().stream()
                .filter(j -> j.getId().equals(jobId))
                .findFirst().orElse(null);

        if (job == null) {
            result.put("error", "岗位不存在");
            result.put("code", 404);
            return result;
        }

        if (!job.getOrganiserId().equals(organiserId)) {
            result.put("error", "无权限删除该岗位");
            result.put("code", 403);
            return result;
        }

        // 删除岗位时同时清理该岗位相关申请和面试，避免 TA 页面显示遗留 pipeline
        Set<String> deletedAppIds = data.getApplications().stream()
                .filter(a -> a.getJobId().equals(jobId))
                .map(Application::getId)
                .collect(Collectors.toSet());
        data.getApplications().removeIf(a -> deletedAppIds.contains(a.getId()));
        data.getInterviews().removeIf(i -> deletedAppIds.contains(i.getApplicationId()));

        data.getJobs().remove(job);
        dataService.save();
        result.put("success", true);
        return result;
    }

    public JsonObject getPipeline(String organiserId, String moduleFilter) {
        DataStore data = dataService.getData();
        Set<String> myJobIds = data.getJobs().stream()
                .filter(j -> j.getOrganiserId().equals(organiserId))
                .map(Job::getId)
                .collect(Collectors.toSet());

        List<Application> apps = data.getApplications().stream()
                .filter(a -> myJobIds.contains(a.getJobId()))
                .filter(a -> {
                    if (moduleFilter == null || "All".equals(moduleFilter) || moduleFilter.isEmpty()) return true;
                    Job job = findJobById(data, a.getJobId());
                    return job != null && job.getModuleCode().equals(moduleFilter);
                })
                .collect(Collectors.toList());

        JsonObject result = new JsonObject();
        for (String status : new String[]{"applied", "review", "shortlisted", "interview", "offer"}) {
            JsonArray arr = new JsonArray();
            apps.stream().filter(a -> a.getStatus().equals(status)).forEach(a -> {
                User applicant = findUserById(data, a.getApplicantId());
                Profile profile = findProfileByUserId(data, a.getApplicantId());
                Job job = findJobById(data, a.getJobId());
                JsonObject obj = new JsonObject();
                obj.addProperty("id", a.getId());
                obj.addProperty("name", applicant != null ? applicant.getName() : "Unknown");
                obj.addProperty("role", job != null ? job.getModuleCode() : "");
                obj.addProperty("status", a.getStatus());
                obj.addProperty("avatar", applicant != null ? String.valueOf(applicant.getName().charAt(0)) : "?");
                obj.addProperty("gpa", profile != null ? String.valueOf(profile.getGpa()) : "N/A");
                obj.addProperty("date", a.getAppliedAt());
                obj.addProperty("matchScore", a.getMatchScore());
                obj.addProperty("riskFlag", a.getRiskFlag());
                obj.addProperty("aiSummary", a.getAiSummary());
                // 如果状态是 interview，添加面试信息
                if ("interview".equals(status)) {
                    Interview iv = data.getInterviews().stream()
                            .filter(i -> i.getApplicationId().equals(a.getId()))
                            .findFirst().orElse(null);
                    if (iv != null) {
                        JsonObject interviewObj = new JsonObject();
                        interviewObj.addProperty("id", iv.getId());
                        interviewObj.addProperty("date", iv.getDate());
                        interviewObj.addProperty("startTime", iv.getStartTime());
                        interviewObj.addProperty("type", iv.getType());
                        interviewObj.addProperty("location", iv.getLocation());
                        interviewObj.addProperty("status", iv.getStatus());
                        obj.add("interview", interviewObj);
                    }
                }
                arr.add(obj);
            });
            result.add(status, arr);
        }
        return result;
    }

    /**
     * 更新候选人状态，返回 Map
     */
    public Map<String, Object> updateCandidateStatus(String organiserId, String appId, String newStatus) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        if (newStatus == null || newStatus.isBlank()) {
            result.put("error", "缺少 status 参数");
            result.put("code", 400);
            return result;
        }

        if (!VALID_APP_STATUSES.contains(newStatus)) {
            result.put("error", "状态值不合法");
            result.put("code", 400);
            return result;
        }

        Application app = data.getApplications().stream()
                .filter(a -> a.getId().equals(appId))
                .findFirst().orElse(null);

        if (app == null) {
            result.put("error", "申请不存在");
            result.put("code", 404);
            return result;
        }

        Job job = findJobById(data, app.getJobId());
        if (job == null || !job.getOrganiserId().equals(organiserId)) {
            result.put("error", "无权限操作该候选人");
            result.put("code", 403);
            return result;
        }

        String oldStatus = app.getStatus();
        app.setStatus(newStatus);
        app.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        if (app.getTimeline() != null) {
            String today = LocalDate.now().toString();
            for (Application.TimelineEntry te : app.getTimeline()) {
                if (te.getStatus().equals(newStatus)) {
                    te.setCompleted(true);
                    te.setDate(today);
                    te.setPending(null);
                    break;
                }
            }
        }

        // Create notification for the applicant
        if (!Objects.equals(oldStatus, newStatus)) {
            String applicantId = app.getApplicantId();
            String moduleCode = job != null ? job.getModuleCode() : "";
            String notifType = "status_change";
            String title = "";
            String message = "";

            switch (newStatus) {
                case "review" -> {
                    title = "Application Under Review";
                    message = "Your application for " + moduleCode + " is now being reviewed.";
                }
                case "shortlisted" -> {
                    title = "You've Been Shortlisted!";
                    message = "Congratulations! Your application for " + moduleCode + " has been shortlisted.";
                }
                case "interview" -> {
                    notifType = "interview_scheduled";
                    title = "Interview Stage";
                    message = "Your application for " + moduleCode + " has moved to the interview stage. An interview will be scheduled soon.";
                }
                case "offer" -> {
                    notifType = "offer";
                    title = "Offer Received!";
                    message = "Congratulations! You have received an offer for " + moduleCode + "!";
                }
                case "rejected" -> {
                    notifType = "rejection";
                    title = "Application Update";
                    message = "We regret to inform you that your application for " + moduleCode + " was not successful.";
                }
                default -> {
                    title = "Application Status Updated";
                    message = "Your application for " + moduleCode + " status changed to: " + newStatus;
                }
            }
            createNotification(data, applicantId, notifType, title, message, app.getId());
            if (!"interview".equals(newStatus) && data.getInterviews() != null) {
                data.getInterviews().stream()
                        .filter(i -> appId.equals(i.getApplicationId()))
                        .filter(i -> i.getStatus() != null && !"cancelled".equalsIgnoreCase(i.getStatus()))
                        .forEach(iv -> {
                            iv.setStatus("cancelled");
                            String cancelTitle = "Interview Cancelled: " + iv.getModuleCode();
                            String cancelMsg = "Your interview for " + iv.getModuleCode() + " " + iv.getModuleName() +
                                    " scheduled on " + iv.getDate() + " at " + iv.getStartTime() +
                                    " has been cancelled because the application moved out of the interview stage.";
                            createNotification(data, iv.getCandidateId(), "interview_cancelled", cancelTitle, cancelMsg, iv.getId());
                        });
            }
        }
        dataService.save();
        result.put("application", app);
        return result;
    }

    /**
     * 获取申请详情，含所有权校验
     */
    public Map<String, Object> getApplicationDetails(String organiserId, String appId) {
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

        Job job = findJobById(data, app.getJobId());
        if (job == null || !job.getOrganiserId().equals(organiserId)) {
            result.put("error", "无权限查看该申请");
            result.put("code", 403);
            return result;
        }

        User applicant = findUserById(data, app.getApplicantId());
        Profile profile = findProfileByUserId(data, app.getApplicantId());

        JsonObject detail = (JsonObject) JsonUtil.getGson().toJsonTree(app);
        if (applicant != null) {
            JsonObject applicantProfile = new JsonObject();
            applicantProfile.addProperty("name", applicant.getName());
            if (profile != null) {
                applicantProfile.addProperty("degree", profile.getDegree());
                applicantProfile.addProperty("gpa", profile.getGpa());
                applicantProfile.add("coursework", JsonUtil.getGson().toJsonTree(profile.getCoursework()));
                applicantProfile.add("experience", JsonUtil.getGson().toJsonTree(profile.getExperience()));
            }
            detail.add("applicantProfile", applicantProfile);
        }
        if (profile != null) {
            detail.add("documents", JsonUtil.getGson().toJsonTree(profile.getDocuments()));
        }

        result.put("data", detail);
        return result;
    }

    /**
     * 对申请执行操作（shortlist/reject/offer），返回 Map
     */
    public Map<String, Object> applicationAction(String organiserId, String appId, String action) {
        Map<String, Object> result = new HashMap<>();

        if (action == null || action.isBlank()) {
            result.put("error", "缺少必填字段");
            result.put("code", 400);
            return result;
        }

        if (!VALID_ACTIONS.contains(action)) {
            result.put("error", "操作类型不合法，仅支持 shortlist/reject/offer");
            result.put("code", 400);
            return result;
        }

        String mappedStatus = switch (action) {
            case "shortlist" -> "shortlisted";
            case "reject" -> "rejected";
            case "offer" -> "offer";
            default -> action;
        };

        return updateCandidateStatus(organiserId, appId, mappedStatus);
    }

    /**
     * 获取面试列表
     */
    public List<Object> getInterviews(String organiserId) {
        DataStore data = dataService.getData();
        return data.getInterviews().stream()
                .filter(i -> i.getOrganiserId().equals(organiserId))
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .map(iv -> (Object) JsonUtil.getGson().toJsonTree(iv))
                .collect(Collectors.toList());
    }

    /**
     * 创建面试，返回 Map
     */
    public Map<String, Object> createInterview(String organiserId, JsonObject body) {
        Map<String, Object> result = new HashMap<>();

        String applicationId = body.has("applicationId") ? body.get("applicationId").getAsString() : null;
        String candidateId = body.has("candidateId") ? body.get("candidateId").getAsString() : null;
        String date = body.has("date") ? body.get("date").getAsString() : null;
        String startTime = body.has("startTime") ? body.get("startTime").getAsString() : null;
        if ((startTime == null || startTime.isBlank()) && body.has("time")) {
            startTime = body.get("time").getAsString();
        }

        if (applicationId == null || applicationId.isBlank() ||
                date == null || date.isBlank() ||
                startTime == null || startTime.isBlank()) {
            result.put("error", "缺少必填字段");
            result.put("code", 400);
            return result;
        }

        String type = body.has("type") ? body.get("type").getAsString() : "video";
        if ("in_person".equals(type)) {
            type = "in-person";
        }
        if (!VALID_INTERVIEW_TYPES.contains(type)) {
            result.put("error", "面试类型不合法，仅支持 video/in-person");
            result.put("code", 400);
            return result;
        }

        DataStore data = dataService.getData();
        Application app = data.getApplications().stream()
                .filter(a -> a.getId().equals(applicationId))
                .findFirst().orElse(null);
        if (app == null) {
            result.put("error", "申请不存在");
            result.put("code", 404);
            return result;
        }
        if ((candidateId == null || candidateId.isBlank()) && app.getApplicantId() != null) {
            candidateId = app.getApplicantId();
        }
        if (candidateId == null || candidateId.isBlank()) {
            result.put("error", "缺少候选人ID");
            result.put("code", 400);
            return result;
        }

        Interview iv = new Interview();
        iv.setId(dataService.generateId("i", data.getInterviews()));
        iv.setApplicationId(applicationId);
        iv.setCandidateId(candidateId);
        iv.setCandidateName(body.has("candidateName") ? body.get("candidateName").getAsString() : "");
        if ((iv.getCandidateName() == null || iv.getCandidateName().isBlank())) {
            User applicant = findUserById(data, candidateId);
            if (applicant != null) {
                iv.setCandidateName(applicant.getName());
            }
        }
        if (body.has("moduleCode")) {
            iv.setModuleCode(body.get("moduleCode").getAsString());
        } else {
            Job job = findJobById(data, app.getJobId());
            iv.setModuleCode(job != null ? job.getModuleCode() : "");
        }
        if (body.has("moduleName")) {
            iv.setModuleName(body.get("moduleName").getAsString());
        } else {
            Job job = findJobById(data, app.getJobId());
            iv.setModuleName(job != null ? job.getTitle() : "");
        }
        iv.setDate(date);
        iv.setStartTime(startTime);
        iv.setEndTime(body.has("endTime") ? body.get("endTime").getAsString() : "");
        iv.setType(type);
        iv.setLocation(body.has("location") ? body.get("location").getAsString() : null);
        iv.setStatus("scheduled");
        iv.setOutcome(null);
        iv.setOrganiserId(organiserId);

        if (!"interview".equals(app.getStatus())) {
            app.setStatus("interview");
            app.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            if (app.getTimeline() != null) {
                String today = LocalDate.now().toString();
                for (Application.TimelineEntry te : app.getTimeline()) {
                    if ("interview".equals(te.getStatus())) {
                        te.setCompleted(true);
                        te.setDate(today);
                        te.setPending(null);
                        break;
                    }
                }
            }
        }

        data.getInterviews().add(iv);

        // Create notification for the applicant about the scheduled interview
        String notifTitle = "Interview Scheduled: " + iv.getModuleCode();
        String notifMsg = "Your interview for " + iv.getModuleCode() + " " + iv.getModuleName() +
                " is scheduled on " + iv.getDate() + " at " + iv.getStartTime() +
                ". Type: " + (iv.getType().equals("video") ? "Video Call" : "In-Person") +
                (iv.getLocation() != null && !iv.getLocation().isBlank() ? " at " + iv.getLocation() : "") + ".";
        createNotification(data, candidateId, "interview_scheduled", notifTitle, notifMsg, iv.getId());

        dataService.save();
        result.put("interview", iv);
        return result;
    }

    /**
     * 更新面试，返回 Map
     */
    public Map<String, Object> updateInterview(String organiserId, String interviewId, JsonObject body) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        Interview iv = data.getInterviews().stream()
                .filter(i -> i.getId().equals(interviewId))
                .findFirst().orElse(null);

        if (iv == null) {
            result.put("error", "面试不存在");
            result.put("code", 404);
            return result;
        }

        if (!iv.getOrganiserId().equals(organiserId)) {
            result.put("error", "无权限操作该面试");
            result.put("code", 403);
            return result;
        }

        if (body.has("date")) iv.setDate(body.get("date").getAsString());
        if (body.has("startTime")) iv.setStartTime(body.get("startTime").getAsString());
        if (body.has("endTime")) iv.setEndTime(body.get("endTime").getAsString());
        if (body.has("status")) iv.setStatus(body.get("status").getAsString());
        if (body.has("outcome")) iv.setOutcome(body.get("outcome").getAsString());
        if (body.has("type")) iv.setType(body.get("type").getAsString());
        if (body.has("location")) iv.setLocation(body.get("location").getAsString());

        dataService.save();
        result.put("interview", iv);
        return result;
    }

    private User findUserById(DataStore data, String userId) {
        return data.getUsers().stream().filter(u -> u.getId().equals(userId)).findFirst().orElse(null);
    }

    private Profile findProfileByUserId(DataStore data, String userId) {
        return data.getProfiles().stream().filter(p -> p.getUserId().equals(userId)).findFirst().orElse(null);
    }

    private Job findJobById(DataStore data, String jobId) {
        return data.getJobs().stream().filter(j -> j.getId().equals(jobId)).findFirst().orElse(null);
    }

    private void createNotification(DataStore data, String userId, String type, String title, String message, String relatedId) {
        if (data.getNotifications() == null) {
            data.setNotifications(new ArrayList<>());
        }
        Notification n = new Notification();
        n.setId(dataService.generateId("n", data.getNotifications()));
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setRelatedId(relatedId);
        n.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        n.setRead(false);
        data.getNotifications().add(n);
    }
}
