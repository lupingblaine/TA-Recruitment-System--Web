package com.tamo.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tamo.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MessageService {

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
        DataStore data = dataService.getData();
        User currentUser = data.getUsers().stream().filter(u -> u.getId().equals(userId)).findFirst().orElse(null);
        if (currentUser == null) return Collections.emptyList();

        String role = currentUser.getRole();
        List<User> contactUsers = new ArrayList<>();

        if ("admin".equals(role)) {
            // Admin can contact everyone except themselves
            contactUsers = data.getUsers().stream()
                    .filter(u -> !u.getId().equals(userId) && "active".equals(u.getStatus()))
                    .collect(Collectors.toList());

        } else if ("applicant".equals(role)) {
            // Can contact: MOs of jobs applied to + all admins
            List<String> appliedJobIds = data.getApplications().stream()
                    .filter(a -> userId.equals(a.getApplicantId()))
                    .map(Application::getJobId)
                    .collect(Collectors.toList());
            Set<String> organiserIds = data.getJobs().stream()
                    .filter(j -> appliedJobIds.contains(j.getId()) && j.getOrganiserId() != null)
                    .map(Job::getOrganiserId)
                    .collect(Collectors.toSet());
            contactUsers = data.getUsers().stream()
                    .filter(u -> !u.getId().equals(userId) && "active".equals(u.getStatus()))
                    .filter(u -> organiserIds.contains(u.getId()) || "admin".equals(u.getRole()))
                    .collect(Collectors.toList());

        } else if ("organiser".equals(role)) {
            // Can contact: applicants who applied to their jobs + all admins
            List<String> myJobIds = data.getJobs().stream()
                    .filter(j -> userId.equals(j.getOrganiserId()))
                    .map(Job::getId)
                    .collect(Collectors.toList());
            Set<String> applicantIds = data.getApplications().stream()
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
            conv.addProperty("lastMessage", lastMsg.getContent());
            conv.addProperty("lastTimestamp", lastMsg.getTimestamp());
            conv.addProperty("unreadCount", unreadCount);
            conversations.add(conv);
        }

        conversations.sort((a, b) -> b.get("lastTimestamp").getAsString().compareTo(a.get("lastTimestamp").getAsString()));
        return conversations;
    }

    public List<Message> getConversationMessages(String userId, String conversationId) {
        DataStore data = dataService.getData();
        if (data.getMessages() == null) return Collections.emptyList();

        return data.getMessages().stream()
                .filter(m -> conversationId.equals(m.getConversationId()))
                .filter(m -> userId.equals(m.getSenderId()) || userId.equals(m.getReceiverId()))
                .sorted(Comparator.comparing(Message::getTimestamp))
                .collect(Collectors.toList());
    }

    public Map<String, Object> sendMessage(String senderId, String receiverId, String content) {
        Map<String, Object> result = new HashMap<>();
        DataStore data = dataService.getData();

        if (receiverId == null || receiverId.isBlank()) {
            result.put("error", "Receiver ID is required");
            result.put("code", 400);
            return result;
        }
        if (content == null || content.isBlank()) {
            result.put("error", "Message content is required");
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

        Message msg = new Message();
        msg.setId(dataService.generateId("msg", data.getMessages()));
        msg.setConversationId(buildConversationId(senderId, receiverId));
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        msg.setRead(false);

        data.getMessages().add(msg);
        dataService.save();

        result.put("message", msg);
        return result;
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
