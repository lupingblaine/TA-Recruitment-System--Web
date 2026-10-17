package com.tamo.servlet;

import com.google.gson.JsonObject;
import com.tamo.model.Job;
import com.tamo.service.ApplicantService;
import com.tamo.service.JsonDataService;
import com.tamo.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,      // 1 MB in memory
    maxFileSize = 10 * 1024 * 1024,       // 10 MB max per file
    maxRequestSize = 15 * 1024 * 1024     // 15 MB max total request
)
/**
 * Implementation of ApplicantServlet.
 * Provides necessary operations and logic for the system.
 */
public class ApplicantServlet extends HttpServlet {

    private ApplicantService service;
    private JsonDataService dataService;

    @Override
    /**
     * Executes the init operation.
     */
    /**
     * Executes the init operation.
     */
    public void init() throws ServletException {
        this.dataService = JsonDataService.getInstance();
        this.service = new ApplicantService(dataService);
    }

    private String getUserId(HttpServletRequest req) {
        return (String) req.getSession().getAttribute("userId");
    }

    private String getUserName(HttpServletRequest req) {
        return (String) req.getSession().getAttribute("userName");
    }

    @Override
    /**
     * Executes the doGet operation.
     * @param req the req argument
     * @param resp the resp argument
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = JsonUtil.getPathInfo(req);
        String userId = getUserId(req);

        if("qualification-options".equals(path)){JsonUtil.writeJson(resp,com.tamo.service.SkillCatalogService.options(dataService.getData()));
        } else if ("availability-periods".equals(path)) {
            JsonUtil.writeJson(resp, new com.tamo.service.AvailabilityService(dataService).periods());
        } else if (path.matches("availability/[\\w]+")) {
            try { JsonUtil.writeJson(resp, new com.tamo.service.AvailabilityService(dataService).get(userId, path.split("/")[1])); }
            catch (com.tamo.service.AvailabilityService.Problem e) { JsonUtil.writeError(resp, e.status, e.getMessage()); }
        } else if ("application-period".equals(path)) {
            JsonUtil.writeJson(resp, new com.tamo.service.ApplicationPeriodService(dataService).current());
        } else if ("dashboard".equals(path)) {
            JsonUtil.writeJson(resp, service.getDashboard(userId,req.getParameter("periodId")));

        } else if ("jobs".equals(path)) {
            String keyword = req.getParameter("keyword");
            String department = req.getParameter("department");
            String type = req.getParameter("type");
            String status = req.getParameter("status");
            JsonUtil.writeJson(resp, service.getJobs(keyword, department, type, status));

        } else if (path.matches("jobs/[\\w]+")) {
            String jobId = path.split("/")[1];
            Job job = service.getJobById(jobId);
            if (job == null) {
                JsonUtil.writeError(resp, 404, "Job not found");
            } else {
                JsonObject detail=JsonUtil.getGson().toJsonTree(job).getAsJsonObject();
                detail.add("sections", JsonUtil.getGson().toJsonTree(dataService.getData().getCourseSections().stream()
                    .filter(s->java.util.Objects.equals(s.getModuleId(),job.getModuleId()) && "active".equals(s.getStatus()) && com.tamo.service.JobSectionService.includes(job,s)).toList()));
                JsonUtil.writeJson(resp, detail);
            }

        } else if ("applications".equals(path)) {
            JsonUtil.writeJson(resp, service.getMyApplications(userId, req.getParameter("periodId")));

        } else if (path.matches("applications/[\\w]+")) {
            String appId = path.split("/")[1];
            Map<String, Object> result = service.getApplicationDetail(userId, appId);
            if (result.containsKey("notFound")) {
                JsonUtil.writeError(resp, 404, "Application not found");
            } else if (result.containsKey("forbidden")) {
                JsonUtil.writeError(resp, 403, "You are not authorized to access this application");
            } else {
                JsonUtil.writeJson(resp, result.get("data"));
            }

        } else if ("qualifications".equals(path)) {
            try{JsonUtil.writeJson(resp,new com.tamo.service.QualificationService(dataService).get(userId));}catch(com.tamo.service.QualificationService.Problem e){JsonUtil.writeError(resp,e.status,e.getMessage());}
        } else if ("profile".equals(path)) {
            JsonObject profile = service.getProfile(userId);
            if (profile == null) {
                JsonUtil.writeError(resp, 404, "Profile not found");
            } else {
                JsonUtil.writeJson(resp, profile);
            }

        } else if ("notifications".equals(path)) {
            JsonUtil.writeJson(resp, service.getNotifications(userId));

        } else {
            JsonUtil.writeError(resp, 404, "Endpoint not found");
        }
    }

    @Override
    /**
     * Executes the doPost operation.
     * @param req the req argument
     * @param resp the resp argument
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = JsonUtil.getPathInfo(req);
        String userId = getUserId(req);

        if (path.matches("jobs/[\\w]+/apply")) {
            String jobId = path.split("/")[1];
            JsonObject body = JsonUtil.readRequestBodyAsJson(req);
            String coverLetter = body.has("coverLetter") ? body.get("coverLetter").getAsString() : null;
            List<String> documentIds = new java.util.ArrayList<>();
            if (body.has("documentIds")) {
                body.getAsJsonArray("documentIds").forEach(e -> documentIds.add(e.getAsString()));
            }

            Map<String, Object> result = service.applyForJob(userId, jobId, coverLetter, documentIds,
                    body.has("periodId") && !body.get("periodId").isJsonNull() ? body.get("periodId").getAsString() : null);
            if (result.containsKey("error")) {
                int code = (int) result.get("code");
                JsonUtil.writeError(resp, code, (String) result.get("error"));
            } else {
                resp.setStatus(201);
                JsonUtil.writeJson(resp, result.get("application"));
                dataService.addLog("Application Submitted", "success", getUserName(req), userId,
                        "Applied for job " + jobId, req.getRemoteAddr());
            }

        } else if ("profile/documents".equals(path)) {
            if(req.getContentType()==null||!req.getContentType().toLowerCase(java.util.Locale.ROOT).startsWith("multipart/form-data")){JsonUtil.writeError(resp,415,"Upload an actual file using multipart/form-data.");return;}
            try {
                Part part=req.getPart("file");String name=part==null?null:part.getSubmittedFileName();
                if(name==null||name.isBlank()||name.contains("/")||name.contains("\\")||name.contains("\r")||name.contains("\n")||name.length()>200){JsonUtil.writeError(resp,400,"Use a valid file name without directory paths.");return;}
                String ext=name.substring(name.lastIndexOf('.')+1).toLowerCase(java.util.Locale.ROOT);
                if(!java.util.Set.of("pdf","doc","docx","txt","png","jpg","jpeg").contains(ext)||part.getSize()==0||part.getSize()>10*1024*1024){JsonUtil.writeError(resp,400,"Upload a supported non-empty file up to 10 MB.");return;}
                Files.createDirectories(com.tamo.service.PrivateDocuments.ROOT);String key=java.util.UUID.randomUUID().toString()+"."+ext;Path target=com.tamo.service.PrivateDocuments.ROOT.resolve(key);
                try(InputStream input=part.getInputStream()){Files.copy(input,target);}
                Map<String,Object> result=service.uploadDocument(userId,name,ext,part.getSize()+" bytes");
                if(result.containsKey("error")){Files.deleteIfExists(target);JsonUtil.writeError(resp,(int)result.get("code"),(String)result.get("error"));return;}
                com.tamo.model.Profile.Document doc=(com.tamo.model.Profile.Document)result.get("document");doc.setStorageKey(key);dataService.save();resp.setStatus(201);JsonUtil.writeJson(resp,doc);
                dataService.addLog("Document Uploaded","info",getUserName(req),userId,"Uploaded private document "+doc.getId(),req.getRemoteAddr());
            }catch(IllegalStateException e){JsonUtil.writeError(resp,400,"File exceeds the upload limit.");}catch(Exception e){getServletContext().log("Private upload failed",e);JsonUtil.writeError(resp,500,"Upload could not be saved.");}

        } else {
            JsonUtil.writeError(resp, 404, "Endpoint not found");
        }
    }

    @Override
    /**
     * Executes the doPut operation.
     * @param req the req argument
     * @param resp the resp argument
     */
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = JsonUtil.getPathInfo(req);
        String userId = getUserId(req);

