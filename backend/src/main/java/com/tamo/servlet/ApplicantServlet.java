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
public class ApplicantServlet extends HttpServlet {

    private ApplicantService service;
    private JsonDataService dataService;
    private static final Path UPLOAD_DIR = Paths.get("src", "main", "webapp", "static", "uploads", "organiser");

    @Override
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = JsonUtil.getPathInfo(req);
        String userId = getUserId(req);

        if ("dashboard".equals(path)) {
            JsonUtil.writeJson(resp, service.getDashboard(userId));

        } else if ("jobs".equals(path)) {
            String keyword = req.getParameter("keyword");
            String department = req.getParameter("department");
            String type = req.getParameter("type");
            JsonUtil.writeJson(resp, service.getJobs(keyword, department, type));

        } else if (path.matches("jobs/[\\w]+")) {
            String jobId = path.split("/")[1];
            Job job = service.getJobById(jobId);
            if (job == null) {
                JsonUtil.writeError(resp, 404, "职位不存在");
            } else {
                JsonUtil.writeJson(resp, job);
            }

        } else if ("applications".equals(path)) {
            JsonUtil.writeJson(resp, service.getMyApplications(userId));

        } else if (path.matches("applications/[\\w]+")) {
            String appId = path.split("/")[1];
            Map<String, Object> result = service.getApplicationDetail(userId, appId);
            if (result.containsKey("notFound")) {
                JsonUtil.writeError(resp, 404, "申请不存在");
            } else if (result.containsKey("forbidden")) {
                JsonUtil.writeError(resp, 403, "无权限访问该申请");
            } else {
                JsonUtil.writeJson(resp, result.get("data"));
            }

        } else if ("profile".equals(path)) {
            JsonObject profile = service.getProfile(userId);
            if (profile == null) {
                JsonUtil.writeError(resp, 404, "资料不存在");
            } else {
                JsonUtil.writeJson(resp, profile);
            }

        } else if ("notifications".equals(path)) {
            JsonUtil.writeJson(resp, service.getNotifications(userId));

        } else {
            JsonUtil.writeError(resp, 404, "接口不存在");
        }
    }

    @Override
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

            Map<String, Object> result = service.applyForJob(userId, jobId, coverLetter, documentIds);
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
            // Real file upload via multipart/form-data
            String fileName = null;
            String fileType = "cv";
            long fileSize = 0;

            try {
                Part filePart = req.getPart("file");
                if (filePart != null) {
                    fileName = filePart.getSubmittedFileName();
                    if (fileName == null || fileName.isBlank()) {
                        // Fallback: JSON body for backward compatibility
                        JsonObject body = JsonUtil.readRequestBodyAsJson(req);
                        fileName = body.has("fileName") ? body.get("fileName").getAsString() : null;
                        fileType = body.has("fileType") ? body.get("fileType").getAsString() : "cv";
                        String sizeStr = body.has("fileSize") ? body.get("fileSize").getAsString() : "0 MB";
                        Map<String, Object> result = service.uploadDocument(userId, fileName, fileType, sizeStr);
                        if (result.containsKey("error")) {
                            JsonUtil.writeError(resp, (int) result.get("code"), (String) result.get("error"));
                        } else {
                            resp.setStatus(201);
                            JsonUtil.writeJson(resp, result.get("document"));
                        }
                        return;
                    }

                    fileSize = filePart.getSize();
                    // Ensure upload directory exists
                    Files.createDirectories(UPLOAD_DIR);
                    Path targetPath = UPLOAD_DIR.resolve(fileName);
                    try (InputStream is = filePart.getInputStream()) {
                        Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }

                    // Determine file type from extension
                    String ext = "";
                    int dotIdx = fileName.lastIndexOf('.');
                    if (dotIdx >= 0) ext = fileName.substring(dotIdx + 1).toLowerCase();
                    fileType = ext.isEmpty() ? "file" : ext;

                    // Format file size for display
                    String sizeStr;
                    if (fileSize < 1024) sizeStr = fileSize + " B";
                    else if (fileSize < 1024 * 1024) sizeStr = String.format("%.1f KB", fileSize / 1024.0);
                    else sizeStr = String.format("%.1f MB", fileSize / (1024.0 * 1024.0));

                    Map<String, Object> result = service.uploadDocument(userId, fileName, fileType, sizeStr);
                    if (result.containsKey("error")) {
                        // Clean up file if metadata save fails
                        Files.deleteIfExists(targetPath);
                        JsonUtil.writeError(resp, (int) result.get("code"), (String) result.get("error"));
                    } else {
                        resp.setStatus(201);
                        JsonUtil.writeJson(resp, result.get("document"));
                        dataService.addLog("Document Uploaded", "info", getUserName(req), userId,
                                "Uploaded document: " + fileName + " (" + sizeStr + ")", req.getRemoteAddr());
                    }
                } else {
                    JsonUtil.writeError(resp, 400, "没有上传文件");
                }
            } catch (Exception e) {
                JsonUtil.writeError(resp, 500, "上传失败: " + e.getMessage());
            }

        } else {
            JsonUtil.writeError(resp, 404, "接口不存在");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = JsonUtil.getPathInfo(req);
        String userId = getUserId(req);

        if ("profile".equals(path)) {
            JsonObject body = JsonUtil.readRequestBodyAsJson(req);
            String error = service.updateProfile(userId, body);
            if (error == null) {
                JsonObject profile = service.getProfile(userId);
                JsonUtil.writeJson(resp, profile);
                dataService.addLog("Profile Updated", "info", getUserName(req), userId,
                        "Applicant updated their profile", req.getRemoteAddr());
            } else if ("GPA 不能超过最大值".equals(error)) {
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
            JsonUtil.writeError(resp, 404, "接口不存在");
        }
    }

    @Override
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
                ok.addProperty("message", "申请已撤销");
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
                ok.addProperty("message", "文档删除成功");
                JsonUtil.writeJson(resp, ok);
                dataService.addLog("Document Deleted", "warning", getUserName(req), userId,
                        "Deleted document " + docId, req.getRemoteAddr());
            }
        } else {
            JsonUtil.writeError(resp, 404, "接口不存在");
        }
    }
}
