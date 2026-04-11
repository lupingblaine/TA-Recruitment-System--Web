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

public class OrganiserServlet extends HttpServlet {

    private OrganiserService service;

    @Override
    public void init() throws ServletException {
        JsonDataService dataService = JsonDataService.getInstance();
        this.service = new OrganiserService(dataService);
    }

    private String getUserId(HttpServletRequest req) {
        return (String) req.getSession().getAttribute("userId");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = JsonUtil.getPathInfo(req);
        String userId = getUserId(req);

        if ("dashboard".equals(path)) {
            JsonUtil.writeJson(resp, service.getDashboard(userId));

        } else if ("jobs".equals(path)) {
            JsonUtil.writeJson(resp, service.getJobs(userId));

        } else if ("pipeline".equals(path)) {
            String module = req.getParameter("module");
            JsonUtil.writeJson(resp, service.getPipeline(userId, module));

        } else if (path.matches("applications/[\\w]+")) {
            String appId = path.split("/")[1];
            Map<String, Object> result = service.getApplicationDetails(userId, appId);
            if (result.containsKey("error")) {
                int code = (int) result.get("code");
                JsonUtil.writeError(resp, code, (String) result.get("error"));
            } else {
                JsonUtil.writeJson(resp, result.get("data"));
            }

        } else if ("interviews".equals(path)) {
            JsonUtil.writeJson(resp, service.getInterviews(userId));

        } else {
            JsonUtil.writeError(resp, 404, "接口不存在");
        }
    }

    @Override
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
            }

        } else {
            JsonUtil.writeError(resp, 404, "接口不存在");
        }
    }

    @Override
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
            }

        } else {
            JsonUtil.writeError(resp, 404, "接口不存在");
        }
    }

    @Override
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
                ok.addProperty("message", "岗位删除成功");
                JsonUtil.writeJson(resp, ok);
            }
        } else {
            JsonUtil.writeError(resp, 404, "接口不存在");
        }
    }
}
