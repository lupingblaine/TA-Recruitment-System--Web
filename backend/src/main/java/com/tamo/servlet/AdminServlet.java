package com.tamo.servlet;

import com.google.gson.JsonObject;
import com.tamo.model.Module;
import com.tamo.service.AdminService;
import com.tamo.service.JsonDataService;
import com.tamo.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class AdminServlet extends HttpServlet {

    private AdminService service;
    private JsonDataService dataService;

    @Override
    public void init() throws ServletException {
        this.dataService = JsonDataService.getInstance();
        this.service = new AdminService(dataService);
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

        switch (path) {
            case "dashboard" -> JsonUtil.writeJson(resp, service.getDashboard());

            case "users" -> {
                String keyword = req.getParameter("keyword");
                String role = req.getParameter("role");
                String status = req.getParameter("status");
                JsonUtil.writeJson(resp, service.getUsers(keyword, role, status));
            }

            case "modules" -> {
                String keyword = req.getParameter("keyword");
                JsonUtil.writeJson(resp, service.getModules(keyword));
            }

            case "properties/terms" -> JsonUtil.writeJson(resp, service.getTerms());

            case "properties/departments" -> JsonUtil.writeJson(resp, service.getDepartments());

            case "logs" -> {
                String keyword = req.getParameter("keyword");
                String type = req.getParameter("type");
                JsonUtil.writeJson(resp, service.getLogs(keyword, type));
            }

            case "workload" -> {
                String keyword = req.getParameter("keyword");
                String status = req.getParameter("status");
                JsonUtil.writeJson(resp, service.getWorkload(keyword, status));
            }

            default -> JsonUtil.writeError(resp, 404, "接口不存在");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = JsonUtil.getPathInfo(req);
        JsonObject body = JsonUtil.readRequestBodyAsJson(req);
        String actor = getUserName(req);
        String actorId = getUserId(req);
        String ip = req.getRemoteAddr();

        switch (path) {
            case "users" -> {
                Map<String, Object> result = service.createUser(body);
                if (result.containsKey("error")) {
                    int code = (int) result.get("code");
                    JsonUtil.writeError(resp, code, (String) result.get("error"));
                } else {
                    resp.setStatus(201);
                    JsonUtil.writeJson(resp, result.get("user"));
                    String newName = body.has("name") ? body.get("name").getAsString() : "";
                    dataService.addLog("User Created", "success", actor, actorId,
                            "Created user: " + newName, ip);
                }
            }

            case "modules" -> {
                Map<String, Object> result = service.createModule(body);
                if (result.containsKey("error")) {
                    int code = (int) result.get("code_http");
                    JsonUtil.writeError(resp, code, (String) result.get("error"));
                } else {
                    resp.setStatus(201);
                    JsonUtil.writeJson(resp, result.get("module"));
                    String modCode = body.has("code") ? body.get("code").getAsString() : "";
                    String modName = body.has("name") ? body.get("name").getAsString() : "";
                    dataService.addLog("Module Created", "success", actor, actorId,
                            "Created module: " + modCode + " - " + modName, ip);
                }
            }

            case "properties/terms" -> {
                Map<String, Object> result = service.createTerm(body);
                if (result.containsKey("error")) {
                    int code = (int) result.get("code");
                    JsonUtil.writeError(resp, code, (String) result.get("error"));
                } else {
                    resp.setStatus(201);
                    JsonUtil.writeJson(resp, result.get("term"));
                    String termName = body.has("name") ? body.get("name").getAsString() : "";
                    dataService.addLog("Term Created", "success", actor, actorId,
                            "Created term: " + termName, ip);
                }
            }

            case "properties/departments" -> {
                Map<String, Object> result = service.createDepartment(body);
                if (result.containsKey("error")) {
                    int code = (int) result.get("code");
                    JsonUtil.writeError(resp, code, (String) result.get("error"));
                } else {
                    resp.setStatus(201);
                    JsonUtil.writeJson(resp, result.get("department"));
                    String deptName = body.has("name") ? body.get("name").getAsString() : "";
                    dataService.addLog("Department Created", "success", actor, actorId,
                            "Created department: " + deptName, ip);
                }
            }

            default -> JsonUtil.writeError(resp, 404, "接口不存在");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = JsonUtil.getPathInfo(req);
        String adminId = getUserId(req);
        String actor = getUserName(req);
        String ip = req.getRemoteAddr();
        JsonObject body = JsonUtil.readRequestBodyAsJson(req);

        if (path.matches("users/[\\w]+")) {
            String userId = path.split("/")[1];
            Map<String, Object> result = service.updateUser(adminId, userId, body);
            if (result.containsKey("error")) {
                int code = (int) result.get("code");
                JsonUtil.writeError(resp, code, (String) result.get("error"));
            } else {
                JsonUtil.writeJson(resp, result.get("user"));
                dataService.addLog("User Updated", "info", actor, adminId,
                        "Updated user " + userId, ip);
            }

        } else if (path.matches("modules/[\\w]+")) {
            String moduleId = path.split("/")[1];
            Module module = service.updateModule(moduleId, body);
            if (module == null) {
                JsonUtil.writeError(resp, 404, "模块不存在");
            } else {
                JsonUtil.writeJson(resp, module);
                dataService.addLog("Module Updated", "info", actor, adminId,
                        "Updated module " + moduleId + " (" + module.getCode() + ")", ip);
            }

        } else if (path.matches("workload/[\\w]+/reassign")) {
            String taId = path.split("/")[1];
            Map<String, Object> result = service.reassignWorkload(taId, body);
            if (result.containsKey("error")) {
                int code = (int) result.get("code");
                JsonUtil.writeError(resp, code, (String) result.get("error"));
            } else {
                JsonObject ok = new JsonObject();
                ok.addProperty("message", "重新分配成功");
                JsonUtil.writeJson(resp, ok);
                dataService.addLog("Workload Reassigned", "info", actor, adminId,
                        "Reassigned workload for TA " + taId, ip);
            }

        } else {
            JsonUtil.writeError(resp, 404, "接口不存在");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = JsonUtil.getPathInfo(req);
        String adminId = getUserId(req);
        String actor = getUserName(req);
        String ip = req.getRemoteAddr();

        if (path.matches("users/[\\w]+")) {
            String userId = path.split("/")[1];
            Map<String, Object> result = service.deleteUser(adminId, userId);
            if (result.containsKey("error")) {
                int code = (int) result.get("code");
                JsonUtil.writeError(resp, code, (String) result.get("error"));
            } else {
                JsonObject ok = new JsonObject();
                ok.addProperty("message", "用户删除成功");
                JsonUtil.writeJson(resp, ok);
                dataService.addLog("User Deleted", "warning", actor, adminId,
                        "Deleted user " + userId, ip);
            }

        } else if (path.matches("modules/[\\w]+")) {
            String moduleId = path.split("/")[1];
            Map<String, Object> result = service.deleteModule(moduleId);
            if (result.containsKey("error")) {
                int code = (int) result.get("code");
                JsonUtil.writeError(resp, code, (String) result.get("error"));
            } else {
                JsonObject ok = new JsonObject();
                ok.addProperty("message", "模块删除成功");
                JsonUtil.writeJson(resp, ok);
                dataService.addLog("Module Deleted", "warning", actor, adminId,
                        "Deleted module " + moduleId, ip);
            }

        } else {
            JsonUtil.writeError(resp, 404, "接口不存在");
        }
    }
}
