package com.tamo.servlet;

import com.google.gson.JsonObject;
import com.tamo.service.JsonDataService;
import com.tamo.service.MessageService;
import com.tamo.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class MessageServlet extends HttpServlet {

    private MessageService service;

    @Override
    public void init() throws ServletException {
        JsonDataService dataService = JsonDataService.getInstance();
        this.service = new MessageService(dataService);
    }

    private String getUserId(HttpServletRequest req) {
        return (String) req.getSession().getAttribute("userId");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = JsonUtil.getPathInfo(req);
        String userId = getUserId(req);

        switch (path) {
            case "contacts" -> JsonUtil.writeJson(resp, service.getContacts(userId));
            case "conversations" -> JsonUtil.writeJson(resp, service.getConversations(userId));
            case "unread-count" -> {
                JsonObject obj = new JsonObject();
                obj.addProperty("count", service.getUnreadCount(userId));
                JsonUtil.writeJson(resp, obj);
            }
            default -> {
                if (path.startsWith("conversations/")) {
                    String conversationId = path.substring("conversations/".length());
                    JsonUtil.writeJson(resp, service.getConversationMessages(userId, conversationId));
                } else {
                    JsonUtil.writeError(resp, 404, "Endpoint not found");
                }
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = JsonUtil.getPathInfo(req);
        String userId = getUserId(req);

        if ("send".equals(path)) {
            JsonObject body = JsonUtil.readRequestBodyAsJson(req);
            String receiverId = body.has("receiverId") ? body.get("receiverId").getAsString() : null;
            String content = body.has("content") ? body.get("content").getAsString() : null;

            Map<String, Object> result = service.sendMessage(userId, receiverId, content);
            if (result.containsKey("error")) {
                int code = (int) result.get("code");
                JsonUtil.writeError(resp, code, (String) result.get("error"));
            } else {
                resp.setStatus(201);
                JsonUtil.writeJson(resp, result.get("message"));
            }
        } else {
            JsonUtil.writeError(resp, 404, "Endpoint not found");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = JsonUtil.getPathInfo(req);
        String userId = getUserId(req);

        if (path.startsWith("read/")) {
            String conversationId = path.substring("read/".length());
            service.markConversationRead(userId, conversationId);
            JsonObject ok = new JsonObject();
            ok.addProperty("success", true);
            JsonUtil.writeJson(resp, ok);
        } else {
            JsonUtil.writeError(resp, 404, "Endpoint not found");
        }
    }
}
