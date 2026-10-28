package com.tamo.servlet;

import com.google.gson.JsonObject;
import com.tamo.service.JsonDataService;
import com.tamo.service.MessageService;
import com.tamo.service.QualificationService;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@MultipartConfig(
        maxFileSize = 20 * 1024 * 1024,
        maxRequestSize = 25 * 1024 * 1024,
        fileSizeThreshold = 1024 * 1024
)
/**
 * Implementation of MessageServlet.
 * Provides necessary operations and logic for the system.
 */
public class MessageServlet extends HttpServlet {

    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );
    private static final Set<String> FILE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "application/zip"
    );

    private MessageService service;

    @Override
    /**
     * Executes the init operation.
     */
    /**
     * Executes the init operation.
     */
    public void init() throws ServletException {
        JsonDataService dataService = JsonDataService.getInstance(
                JsonDataService.resolveClassesPath(getServletContext()));
        this.service = new MessageService(dataService);
    }

    private String getUserId(HttpServletRequest req) {
        return (String) req.getSession().getAttribute("userId");
    }

    @Override
    /**
     * Executes the doGet operation.
     * @param req the req argument
     * @param resp the resp argument
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String path = JsonUtil.getPathInfo(req);
        String userId = getUserId(req);

        try {
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
                    } else if (path.startsWith("user-info/")) {
                        String targetUserId = path.substring("user-info/".length());
                        com.google.gson.JsonObject info = service.getUserInfo(userId, targetUserId);
                        if (info == null) JsonUtil.writeError(resp, 404, "User not found");
                        else JsonUtil.writeJson(resp, info);
                    } else {
                        JsonUtil.writeError(resp, 404, "Endpoint not found");
                    }
                }
            }
        } catch (QualificationService.Problem problem) {
            JsonUtil.writeError(resp, problem.status, problem.getMessage());
        }
    }

    @Override
    /**
     * Executes the doPost operation.
     * @param req the req argument
     * @param resp the resp argument
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String path = JsonUtil.getPathInfo(req);
        String userId = getUserId(req);

        if ("send".equals(path)) {
            JsonObject body = JsonUtil.readRequestBodyAsJson(req);
            String receiverId = body.has("receiverId") ? body.get("receiverId").getAsString() : null;
            String content = body.has("content") ? body.get("content").getAsString() : null;
            String replyToId = body.has("replyToId") ? body.get("replyToId").getAsString() : null;
            String messageType = body.has("messageType") ? body.get("messageType").getAsString() : null;
            String attachmentUrl = body.has("attachmentUrl") ? body.get("attachmentUrl").getAsString() : null;
            String attachmentName = body.has("attachmentName") ? body.get("attachmentName").getAsString() : null;
            String attachmentSize = body.has("attachmentSize") ? body.get("attachmentSize").getAsString() : null;

            Map<String, Object> result = service.sendMessage(userId, receiverId, content, replyToId,
                    messageType, attachmentUrl, attachmentName, attachmentSize);
            if (result.containsKey("error")) {
                int code = (int) result.get("code");
                JsonUtil.writeError(resp, code, (String) result.get("error"));
            } else {
                resp.setStatus(201);
                JsonUtil.writeJson(resp, result.get("message"));
            }
        } else if ("upload".equals(path)) {
            handleUpload(req, resp);
        } else {
            JsonUtil.writeError(resp, 404, "Endpoint not found");
        }
    }

    private void handleUpload(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Part filePart = req.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                JsonUtil.writeError(resp, 400, "No file uploaded");
                return;
            }

            String contentType = filePart.getContentType();
            if (contentType == null || !FILE_TYPES.contains(contentType.toLowerCase())) {
                JsonUtil.writeError(resp, 400, "File type not allowed");
                return;
            }

            long size = filePart.getSize();
            boolean isImage = IMAGE_TYPES.contains(contentType.toLowerCase());
            if (isImage && size > 10 * 1024 * 1024) {
                JsonUtil.writeError(resp, 400, "Image must be under 10MB");
                return;
            }
            if (!isImage && size > 20 * 1024 * 1024) {
                JsonUtil.writeError(resp, 400, "File must be under 20MB");
                return;
            }

            String originalName = getFileName(filePart);
            String safeName = sanitizeFilename(originalName);
            String storedName = UUID.randomUUID().toString().replace("-", "") + "_" + safeName;

            Path uploadDir = Paths.get(getServletContext().getRealPath("/static/uploads/messages"));
            Files.createDirectories(uploadDir);
            Path target = uploadDir.resolve(storedName);

            try (InputStream in = filePart.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            String ctx = req.getContextPath();
            String url = ctx + "/static/uploads/messages/" + storedName;
            String messageType = isImage ? "image" : "file";
            String sizeLabel = formatSize(size);

            JsonObject result = new JsonObject();
            result.addProperty("url", url);
            result.addProperty("fileName", originalName);
            result.addProperty("fileType", contentType);
            result.addProperty("fileSize", sizeLabel);
            result.addProperty("messageType", messageType);
            resp.setStatus(201);
            JsonUtil.writeJson(resp, result);
        } catch (Exception e) {
            JsonUtil.writeError(resp, 500, "Upload failed: " + e.getMessage());
        }
    }

    @Override
    /**
     * Executes the doPut operation.
     * @param req the req argument
     * @param resp the resp argument
     */
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String path = JsonUtil.getPathInfo(req);
        String userId = getUserId(req);

        if (path.startsWith("read/")) {
            String conversationId = path.substring("read/".length());
            service.markConversationRead(userId, conversationId);
            JsonObject ok = new JsonObject();
            ok.addProperty("success", true);
            JsonUtil.writeJson(resp, ok);
        } else if (path.startsWith("edit/")) {
            String messageId = path.substring("edit/".length());
            JsonObject body = JsonUtil.readRequestBodyAsJson(req);
            String content = body.has("content") ? body.get("content").getAsString() : null;
            Map<String, Object> result = service.editMessage(userId, messageId, content);
            if (result.containsKey("error")) {
                JsonUtil.writeError(resp, (int) result.get("code"), (String) result.get("error"));
            } else {
                JsonUtil.writeJson(resp, result.get("message"));
            }
        } else if (path.startsWith("recall/")) {
            String messageId = path.substring("recall/".length());
            Map<String, Object> result = service.recallMessage(userId, messageId);
            if (result.containsKey("error")) {
                JsonUtil.writeError(resp, (int) result.get("code"), (String) result.get("error"));
            } else {
                JsonUtil.writeJson(resp, result.get("message"));
            }
        } else {
            JsonUtil.writeError(resp, 404, "Endpoint not found");
        }
    }

    private String getFileName(Part part) {
        String submitted = part.getSubmittedFileName();
        if (submitted != null && !submitted.isBlank()) return submitted;
        return "file";
    }

    private String sanitizeFilename(String name) {
        if (name == null || name.isBlank()) return "file";
        String base = Paths.get(name).getFileName().toString();
        return base.replaceAll("[^a-zA-Z0-9._\\u4e00-\\u9fa5-]", "_");
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
