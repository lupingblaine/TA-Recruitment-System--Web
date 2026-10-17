package com.tamo.service;

import com.google.gson.*;
import com.tamo.model.*;
import com.tamo.util.JsonUtil;
import java.time.*;
import java.util.*;

/** Global intake windows. Position review remains available regardless of intake status. */
public class ApplicationPeriodService {
    private final JsonDataService data;
    private final Clock clock;

    public ApplicationPeriodService(JsonDataService data) { this(data, Clock.systemUTC()); }
    public ApplicationPeriodService(JsonDataService data, Clock clock) { this.data = data; this.clock = clock; }

    public static class Problem extends RuntimeException {
        public final int status;
        public Problem(int status, String message) { super(message); this.status = status; }
    }

    public List<ApplicationPeriod> periods() { return data.getData().getApplicationPeriods(); }

    public String effectiveStatus(ApplicationPeriod period) {
        if (!"open".equals(period.getStatus())) return period.getStatus();
        Instant now = clock.instant();
        if (now.isBefore(Instant.parse(period.getOpensAt()))) return "scheduled";
        if (!now.isBefore(Instant.parse(period.getClosesAt()))) return "expired";
        return "open";
    }

    public ApplicationPeriod acceptingPeriod() {
        return periods().stream().filter(p -> "open".equals(effectiveStatus(p))).findFirst().orElse(null);
    }

    public JsonObject current() {
        ApplicationPeriod period = periods().stream().filter(p -> "open".equals(p.getStatus())).findFirst()
                .orElseGet(() -> periods().stream().filter(p -> !p.getLegacy() && "closed".equals(p.getStatus()))
                        .max(Comparator.comparing(ApplicationPeriod::getClosesAt)).orElse(null));
        JsonObject result = new JsonObject();
        result.addProperty("accepting", period != null && "open".equals(effectiveStatus(period)));
        result.addProperty("effectiveStatus", period == null ? "none" : effectiveStatus(period));
        result.add("period", period == null ? JsonNull.INSTANCE : summary(period));
        return result;
    }

    public List<JsonObject> list() { return periods().stream().map(this::summary).toList(); }

    public JsonObject summary(ApplicationPeriod period) {
        JsonObject result = JsonUtil.getGson().toJsonTree(period).getAsJsonObject();
        result.addProperty("effectiveStatus", effectiveStatus(period));
        result.addProperty("accepting", "open".equals(effectiveStatus(period)));
        result.addProperty("applicationCount", count(period.getId()));
        data.getData().getTerms().stream().filter(t -> Objects.equals(t.getId(), period.getTermId())).findFirst()
                .ifPresent(t -> result.addProperty("termName", t.getName()));
        return result;
    }

    public ApplicationPeriod create(JsonObject body) {
        ApplicationPeriod period = new ApplicationPeriod();
        period.setId("period_" + UUID.randomUUID().toString().replace("-", ""));
        period.setStatus("draft");
        period.setCreatedAt(clock.instant().toString());
        configure(period, body);
        periods().add(period);
        data.save();
        return period;
    }

    public ApplicationPeriod update(String id, JsonObject body) {
        ApplicationPeriod original = find(id);
        if (original.getLegacy()) throw new Problem(409, "Historical application periods are read-only.");
        ApplicationPeriod changed = JsonUtil.getGson().fromJson(JsonUtil.getGson().toJson(original), ApplicationPeriod.class);
        configure(changed, body);
        if (count(id) > 0 && !Objects.equals(original.getTermId(), changed.getTermId())) {
            throw new Problem(409, "The academic term cannot change after applications have been submitted.");
        }
        periods().set(periods().indexOf(original), changed);
        data.save();
        return changed;
    }

    private void configure(ApplicationPeriod period, JsonObject body) {
        if (body == null) throw new Problem(400, "Application period details are required.");
        String name = text(body, "name");
        String termId = text(body, "termId");
        String opensAt = timestamp(body, "opensAt");
        String closesAt = timestamp(body, "closesAt");
        if (name.length() > 120) throw new Problem(400, "Name must be 120 characters or fewer.");
        if (data.getData().getTerms().stream().noneMatch(t -> t.getId().equals(termId))) {
            throw new Problem(400, "Choose an existing academic term.");
        }
        if (!Instant.parse(opensAt).isBefore(Instant.parse(closesAt))) {
            throw new Problem(400, "Opening time must be before the deadline.");
        }
        period.setName(name); period.setTermId(termId);
        period.setOpensAt(opensAt); period.setClosesAt(closesAt);
        period.setUpdatedAt(clock.instant().toString());
    }

    private static String text(JsonObject body, String key) {
        JsonElement value = body.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()
                || value.getAsString().isBlank()) throw new Problem(400, key + " is required.");
        return value.getAsString().trim();
    }

    private static String timestamp(JsonObject body, String key) {
        String value = text(body, key);
        try { return OffsetDateTime.parse(value).toInstant().toString(); }
        catch (DateTimeException e) { throw new Problem(400, key + " must include a timezone offset."); }
    }

    public ApplicationPeriod changeStatus(String id, String action) {
        ApplicationPeriod period = find(id);
        if (period.getLegacy()) throw new Problem(409, "Historical application periods are read-only.");
        if ("open".equals(action)) {
            if (!clock.instant().isBefore(Instant.parse(period.getClosesAt()))) {
                throw new Problem(409, "Extend the deadline before reopening an expired period.");
            }
            if (periods().stream().anyMatch(p -> !p.getId().equals(id) && "open".equals(p.getStatus()))) {
                throw new Problem(409, "Close the currently open or scheduled period first.");
            }
            period.setStatus("open");
        } else if ("close".equals(action)) {
            period.setStatus("closed");
        } else throw new Problem(400, "Unknown period action.");
        period.setUpdatedAt(clock.instant().toString());
        data.save();
        return period;
    }

    public ApplicationPeriod find(String id) {
        return periods().stream().filter(p -> p.getId().equals(id)).findFirst()
                .orElseThrow(() -> new Problem(404, "Application period not found."));
    }

    private long count(String id) {
        return data.getData().getApplications().stream().filter(a -> id.equals(a.getPeriodId())).count();
    }

    public List<JsonObject> applications(String id) {
        find(id);
        DataStore ds = data.getData();
        return ds.getApplications().stream().filter(a -> id.equals(a.getPeriodId())).map(a -> {
            JsonObject row = new JsonObject();
            row.addProperty("id", a.getId()); row.addProperty("status", a.getStatus());
            row.add("availabilitySnapshot", JsonUtil.getGson().toJsonTree(a.getAvailabilitySnapshot()));
            row.addProperty("appliedAt", a.getAppliedAt());
            ds.getUsers().stream().filter(u -> u.getId().equals(a.getApplicantId())).findFirst().ifPresent(u -> {
                row.addProperty("applicantName", u.getName()); row.addProperty("email", u.getEmail());
            });
            ds.getJobs().stream().filter(j -> j.getId().equals(a.getJobId())).findFirst().ifPresent(j -> {
                row.addProperty("jobTitle", j.getTitle()); row.addProperty("moduleCode", j.getModuleCode());
            });
            return row;
        }).toList();
    }
}
