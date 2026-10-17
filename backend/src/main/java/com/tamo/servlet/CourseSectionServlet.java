package com.tamo.servlet;

import com.google.gson.JsonParseException;
import com.tamo.model.CourseSection;
import com.tamo.service.*;
import com.tamo.util.JsonUtil;
import jakarta.servlet.http.*;
import java.io.IOException;

/** Course ownership is checked for every list and mutation, independently of the UI. */
public class CourseSectionServlet extends HttpServlet {
    private JsonDataService data;
    private CourseSectionService service;
    @Override public void init() { data = JsonDataService.getInstance(); service = new CourseSectionService(data); }
    private String user(HttpServletRequest req) { return (String) req.getSession().getAttribute("userId"); }
    private String role(HttpServletRequest req) { return (String) req.getSession().getAttribute("role"); }
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = JsonUtil.getPathInfo(req);
        try {
            if ("modules".equals(path)) JsonUtil.writeJson(resp, service.modules(user(req), role(req)));
            else if (path.matches("modules/[\\w]+/sections")) JsonUtil.writeJson(resp, service.list(user(req), role(req), path.split("/")[1],req.getParameter("periodId")));
            else JsonUtil.writeError(resp, 404, "Endpoint not found");
        } catch (CourseSectionService.Problem e) { JsonUtil.writeError(resp, e.status, e.getMessage()); }
    }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException { write(req, resp, "POST"); }
    @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException { write(req, resp, "PUT"); }
    @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException { write(req, resp, "DELETE"); }
    private void write(HttpServletRequest req, HttpServletResponse resp, String method) throws IOException {
        String path = JsonUtil.getPathInfo(req);
        try {
            CourseSection section;
            String action;
            if ("POST".equals(method) && path.matches("modules/[\\w]+/sections")) {
                section = service.create(user(req), role(req), path.split("/")[1], JsonUtil.readRequestBodyAsJson(req));
                action = "Created"; resp.setStatus(201);
            } else if ("PUT".equals(method) && path.matches("sections/[\\w]+")) {
                section = service.update(user(req), role(req), path.split("/")[1], JsonUtil.readRequestBodyAsJson(req)); action = "Updated";
            } else if ("DELETE".equals(method) && path.matches("sections/[\\w]+")) {
                section = service.archive(user(req), role(req), path.split("/")[1], true); action = "Archived";
            } else if ("POST".equals(method) && path.matches("sections/[\\w]+/restore")) {
                section = service.archive(user(req), role(req), path.split("/")[1], false); action = "Restored";
            } else { JsonUtil.writeError(resp, 404, "Endpoint not found"); return; }
            data.addLog("Course Section " + action, "info", (String) req.getSession().getAttribute("userName"), user(req),
                    action + " section " + section.getId() + " in course " + section.getModuleId(), req.getRemoteAddr());
            JsonUtil.writeJson(resp, section);
        } catch (CourseSectionService.Problem e) { JsonUtil.writeError(resp, e.status, e.getMessage()); }
        catch (JsonParseException e) { JsonUtil.writeError(resp, 400, "Invalid JSON request."); }
    }
}
