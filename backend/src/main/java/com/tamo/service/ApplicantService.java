package com.tamo.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tamo.model.Application;
import com.tamo.model.DataStore;
import com.tamo.model.Job;
import com.tamo.model.Profile;
import com.tamo.model.User;
import com.tamo.util.JsonUtil;

import com.tamo.model.Notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ApplicantService.
 * Provides necessary operations and logic for the system.
 */
public class ApplicantService {

    private final JsonDataService dataService;

    /**
     * Constructs a new ApplicantService instance.
     */
    /**
     * Constructs a new ApplicantService instance.
     */
    public ApplicantService(JsonDataService dataService) {
        this.dataService = dataService;
    }

    /**
     * Executes the getDashboard operation.
     * @param userId the userId argument
     * @return the resulting object or status
     */
    /**
     * Executes the getDashboard operation.
     * @param userId the userId argument
     * @return the resulting object or status
     */
    public JsonObject getDashboard(String userId) { return getDashboard(userId,null); }
    public JsonObject getDashboard(String userId,String periodId) {
        DataStore data = dataService.getData();
        Profile profile = findProfileByUserId(data, userId);

        List<Application> myApps = data.getApplications().stream()
                .filter(a -> a.getApplicantId().equals(userId))
                .collect(Collectors.toList());

        long activeCount = myApps.stream()
                .filter(a -> !"rejected".equals(a.getStatus()) && !"offer".equals(a.getStatus()))
                .count();


        int profileCompletion = profile != null ? profile.getProfileCompletion() : 0;

        JsonObject result = new JsonObject();
        result.addProperty("activeApplications", activeCount);
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
                    appObj.addProperty("status", RecruitmentStatus.current(app.getStatus()));
                    appObj.addProperty("appliedAt", app.getAppliedAt());
                    enrichApplicationForApplicantApi(appObj, job, app.getJobId(), app.getAppliedAt());
                    recentApps.add(appObj);
                });
        result.add("recentApplications", recentApps);

        JsonObject actualWorkload=new AssignmentReportingService(data).workload(userId,periodId,false);
        result.add("workload",actualWorkload);
        result.add("offers",actualWorkload.getAsJsonArray("assignments"));
        result.addProperty("periodId",actualWorkload.get("periodId").getAsString());
        result.add("periods",JsonUtil.getGson().toJsonTree(data.getApplicationPeriods()));
        JsonArray schedule = new JsonArray();
        for (var assignment : actualWorkload.getAsJsonArray("assignments")) {
            JsonObject entry = assignment.getAsJsonObject().deepCopy();
            entry.addProperty("type", "duty"); schedule.add(entry);
        }
        result.add("schedule", schedule);

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
                    .filter(n -> RecruitmentStatus.visibleNotification(n.getType()))
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
     * Get job list, support search, department/type filtering and status filtering (active / closed)

     */
    /**
     * Executes the getJobs operation.
     * @param keyword the keyword argument
     * @param department the department argument
     * @param type the type argument
     * @param status the status argument
     * @return the resulting object or status
     */
    /**
     * Executes the getJobs operation.
     * @param keyword the keyword argument
     * @param department the department argument
     * @param type the type argument
     * @param status the status argument
     * @return the resulting object or status
     */
    public List<JsonObject> getJobs(String keyword, String department, String type, String status) {
        DataStore data = dataService.getData();
        return data.getJobs().stream()
                .filter(j -> status == null || status.isEmpty() ||
                        j.getStatus().equalsIgnoreCase(status))
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
     * Get single job details (viewable in any status)

     */
    /**
     * Executes the getJobById operation.
     * @param jobId the jobId argument
     * @return the resulting object or status
     */
    /**
     * Executes the getJobById operation.
     * @param jobId the jobId argument
     * @return the resulting object or status
     */
    public Job getJobById(String jobId) {
        DataStore data = dataService.getData();
        return findJobById(data, jobId);
    }

    /**
     * Submit application, result returned in Map:
     * - "error" + "code" for failure
     * - "application" for success

     */
    /**
     * Executes the applyForJob operation.
     * @param userId the userId argument
     * @param jobId the jobId argument
     * @param coverLetter the coverLetter argument
     * @param documentIds the documentIds argument
     * @return the resulting object or status
     */
    /**
     * Executes the applyForJob operation.
     * @param userId the userId argument
     * @param jobId the jobId argument
     * @param coverLetter the coverLetter argument
     * @param documentIds the documentIds argument
     * @return the resulting object or status
     */
    public Map<String, Object> applyForJob(String userId, String jobId, String coverLetter, List<String> documentIds) {
        return applyForJob(userId, jobId, coverLetter, documentIds, null);
    }

    public Map<String, Object> applyForJob(String userId, String jobId, String coverLetter, List<String> documentIds, String expectedPeriodId) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        Job job = findJobById(data, jobId);
        if (job == null) {
            result.put("error", "Job not found");
            result.put("code", 404);
            return result;
        }

        if (!"active".equals(job.getStatus())) {
            result.put("error", "This position is closed and cannot be applied for");
            result.put("code", 400);
            return result;
        }

        if (coverLetter == null || coverLetter.isBlank()) {
            result.put("error", "Missing required field(s)");
            result.put("code", 400);
            return result;
        }

        com.tamo.model.ApplicationPeriod period = new ApplicationPeriodService(dataService).acceptingPeriod();
        if (period == null) {
            result.put("error", "Applications are not currently open. Please check the application period.");
            result.put("code", 403);
            return result;
        }

        if (expectedPeriodId != null && !expectedPeriodId.equals(period.getId())) {
            result.put("error", "The application period has changed. Refresh this page before applying.");
            result.put("code", 409);
            return result;
        }

        Job selectedJob = getJobById(jobId);
        if (selectedJob != null && !JobSectionService.acceptsPeriod(data, selectedJob, period.getId())) {
            result.put("error", "This posting has no active Sections in the current application cycle.");
            result.put("code", 409); return result;
        }

        boolean alreadyApplied = data.getApplications().stream()
                .anyMatch(a -> a.getApplicantId().equals(userId) && a.getJobId().equals(jobId)
                        && period.getId().equals(a.getPeriodId()));
        if (alreadyApplied) {
            result.put("error", "You have already applied for this position");
            result.put("code", 409);
            return result;
        }
   

        JsonObject qualificationSnapshot;
        try{qualificationSnapshot=new QualificationService(dataService).snapshot(userId);}
        catch(QualificationService.Problem e){result.put("error",e.getMessage());result.put("code",e.status);return result;}
        Profile submittedProfile=findProfileByUserId(data,userId);
        if(documentIds!=null&&documentIds.stream().anyMatch(id->submittedProfile.getDocuments()==null||submittedProfile.getDocuments().stream().noneMatch(doc->id.equals(doc.getId())))){result.put("error","Selected documents must belong to your profile.");result.put("code",400);return result;}
        qualificationSnapshot.getAsJsonObject("academicProfile").add("documents",JsonUtil.getGson().toJsonTree(submittedProfile.getDocuments()==null?List.of():submittedProfile.getDocuments().stream().filter(d->documentIds!=null&&documentIds.contains(d.getId())).toList()));
        com.tamo.model.WeeklyAvailability snapshot;
        try { snapshot = new AvailabilityService(dataService).submissionSnapshot(userId, period.getId()); }
        catch (AvailabilityService.Problem e) {
            result.put("error", e.getMessage()); result.put("code", e.status); return result;
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String today = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        Application app = new Application();
        app.setId(dataService.generateId("a", data.getApplications()));
        app.setJobId(jobId);
        app.setPeriodId(period.getId());
        app.setAvailabilitySnapshot(snapshot);
        app.setQualificationSnapshot(qualificationSnapshot);
        app.setApplicantId(userId);
        app.setStatus("applied");
        app.setCoverLetter(coverLetter);
        app.setDocumentIds(documentIds != null ? documentIds : new ArrayList<>());
        app.setMatchScore(0);
        app.setRiskFlag(null);
        app.setAiSummary("Review the submitted qualifications and explained matching criteria.");
        app.setAppliedAt(now);
        app.setUpdatedAt(now);

        List<Application.TimelineEntry> timeline = new ArrayList<>();
        Application.TimelineEntry t1 = new Application.TimelineEntry();
        t1.setStatus("applied"); t1.setDate(today); t1.setCompleted(true);
        timeline.add(t1);
        for (String s : new String[]{"review", "shortlisted", "offer"}) {
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

    /**
     * Executes the getMyApplications operation.
     * @param userId the userId argument
     * @return the resulting object or status
     */
    /**
     * Executes the getMyApplications operation.
     * @param userId the userId argument
     * @return the resulting object or status
     */
    public List<JsonObject> getMyApplications(String userId) { return getMyApplications(userId, null); }

    public List<JsonObject> getMyApplications(String userId, String periodId) {
        DataStore data = dataService.getData();
        return data.getApplications().stream()
                .filter(a -> a.getApplicantId().equals(userId))
                .filter(a -> periodId == null || periodId.isBlank() || periodId.equals(a.getPeriodId()))
                .sorted((a, b) -> b.getAppliedAt().compareTo(a.getAppliedAt()))
                .map(a -> {
                    JsonObject obj = RecruitmentStatus.project((JsonObject) JsonUtil.getGson().toJsonTree(a));
                    data.getApplicationPeriods().stream().filter(p -> p.getId().equals(a.getPeriodId())).findFirst()
                            .ifPresent(p -> obj.addProperty("periodName", p.getName()));
                    Job job = findJobById(data, a.getJobId());
                    enrichApplicationForApplicantApi(obj, job, a.getJobId(), a.getAppliedAt());
                    return obj;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get application details, result returned in Map:
     * - "notFound" = true : application does not exist
     * - "forbidden" = true : access denied
     * - "data" : application data

     */
    /**
     * Executes the getApplicationDetail operation.
     * @param userId the userId argument
     * @param appId the appId argument
     * @return the resulting object or status
     */
    /**
     * Executes the getApplicationDetail operation.
     * @param userId the userId argument
     * @param appId the appId argument
     * @return the resulting object or status
     */
    public Map<String, Object> getApplicationDetail(String userId, String appId) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        Application app = data.getApplications().stream()
                .filter(a -> appId.equals(a.getId()) && userId.equals(a.getApplicantId()))
                .findFirst().orElse(null);

        if (app == null) {
            boolean idExists = data.getApplications().stream().anyMatch(a -> appId.equals(a.getId()));
            if (idExists) {
                result.put("forbidden", true);
            } else {
                result.put("notFound", true);
            }
            return result;
        }

        JsonObject obj = RecruitmentStatus.project((JsonObject) JsonUtil.getGson().toJsonTree(app));
        Job job = findJobById(data, app.getJobId());
        enrichApplicationForApplicantApi(obj, job, app.getJobId(), app.getAppliedAt());
        result.put("data", obj);
        return result;
    }

    /**
     * Executes the withdrawApplication operation.
     * @param userId the userId argument
     * @param appId the appId argument
     * @return the resulting object or status
     */
    /**
     * Executes the withdrawApplication operation.
     * @param userId the userId argument
     * @param appId the appId argument
     * @return the resulting object or status
     */
    public Map<String, Object> withdrawApplication(String userId, String appId) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        Application app = data.getApplications().stream()
                .filter(a -> appId.equals(a.getId()) && userId.equals(a.getApplicantId()))
                .findFirst().orElse(null);

        if (app == null) {
            boolean idExists = data.getApplications().stream().anyMatch(a -> appId.equals(a.getId()));
            if (idExists) {
                result.put("error", "You are not authorized to withdraw this application");
                result.put("code", 403);
            } else {
                result.put("error", "Application not found");
                result.put("code", 404);
            }
            return result;
        }

        if ("offer".equals(app.getStatus()) || "rejected".equals(app.getStatus())) {
            result.put("error", "Withdrawal not allowed for current status");
            result.put("code", 400);
            return result;
        }

        if (SectionAssignmentService.hasHistory(data,"applications",appId)) {
            result.put("error","This application has assignment history and must be retained."); result.put("code",409);return result;
        }
        data.getApplications().remove(app);
        if (data.getInterviews() != null) {
            data.getInterviews().removeIf(i -> appId.equals(i.getApplicationId()));
        }
        dataService.save();

        result.put("success", true);
        return result;
    }

    /**
     * Executes the getProfile operation.
     * @param userId the userId argument
     * @return the resulting object or status
     */
    /**
     * Executes the getProfile operation.
     * @param userId the userId argument
     * @return the resulting object or status
     */
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
     * Update profile, return error message or null (success)

     */
    /**
     * Executes the updateProfile operation.
     * @param userId the userId argument
     * @param updates the updates argument
     * @return the resulting object or status
     */
    /**
     * Executes the updateProfile operation.
     * @param userId the userId argument
     * @param updates the updates argument
     * @return the resulting object or status
     */
    public String updateProfile(String userId, JsonObject updates) {
        DataStore data = dataService.getData();
        Profile profile = getOrCreateProfile(data, userId);
        User user = data.getUsers().stream()
                .filter(u -> u.getId().equals(userId)).findFirst().orElse(null);
        if (profile == null || user == null) return "Profile not found";

        if (updates.has("gpa") && updates.has("maxGpa")) {
            double gpa = updates.get("gpa").getAsDouble();
            double maxGpa = updates.get("maxGpa").getAsDouble();
            if (gpa > maxGpa) {
                return "GPA cannot exceed maximum";
            }
        } else if (updates.has("gpa")) {
            double gpa = updates.get("gpa").getAsDouble();
            if (gpa > profile.getMaxGpa()) {
                return "GPA cannot exceed maximum";
            }
        }

        if (updates.has("name")) user.setName(updates.get("name").getAsString());
        if (updates.has("phone")) user.setPhone(updates.get("phone").getAsString());
        if (updates.has("department")) user.setDepartment(updates.get("department").getAsString());
        if (updates.has("studentId")) profile.setStudentId(updates.get("studentId").getAsString());
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
     * Upload document (simulated), result returned in Map:
     * - "error" + "code" for failure
     * - "document" for success

     */
    /**
     * Executes the uploadDocument operation.
     * @param userId the userId argument
     * @param fileName the fileName argument
     * @param fileType the fileType argument
     * @param fileSize the fileSize argument
     * @return the resulting object or status
     */
    /**
     * Executes the uploadDocument operation.
     * @param userId the userId argument
     * @param fileName the fileName argument
     * @param fileType the fileType argument
     * @param fileSize the fileSize argument
     * @return the resulting object or status
     */
    public Map<String, Object> uploadDocument(String userId, String fileName, String fileType, String fileSize) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        if (fileName == null || fileName.isBlank()) {
            result.put("error", "No file uploaded");
            result.put("code", 400);
            return result;
        }

        String ext = "";
        int dotIdx = fileName.lastIndexOf('.');
        if (dotIdx >= 0) {
            ext = fileName.substring(dotIdx + 1).toLowerCase();
        }
        if (!ALLOWED_FILE_EXTENSIONS.contains(ext)) {
            result.put("error", "Unsupported file type");
            result.put("code", 400);
            return result;
        }

        Profile profile = getOrCreateProfile(data, userId);
        if (profile == null) {
            result.put("error", "Profile not found");
            result.put("code", 404);
            return result;
        }

        Profile.Document doc = new Profile.Document();
        doc.setId("doc_"+java.util.UUID.randomUUID().toString().replace("-",""));
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
     * Delete document, result returned in Map:
     * - "error" + "code" for failure
     * - "success" = true for success

     */
    /**
     * Executes the deleteDocument operation.
     * @param userId the userId argument
     * @param docId the docId argument
     * @return the resulting object or status
     */
    /**
     * Executes the deleteDocument operation.
     * @param userId the userId argument
     * @param docId the docId argument
     * @return the resulting object or status
     */
    public Map<String, Object> deleteDocument(String userId, String docId) {
        DataStore data = dataService.getData();
        Map<String, Object> result = new HashMap<>();

        Profile profile = findProfileByUserId(data, userId);
        if (profile == null) {
            result.put("error", "Profile not found");
            result.put("code", 404);
            return result;
        }

        if(data.getApplications().stream().anyMatch(app->userId.equals(app.getApplicantId())&&app.getDocumentIds()!=null&&app.getDocumentIds().contains(docId))){result.put("error","This document belongs to a submitted application and must be retained.");result.put("code",409);return result;}
        boolean removed = profile.getDocuments().removeIf(d -> d.getId().equals(docId));
        if (!removed) {
            result.put("error", "Document not found");
            result.put("code", 404);
            return result;
        }

        dataService.save();
        result.put("success", true);
        return result;
    }

    /**
     * Agreed with frontend: jobTitle, moduleCode, appliedDate; role, module kept for backward compatibility.

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

    /**
     * Executes the getNotifications operation.
     * @param userId the userId argument
     * @return the resulting object or status
     */
    /**
     * Executes the getNotifications operation.
     * @param userId the userId argument
     * @return the resulting object or status
     */
    public List<JsonObject> getNotifications(String userId) {
        DataStore data = dataService.getData();
        List<Notification> notifications = (data.getNotifications() != null
                ? data.getNotifications() : Collections.<Notification>emptyList())
                .stream()
                .filter(n -> userId.equals(n.getUserId()))
                    .filter(n -> RecruitmentStatus.visibleNotification(n.getType()))
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

    /**
     * Executes the markNotificationRead operation.
     * @param userId the userId argument
     * @param notifId the notifId argument
     */
    /**
     * Executes the markNotificationRead operation.
     * @param userId the userId argument
     * @param notifId the notifId argument
     */
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

    /**
     * Executes the markAllNotificationsRead operation.
     * @param userId the userId argument
     */
    /**
     * Executes the markAllNotificationsRead operation.
     * @param userId the userId argument
     */
    public void markAllNotificationsRead(String userId) {
        DataStore data = dataService.getData();
        if (data.getNotifications() == null) return;
        data.getNotifications().stream()
                .filter(n -> n.getUserId().equals(userId) && !n.isRead())
                .forEach(n -> n.setRead(true));
        dataService.save();
    }
}