        if (path.matches("availability/[\\w]+")) {
            try {
                var availability = new com.tamo.service.AvailabilityService(dataService).save(userId, path.split("/")[1], JsonUtil.readRequestBodyAsJson(req));
                dataService.addLog("Availability Updated", "info", getUserName(req), userId,
                        "Updated weekly availability for period " + availability.getPeriodId(), req.getRemoteAddr());
                JsonUtil.writeJson(resp, availability);
            } catch (com.tamo.service.AvailabilityService.Problem e) { JsonUtil.writeError(resp, e.status, e.getMessage()); }
            catch (com.google.gson.JsonParseException e) { JsonUtil.writeError(resp, 400, "Invalid JSON request."); }
        } else if ("qualifications".equals(path)) {
            try{JsonUtil.writeJson(resp,new com.tamo.service.QualificationService(dataService).save(userId,JsonUtil.readRequestBodyAsJson(req)));}catch(com.tamo.service.QualificationService.Problem e){JsonUtil.writeError(resp,e.status,e.getMessage());}
        } else if ("profile".equals(path)) {
            JsonObject body = JsonUtil.readRequestBodyAsJson(req);
            String error = service.updateProfile(userId, body);
            if (error == null) {
                JsonObject profile = service.getProfile(userId);
                JsonUtil.writeJson(resp, profile);
                dataService.addLog("Profile Updated", "info", getUserName(req), userId,
                        "Applicant updated their profile", req.getRemoteAddr());
            } else if ("GPA cannot exceed maximum".equals(error)) {
                JsonUtil.writeError(resp, 400, error);
            } else {
                JsonUtil.writeError(resp, 404, error);
            }
        } else if (path.matches("notifications/[\\w]+/read")) {
            String notifId = path.split("/")[1];
            service.markNotificationRead(userId, notifId);
            JsonObject ok = new JsonObject();
            ok.addProperty("success", true);
            JsonUtil.writeJson(resp, ok);
        } else if ("notifications/read-all".equals(path)) {
            service.markAllNotificationsRead(userId);
            JsonObject ok = new JsonObject();
            ok.addProperty("success", true);
            JsonUtil.writeJson(resp, ok);
        } else {
            JsonUtil.writeError(resp, 404, "Endpoint not found");
        }
    }

    @Override
    /**
     * Executes the doDelete operation.
     * @param req the req argument
     * @param resp the resp argument
     */
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = JsonUtil.getPathInfo(req);
        String userId = getUserId(req);

        if (path.matches("applications/[\\w]+")) {
            String appId = path.split("/")[1];
            Map<String, Object> result = service.withdrawApplication(userId, appId);
            if (result.containsKey("error")) {
                int code = (int) result.get("code");
                JsonUtil.writeError(resp, code, (String) result.get("error"));
            } else {
                JsonObject ok = new JsonObject();
                ok.addProperty("message", "Application withdrawn");
                JsonUtil.writeJson(resp, ok);
                dataService.addLog("Application Withdrawn", "warning", getUserName(req), userId,
                        "Withdrew application " + appId, req.getRemoteAddr());
            }
        } else if (path.matches("profile/documents/[\\w]+")) {
            String docId = path.split("/")[2];
            Map<String, Object> result = service.deleteDocument(userId, docId);
            if (result.containsKey("error")) {
                int code = (int) result.get("code");
                JsonUtil.writeError(resp, code, (String) result.get("error"));
            } else {
                JsonObject ok = new JsonObject();
                ok.addProperty("message", "Document deleted successfully");
                JsonUtil.writeJson(resp, ok);
                dataService.addLog("Document Deleted", "warning", getUserName(req), userId,
                        "Deleted document " + docId, req.getRemoteAddr());
            }
        } else {
            JsonUtil.writeError(resp, 404, "Endpoint not found");
        }
    }
}
