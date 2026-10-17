package com.tamo.servlet;

import com.google.gson.JsonObject;
import com.tamo.service.JsonDataService;
import com.tamo.service.OrganiserService;
import com.tamo.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Implementation of OrganiserServlet.
 * Provides necessary operations and logic for the system.
 */
public class OrganiserServlet extends HttpServlet {

    private OrganiserService service;
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
        this.service = new OrganiserService(dataService);
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

        if ("dashboard".equals(path)) {
            JsonUtil.writeJson(resp, service.getDashboard(userId));

        } else if ("jobs".equals(path)) {
            JsonUtil.writeJson(resp, service.getJobs(userId));

        } else if ("modules".equals(path)) {
            JsonUtil.writeJson(resp, service.getMyModules(userId));

        } else if ("pipeline".equals(path)) {
            String module = req.getParameter("module");
            JsonUtil.writeJson(resp, service.getPipeline(userId, module, req.getParameter("periodId")));

        } else if (path.matches("applications/[\\w]+")) {
            String appId = path.split("/")[1];
            Map<String, Object> result = service.getApplicationDetails(userId, appId);
            if (result.containsKey("error")) {
                int code = (int) result.get("code");
                JsonUtil.writeError(resp, code, (String) result.get("error"));
            } else {
                JsonUtil.writeJson(resp, result.get("data"));
            }

        } else if ("notifications".equals(path)) {
            JsonUtil.writeJson(resp, service.getNotifications(userId));

        } else if ("interviews".equals(path)) {
            JsonUtil.writeJson(resp, service.getInterviews(userId));

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

        if ("jobs".equals(path)) {
            JsonObject body = JsonUtil.readRequestBodyAsJson(req);
            Map<String, Object> result = service.createJob(userId, body);
            if (result.containsKey("error")) {
                int code = (int) result.get("code");
                JsonUtil.writeError(resp, code, (String) result.get("error"));
            } else {
                resp.setStatus(201);
                JsonUtil.writeJson(resp, result.get("job"));
                String title = body.has("title") ? body.get("title").getAsString() : "";
                dataService.addLog("Job Posted", "success", getUserName(req), userId,
                        "Posted new job: " + title, req.getRemoteAddr());
            }

        } else if ("interviews".equals(path)) {
            JsonObject body = JsonUtil.readRequestBodyAsJson(req);
            Map<String, Object> result = service.createInterview(userId, body);
            if (result.containsKey("error")) {
                int code = (int) result.get("code");
                JsonUtil.writeError(resp, code, (String) result.get("error"));
            } else {
                resp.setStatus(201);
                JsonUtil.writeJson(resp, result.get("interview"));
                String appId = body.has("applicationId") ? body.get("applicationId").getAsString() : "";
                dataService.addLog("Interview Scheduled", "info", getUserName(req), userId,
                        "Scheduled interview for application " + appId, req.getRemoteAddr());
            }

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

        if (path.matches("jobs/[\\w]+")) {
            String jobId = path.split("/")[1];
            JsonObject body = JsonUtil.readRequestBodyAsJson(req);
            Map<String, Object> result = service.updateJob(userId, jobId, body);
            if (result.containsKey("error")) {
                int code = (int) result.get("code");
                JsonUtil.writeError(resp, code, (String) result.get("error"));
            } else {
                JsonUtil.writeJson(resp, result.get("job"));
                dataService.addLog("Job Updated", "info", getUserName(req), userId,
                        "Updated job " + jobId, req.getRemoteAddr());
            }

        } else if (path.matches("pipeline/[\\w]+/status")) {
            String appId = path.split("/")[1];
            JsonObject body = JsonUtil.readRequestBodyAsJson(req);
            String newStatus = body.has("status") ? body.get("status").getAsString() : null;
            Map<String, Object> result = service.updateCandidateStatus(userId, appId, newStatus);
            if (result.containsKey("error")) {
                int code = (int) result.get("code");
                JsonUtil.writeError(resp, code, (String) result.get("error"));
            } else {
                JsonUtil.writeJson(resp, result.get("application"));
                dataService.addLog("Candidate Status Changed", "info", getUserName(req), userId,
                        "Changed application " + appId + " status to " + newStatus, req.getRemoteAddr());
            }

        } else if (path.matches("applications/[\\w]+/action")) {
            String appId = path.split("/")[1];
            JsonObject body = JsonUtil.readRequestBodyAsJson(req);
            String action = body.has("action") ? body.get("action").getAsString() : null;
            Map<String, Object> result = service.applicationAction(userId, appId, action);
            if (result.containsKey("error")) {
                int code = (int) result.get("code");
                JsonUtil.writeError(resp, code, (String) result.get("error"));
            } else {
                JsonUtil.writeJson(resp, result.get("application"));
                dataService.addLog("Application " + (action != null ? action.substring(0, 1).toUpperCase() + action.substring(1) : "Action"),
                        "approve".equals(action) ? "success" : "warning", getUserName(req), userId,
                        "Application " + appId + " " + action, req.getRemoteAddr());
            }

        } else if (path.matches("interviews/[\\w]+")) {
            String ivId = path.split("/")[1];
            JsonObject body = JsonUtil.readRequestBodyAsJson(req);
            Map<String, Object> result = service.updateInterview(userId, ivId, body);
            if (result.containsKey("error")) {
                int code = (int) result.get("code");
                JsonUtil.writeError(resp, code, (String) result.get("error"));
            } else {
                JsonUtil.writeJson(resp, result.get("interview"));
                dataService.addLog("Interview Updated", "info", getUserName(req), userId,
                        "Updated interview " + ivId, req.getRemoteAddr());
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

        if (path.matches("jobs/[\\w]+")) {
            String jobId = path.split("/")[1];
            Map<String, Object> result = service.deleteJob(userId, jobId);
            if (result.containsKey("error")) {
                int code = (int) result.get("code");
                JsonUtil.writeError(resp, code, (String) result.get("error"));
            } else {
                JsonObject ok = new JsonObject();
                ok.addProperty("message", "Job deleted successfully");
                JsonUtil.writeJson(resp, ok);
                dataService.addLog("Job Deleted", "warning", getUserName(req), userId,
                        "Deleted job " + jobId, req.getRemoteAddr());
            }
        } else if (path.matches("interviews/[\\w]+")) {
            String interviewId = path.split("/")[1];
            Map<String, Object> result = service.deleteInterview(userId, interviewId);
            if (result.containsKey("error")) {
                int code = (int) result.get("code");
                JsonUtil.writeError(resp, code, (String) result.get("error"));
            } else {
                JsonObject ok = new JsonObject();
                ok.addProperty("message", "Interview deleted successfully");
                JsonUtil.writeJson(resp, ok);
                dataService.addLog("Interview Deleted", "warning", getUserName(req), userId,
                        "Deleted interview " + interviewId, req.getRemoteAddr());
            }
        } else {
            JsonUtil.writeError(resp, 404, "Endpoint not found");
        }
    }
}
