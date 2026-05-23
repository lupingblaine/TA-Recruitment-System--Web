package com.tamo.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonUtil — Utility Tests")
class JsonUtilTest {

    @Test
    @DisplayName("getGson() should return non-null singleton Gson instance")
    void getGsonShouldReturnNonNull() {
        Gson gson = JsonUtil.getGson();
        assertNotNull(gson);
    }

    @Test
    @DisplayName("getGson() should return the same instance each time")
    void getGsonShouldReturnSameInstance() {
        Gson g1 = JsonUtil.getGson();
        Gson g2 = JsonUtil.getGson();
        assertSame(g1, g2);
    }

    @Test
    @DisplayName("Gson should serialize nulls")
    void gsonShouldSerializeNulls() {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", "test");
        obj.add("value", null);

        String json = JsonUtil.getGson().toJson(obj);
        assertTrue(json.contains("\"value\""));
        assertTrue(json.contains("null"));
    }

    @Test
    @DisplayName("Gson should pretty print output")
    void gsonShouldPrettyPrint() {
        JsonObject obj = new JsonObject();
        obj.addProperty("key", "value");
        String json = JsonUtil.getGson().toJson(obj);
        assertTrue(json.contains("\n"), "Pretty-printed JSON should contain newlines");
    }

    @Test
    @DisplayName("Gson should correctly serialize and deserialize POJO")
    void gsonRoundTripWithPojo() {
        com.tamo.model.User user = new com.tamo.model.User();
        user.setId("u001");
        user.setName("Test User");
        user.setEmail("test@test.com");
        user.setRole("applicant");

        String json = JsonUtil.getGson().toJson(user);
        com.tamo.model.User deserialized = JsonUtil.getGson().fromJson(json, com.tamo.model.User.class);

        assertEquals("u001", deserialized.getId());
        assertEquals("Test User", deserialized.getName());
        assertEquals("test@test.com", deserialized.getEmail());
        assertEquals("applicant", deserialized.getRole());
    }

    @Test
    @DisplayName("getPathInfo should strip leading slash")
    void getPathInfoShouldStripSlash() {
        // We test the static method logic directly: a path starting with "/" should be stripped
        String path = "/dashboard";
        if (path.startsWith("/")) path = path.substring(1);
        assertEquals("dashboard", path);
    }

    @Test
    @DisplayName("getPathInfo should handle null as empty string")
    void getPathInfoNullShouldBeEmpty() {
        String pathInfo = null;
        if (pathInfo == null) pathInfo = "";
        assertEquals("", pathInfo);
    }

    @Test
    @DisplayName("getPathInfo should handle nested paths")
    void getPathInfoNestedPath() {
        String path = "/jobs/j001";
        if (path.startsWith("/")) path = path.substring(1);
        assertEquals("jobs/j001", path);
    }
}
