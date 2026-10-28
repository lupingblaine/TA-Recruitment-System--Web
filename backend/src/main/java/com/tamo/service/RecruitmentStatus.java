package com.tamo.service;

import com.google.gson.*;

/** Presentation compatibility for the retired interview workflow; stored history is unchanged. */
public final class RecruitmentStatus {
    private RecruitmentStatus() {}
    public static String current(String status) { return "interview".equals(status) ? "review" : status; }
    public static boolean visibleNotification(String type) { return type == null || !type.startsWith("interview_"); }
    public static JsonObject project(JsonObject row) {
        if (row.has("status") && !row.get("status").isJsonNull()) row.addProperty("status", current(row.get("status").getAsString()));
        if (row.has("timeline") && row.get("timeline").isJsonArray()) {
            JsonArray timeline = new JsonArray();
            for (JsonElement entry : row.getAsJsonArray("timeline")) {
                JsonObject item = entry.getAsJsonObject();
                if (!item.has("status") || !"interview".equals(item.get("status").getAsString())) timeline.add(item);
            }
            row.add("timeline", timeline);
        }
        return row;
    }
}
