package com.tamo.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tamo.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MessageService {

    private static final int EDIT_WINDOW_MINUTES = 15;
    private static final int RECALL_WINDOW_MINUTES = 2;

    private final JsonDataService dataService;

    public MessageService(JsonDataService dataService) {
        this.dataService = dataService;
    }

    private static String buildConversationId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0
                ? userId1 + "_" + userId2
                : userId2 + "_" + userId1;
    }

    public boolean canMessage(String senderId, String receiverId) {
        DataStore data = dataService.getData();
        User sender = data.getUsers().stream().filter(u -> u.getId().equals(senderId)).findFirst().orElse(null);
        User receiver = data.getUsers().stream().filter(u -> u.getId().equals(receiverId)).findFirst().orElse(null);
        if (sender == null || receiver == null) return false;

        String senderRole = sender.getRole();
        String receiverRole = receiver.getRole();

        // Admin can message anyone; anyone can message admin
        if ("admin".equals(senderRole) || "admin".equals(receiverRole)) return true;

        if ("applicant".equals(senderRole) && "organiser".equals(receiverRole)) {
            // Applicant can message MOs whose jobs they applied to
            List<String> appliedJobIds = data.getApplications().stream()
                    .filter(a -> senderId.equals(a.getApplicantId()))
                    .map(Application::getJobId)
                    .collect(Collectors.toList());
            return data.getJobs().stream()
                    .anyMatch(j -> appliedJobIds.contains(j.getId()) && receiverId.equals(j.getOrganiserId()));
        }

        if ("organiser".equals(senderRole) && "applicant".equals(receiverRole)) {
            // Organiser can message applicants who applied to their jobs
            List<String> organiserJobIds = data.getJobs().stream()
                    .filter(j -> senderId.equals(j.getOrganiserId()))
                    .map(Job::getId)
                    .collect(Collectors.toList());
            return data.getApplications().stream()
                    .anyMatch(a -> receiverId.equals(a.getApplicantId()) && organiserJobIds.contains(a.getJobId()));
        }

        return false;
    }

    public List<JsonObject> getContacts(String userId) {
        if (userId == null || userId.isBlank()) return Collections.emptyList();

        DataStore data = dataService.getData();
        if (data.getUsers() == null || data.getUsers().isEmpty()) return Collections.emptyList();

        User currentUser = data.getUsers().stream().filter(u -> u.getId().equals(userId)).findFirst().orElse(null);
        if (currentUser == null) return Collections.emptyList();

        String role = currentUser.getRole();
        List<User> contactUsers = new ArrayList<>();
        List<Application> applications = data.getApplications() != null ? data.getApplications() : List.of();
        List<Job> jobs = data.getJobs() != null ? data.getJobs() : List.of();

        if ("admin".equals(role)) {
            // Admin can contact everyone except themselves
            contactUsers = data.getUsers().stream()
                    .filter(u -> !u.getId().equals(userId) && "active".equals(u.getStatus()))
                    .collect(Collectors.toList());

        } else if ("applicant".equals(role)) {
            // Can contact: MOs of jobs applied to + all admins
            List<String> appliedJobIds = applications.stream()
                    .filter(a -> userId.equals(a.getApplicantId()))
                    .map(Application::getJobId)
                    .collect(Collectors.toList());
            Set<String> organiserIds = jobs.stream()
                    .filter(j -> appliedJobIds.contains(j.getId()) && j.getOrganiserId() != null)
                    .map(Job::getOrganiserId)
                    .collect(Collectors.toSet());
            contactUsers = data.getUsers().stream()
                    .filter(u -> !u.getId().equals(userId) && "active".equals(u.getStatus()))
                    .filter(u -> organiserIds.contains(u.getId()) || "admin".equals(u.getRole()))
                    .collect(Collectors.toList());

        } else if ("organiser".equals(role)) {
            // Can contact: applicants who applied to their jobs + all admins
            List<String> myJobIds = jobs.stream()
                    .filter(j -> userId.equals(j.getOrganiserId()))
                    .map(Job::getId)
                    .collect(Collectors.toList());
            Set<String> applicantIds = applications.stream()
                    .filter(a -> myJobIds.contains(a.getJobId()) && a.getApplicantId() != null)
                    .map(Application::getApplicantId)
                    .collect(Collectors.toSet());
            contactUsers = data.getUsers().stream()
                    .filter(u -> !u.getId().equals(userId) && "active".equals(u.getStatus()))
                    .filter(u -> applicantIds.contains(u.getId()) || "admin".equals(u.getRole()))
                    .collect(Collectors.toList());
        }

        return contactUsers.stream().map(u -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", u.getId());
            obj.addProperty("name", u.getName());
            obj.addProperty("role", u.getRole());
            obj.addProperty("department", u.getDepartment());
            obj.addProperty("avatar", u.getAvatar());
            return obj;
        }).collect(Collectors.toList());
    }

    public List<JsonObject> getConversations(String userId) {
        DataStore data = dataService.getData();
        if (data.getMessages() == null) return Collections.emptyList();

        Map<String, List<Message>> byConversation = data.getMessages().stream()
                .filter(m -> userId.equals(m.getSenderId()) || userId.equals(m.getReceiverId()))
                .collect(Collectors.groupingBy(Message::getConversationId));

        List<JsonObject> conversations = new ArrayList<>();
        for (Map.Entry<String, List<Message>> entry : byConversation.entrySet()) {
            List<Message> msgs = entry.getValue();
            msgs.sort(Comparator.comparing(Message::getTimestamp));
            Message lastMsg = msgs.get(msgs.size() - 1);

            String tentativeOtherId = lastMsg.getSenderId().equals(userId)
                    ? lastMsg.getReceiverId() : lastMsg.getSenderId();
            if (tentativeOtherId.equals(userId)) {
                String[] parts = entry.getKey().split("_");
                tentativeOtherId = parts[0].equals(userId) ? parts[1] : parts[0];
            }
            final String otherUserId = tentativeOtherId;

            User otherUser = data.getUsers().stream()
                    .filter(u -> u.getId().equals(otherUserId)).findFirst().orElse(null);

            long unreadCount = msgs.stream()
                    .filter(m -> m.getReceiverId().equals(userId) && !m.isRead())
                    .count();

            JsonObject conv = new JsonObject();
            conv.addProperty("conversationId", entry.getKey());
            conv.addProperty("otherUserId", otherUserId);
            conv.addProperty("otherUserName", otherUser != null ? otherUser.getName() : "Unknown");
            conv.addProperty("otherUserRole", otherUser != null ? otherUser.getRole() : "");
            conv.addProperty("otherUserAvatar", otherUser != null ? otherUser.getAvatar() : null);
            conv.addProperty("lastMessage", previewForMessage(lastMsg));
            conv.addProperty("lastTimestamp", lastMsg.getTimestamp());
            conv.addProperty("unreadCount", unreadCount);
            conversations.add(conv);
        }

        conversations.sort((a, b) -> b.get("lastTimestamp").getAsString().compareTo(a.get("lastTimestamp").getAsString()));
        return conversations;
    }

    public List<JsonObject> getConversationMessages(String userId, String conversationId) {
        DataStore data = dataService.getData();
        if (data.getMessages() == null) return Collections.emptyList();

        Map<String, Message> byId = data.getMessages().stream()
                .collect(Collectors.toMap(Message::getId, m -> m, (a, b) -> a));

        return data.getMessages().stream()
                .filter(m -> conversationId.equals(m.getConversationId()))
                .filter(m -> userId.equals(m.getSenderId()) || userId.equals(m.getReceiverId()))
                .sorted(Comparator.comparing(Message::getTimestamp))
                .map(m -> toMessageJson(m, data, byId, userId))
                .collect(Collectors.toList());
    }

    public Map<String, Object> sendMessage(String senderId, String receiverId, String content,
                                             String replyToId, String messageType,
                                             String attachmentUrl, String attachmentName, String attachmentSize) {
        Map<String, Object> result = new HashMap<>();
        DataStore data = dataService.getData();

        if (receiverId == null || receiverId.isBlank()) {
            result.put("error", "Receiver ID is required");
            result.put("code", 400);
            return result;
        }
        boolean hasText = content != null && !content.isBlank();
        boolean hasAttachment = attachmentUrl != null && !attachmentUrl.isBlank();
        if (!hasText && !hasAttachment) {
            result.put("error", "Message content or attachment is required");
            result.put("code", 400);
            return result;
        }
        if (senderId.equals(receiverId)) {
            result.put("error", "Cannot send message to yourself");
            result.put("code", 400);
            return result;
        }

        if (!canMessage(senderId, receiverId)) {
            result.put("error", "You do not have permission to message this user");
            result.put("code", 403);
            return result;
        }

        if (data.getMessages() == null) {
            data.setMessages(new ArrayList<>());
        }

        if (replyToId != null && !replyToId.isBlank()) {
            Message replyTarget = findMessage(data, replyToId);
            if (replyTarget == null) {
                result.put("error", "Referenced message does not exist");
                result.put("code", 400);
                return result;
            }
            String convId = buildConversationId(senderId, receiverId);
            if (!convId.equals(replyTarget.getConversationId())) {
                result.put("error", "Cannot reply to a message outside this conversation");
                result.put("code", 400);
                return result;
            }
        }

        Message msg = new Message();
        msg.setId(dataService.generateId("msg", data.getMessages()));
        msg.setConversationId(buildConversationId(senderId, receiverId));
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(hasText ? content.trim() : "");
        msg.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        msg.setRead(false);
        msg.setReplyToId(replyToId != null && !replyToId.isBlank() ? replyToId : null);
        msg.setRecalled(false);
        msg.setEdited(false);
        msg.setMessageType(messageType != null && !messageType.isBlank() ? messageType : (hasAttachment ? "file" : "text"));
        msg.setAttachmentUrl(hasAttachment ? attachmentUrl : null);
        msg.setAttachmentName(attachmentName);
        msg.setAttachmentSize(attachmentSize);

        data.getMessages().add(msg);
        dataService.save();

        Map<String, Message> byId = data.getMessages().stream()
                .collect(Collectors.toMap(Message::getId, m -> m, (a, b) -> a));
        result.put("message", toMessageJson(msg, data, byId, senderId));
        return result;
    }

    public Map<String, Object> editMessage(String userId, String messageId, String newContent) {
        Map<String, Object> result = new HashMap<>();
        DataStore data = dataService.getData();
        if (data.getMessages() == null) {
            result.put("error", "Record not found");
            result.put("code", 404);
            return result;
        }

        Message msg = findMessage(data, messageId);
        if (msg == null) {
            result.put("error", "Record not found");
            result.put("code", 404);
            return result;
        }
        if (!userId.equals(msg.getSenderId())) {
            result.put("error", "Only the sender can edit this message");
            result.put("code", 403);
            return result;
        }
        if (msg.isRecalled()) {
            result.put("error", "Recalled messages cannot be edited");
            result.put("code", 400);
            return result;
        }
        if (msg.getAttachmentUrl() != null && !msg.getAttachmentUrl().isBlank()) {
            result.put("error", "Attachment messages cannot be edited");
            result.put("code", 400);
            return result;
        }
        if (!withinMinutes(msg.getTimestamp(), EDIT_WINDOW_MINUTES)) {
            result.put("error", "Edit window expired (" + EDIT_WINDOW_MINUTES + " minutes)");
            result.put("code", 400);
            return result;
        }
        if (newContent == null || newContent.isBlank()) {
            result.put("error", "Message content is required");
            result.put("code", 400);
            return result;
        }

        msg.setContent(newContent.trim());
        msg.setEdited(true);
        msg.setEditedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        dataService.save();

        Map<String, Message> byId = data.getMessages().stream()
                .collect(Collectors.toMap(Message::getId, m -> m, (a, b) -> a));
        result.put("message", toMessageJson(msg, data, byId, userId));
        return result;
    }

    public Map<String, Object> recallMessage(String userId, String messageId) {
        Map<String, Object> result = new HashMap<>();
        DataStore data = dataService.getData();
        if (data.getMessages() == null) {
            result.put("error", "Record not found");
            result.put("code", 404);
            return result;
        }

        Message msg = findMessage(data, messageId);
        if (msg == null) {
            result.put("error", "Record not found");
            result.put("code", 404);
            return result;
        }
        if (!userId.equals(msg.getSenderId())) {
            result.put("error", "Only the sender can recall this message");
            result.put("code", 403);
            return result;
        }
        if (msg.isRecalled()) {
            result.put("error", "Message already recalled");
            result.put("code", 400);
            return result;
        }
        if (!withinMinutes(msg.getTimestamp(), RECALL_WINDOW_MINUTES)) {
            result.put("error", "Recall window expired (" + RECALL_WINDOW_MINUTES + " minutes)");
            result.put("code", 400);
            return result;
        }

        msg.setRecalled(true);
        msg.setContent("");
        dataService.save();

        Map<String, Message> byId = data.getMessages().stream()
                .collect(Collectors.toMap(Message::getId, m -> m, (a, b) -> a));
        result.put("message", toMessageJson(msg, data, byId, userId));
        return result;
    }

    private Message findMessage(DataStore data, String messageId) {
        if (data.getMessages() == null) return null;
        return data.getMessages().stream().filter(m -> messageId.equals(m.getId())).findFirst().orElse(null);
    }

    private boolean withinMinutes(String timestamp, int minutes) {
        if (timestamp == null || timestamp.isBlank()) return false;
        try {
            LocalDateTime sent = LocalDateTime.parse(timestamp);
            return !sent.plusMinutes(minutes).isBefore(LocalDateTime.now());
        } catch (Exception e) {
            return false;
        }
    }

    private String previewForMessage(Message m) {
        if (m.isRecalled()) return "[Message recalled]";
        if ("image".equals(m.getMessageType())) return "[Image]";
        if ("file".equals(m.getMessageType())) {
            return "[File]" + (m.getAttachmentName() != null ? " " + m.getAttachmentName() : "");
        }
        if (m.getContent() != null && !m.getContent().isBlank()) {
            return m.getContent().length() > 80 ? m.getContent().substring(0, 80) + "..." : m.getContent();
        }
        return "[Message]";
    }

    private String snippet(String text, int max) {
        if (text == null) return "";
        return text.length() > max ? text.substring(0, max) + "..." : text;
    }

    private String getUserName(DataStore data, String userId) {
        return data.getUsers().stream()
                .filter(u -> u.getId().equals(userId))
                .map(User::getName)
                .findFirst()
                .orElse("Unknown");
    }

    private JsonObject toMessageJson(Message m, DataStore data, Map<String, Message> byId, String viewerId) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", m.getId());
        obj.addProperty("conversationId", m.getConversationId());
        obj.addProperty("senderId", m.getSenderId());
        obj.addProperty("receiverId", m.getReceiverId());
        obj.addProperty("content", m.getContent() != null ? m.getContent() : "");
        obj.addProperty("timestamp", m.getTimestamp());
        obj.addProperty("read", m.isRead());
        if (m.getReplyToId() != null) obj.addProperty("replyToId", m.getReplyToId());
        obj.addProperty("recalled", m.isRecalled());
        obj.addProperty("edited", m.isEdited());
        if (m.getEditedAt() != null) obj.addProperty("editedAt", m.getEditedAt());
        if (m.getMessageType() != null) obj.addProperty("messageType", m.getMessageType());
        if (m.getAttachmentUrl() != null) obj.addProperty("attachmentUrl", m.getAttachmentUrl());
        if (m.getAttachmentName() != null) obj.addProperty("attachmentName", m.getAttachmentName());
        if (m.getAttachmentSize() != null) obj.addProperty("attachmentSize", m.getAttachmentSize());

        if (m.getReplyToId() != null && byId.containsKey(m.getReplyToId())) {
            Message reply = byId.get(m.getReplyToId());
            obj.addProperty("replyToSenderName", getUserName(data, reply.getSenderId()));
            if (reply.isRecalled()) {
                obj.addProperty("replyToContent", "[Message recalled]");
            } else if ("image".equals(reply.getMessageType())) {
                obj.addProperty("replyToContent", "[Image]");
            } else if ("file".equals(reply.getMessageType())) {
                obj.addProperty("replyToContent", "[File] " + (reply.getAttachmentName() != null ? reply.getAttachmentName() : ""));
            } else {
                obj.addProperty("replyToContent", snippet(reply.getContent(), 120));
            }
        }

        boolean isMine = viewerId != null && viewerId.equals(m.getSenderId());
        obj.addProperty("canEdit", isMine && !m.isRecalled()
                && (m.getAttachmentUrl() == null || m.getAttachmentUrl().isBlank())
                && withinMinutes(m.getTimestamp(), EDIT_WINDOW_MINUTES));
        obj.addProperty("canRecall", isMine && !m.isRecalled() && withinMinutes(m.getTimestamp(), RECALL_WINDOW_MINUTES));
        return obj;
    }

    public void markConversationRead(String userId, String conversationId) {
        DataStore data = dataService.getData();
        if (data.getMessages() == null) return;

        boolean changed = false;
        for (Message m : data.getMessages()) {
            if (conversationId.equals(m.getConversationId())
                    && userId.equals(m.getReceiverId())
                    && !m.isRead()) {
                m.setRead(true);
                changed = true;
            }
        }
        if (changed) dataService.save();
    }

    public int getUnreadCount(String userId) {
        DataStore data = dataService.getData();
        if (data.getMessages() == null) return 0;
        return (int) data.getMessages().stream()
                .filter(m -> userId.equals(m.getReceiverId()) && !m.isRead())
                .count();
    }
}
