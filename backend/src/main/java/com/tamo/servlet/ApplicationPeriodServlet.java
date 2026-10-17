package com.tamo.servlet;

import com.google.gson.JsonParseException;
import com.tamo.model.ApplicationPeriod;
import com.tamo.service.*;
import com.tamo.util.JsonUtil;
import jakarta.servlet.http.*;
import java.io.IOException;

/** Admin writes, with a separate read-only period list for authorised organisers. */
public class ApplicationPeriodServlet extends HttpServlet {
    private JsonDataService data;
    private ApplicationPeriodService service;
    @Override public void init() {
        data = JsonDataService.getInstance();
        service = new ApplicationPeriodService(data);
    }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = JsonUtil.getPathInfo(req);
            if (path.isEmpty()) JsonUtil.writeJson(resp, service.list());
            else if (path.matches("[\\w]+/applications") && "admin".equals(req.getSession().getAttribute("role"))) {
                JsonUtil.writeJson(resp, service.applications(path.split("/")[0]));
            } else JsonUtil.writeError(resp, 404, "Endpoint not found");
        } catch (ApplicationPeriodService.Problem e) { JsonUtil.writeError(resp, e.status, e.getMessage()); }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        write(req, resp, false);
    }
    @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        write(req, resp, true);
    }

    private void write(HttpServletRequest req, HttpServletResponse resp, boolean edit) throws IOException {
        if (!"admin".equals(req.getSession().getAttribute("role"))) {
            JsonUtil.writeError(resp, 403, "Only administrators can manage application periods."); return;
        }
        String path = JsonUtil.getPathInfo(req);
        try {
            ApplicationPeriod period;
            String action;
            if (edit && path.matches("[\\w]+")) {
                period = service.update(path, JsonUtil.readRequestBodyAsJson(req)); action = "Updated";
            } else if (!edit && path.isEmpty()) {
                period = service.create(JsonUtil.readRequestBodyAsJson(req)); action = "Created";
                resp.setStatus(201);
            } else if (!edit && path.matches("[\\w]+/(open|close)")) {
                String[] parts = path.split("/");
                period = service.changeStatus(parts[0], parts[1]); action = parts[1];
            } else { JsonUtil.writeError(resp, 404, "Endpoint not found"); return; }
            data.addLog("Application Period " + action, "info",
                    (String) req.getSession().getAttribute("userName"),
                    (String) req.getSession().getAttribute("userId"),
                    action + " application period " + period.getId(), req.getRemoteAddr());
            JsonUtil.writeJson(resp, service.summary(period));
        } catch (ApplicationPeriodService.Problem e) { JsonUtil.writeError(resp, e.status, e.getMessage()); }
        catch (JsonParseException e) {
            JsonUtil.writeError(resp, 400, "Invalid JSON request.");
        }
    }
}
