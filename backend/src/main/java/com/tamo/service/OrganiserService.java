package com.tamo.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tamo.model.Application;
import com.tamo.model.DataStore;
import com.tamo.model.Interview;
import com.tamo.model.Job;
import com.tamo.model.Profile;
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
        long pendingInterviews = relatedApps.stream()
                .filter(a -> "interview".equals(a.getStatus()))
                .count();

        JsonObject result = new JsonObject();
        result.addProperty("activePostings", activePostings);
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
                    obj.addProperty("date", a.getUpdatedAt());
                    recentActivities.add(obj);
                });
        result.add("recentActivities", recentActivities);

        JsonArray activeJobsArr = new JsonArray();
        myJobs.stream().filter(j -> "active".equals(j.getStatus())).forEach(j -> {
            long appCount = relatedApps.stream().filter(a -> a.getJobId().equals(j.getId())).count();
            JsonObject obj = (JsonObject) JsonUtil.getGson().toJsonTree(j);
            obj.addProperty("applicants", appCount);
            activeJobsArr.add(obj);
        });
        result.add("activeJobs", activeJobsArr);

        return result;
    }

    public List<JsonObject> getJobs(String organiserId) {
        DataStore data = dataService.getData();
        return data.getJobs().stream()
                .filter(j -> j.getOrganiserId().equals(organiserId))
                .map(j -> {
                    JsonObject obj = (JsonObject) JsonUtil.getGson().toJsonTree(j);
                    long applicants = data.getApplications().stream()
                            .filter(a -> a.getJobId().equals(j.getId())).count();
                    long shortlisted = data.getApplications().stream()
                            .filter(a -> a.getJobId().equals(j.getId()) &&
                                    ("shortlisted".equals(a.getStatus()) || "interview".equals(a.getStatus()) || "offer".equals(a.getStatus())))
                            .count();
                    obj.addProperty("applicants", applicants);
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
        if (body.has("description")) job.setDescription(body.get("description").getAsString());
        if (body.has("hoursPerWeek")) job.setHoursPerWeek(body.get("hoursPerWeek").getAsInt());
        if (body.has("positions")) job.setPositions(body.get("positions").getAsInt());
        if (body.has("deadline")) job.setDeadline(body.get("deadline").getAsString());
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

        if (applicationId == null || applicationId.isBlank() ||
                candidateId == null || candidateId.isBlank() ||
                date == null || date.isBlank() ||
                startTime == null || startTime.isBlank()) {
            result.put("error", "缺少必填字段");
            result.put("code", 400);
            return result;
        }

        String type = body.has("type") ? body.get("type").getAsString() : "video";
        if (!VALID_INTERVIEW_TYPES.contains(type)) {
            result.put("error", "面试类型不合法，仅支持 video/in-person");
            result.put("code", 400);
            return result;
        }

        DataStore data = dataService.getData();
        Interview iv = new Interview();
        iv.setId(dataService.generateId("i", data.getInterviews()));
        iv.setApplicationId(applicationId);
        iv.setCandidateId(candidateId);
        iv.setCandidateName(body.has("candidateName") ? body.get("candidateName").getAsString() : "");
        iv.setModuleCode(body.has("moduleCode") ? body.get("moduleCode").getAsString() : "");
        iv.setModuleName(body.has("moduleName") ? body.get("moduleName").getAsString() : "");
        iv.setDate(date);
        iv.setStartTime(startTime);
        iv.setEndTime(body.has("endTime") ? body.get("endTime").getAsString() : "");
        iv.setType(type);
        iv.setLocation(body.has("location") ? body.get("location").getAsString() : null);
        iv.setStatus("scheduled");
        iv.setOutcome(null);
        iv.setOrganiserId(organiserId);

        data.getInterviews().add(iv);
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
}
