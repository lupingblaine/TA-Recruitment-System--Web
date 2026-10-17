package com.tamo.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Implementation of JsonUtil.
 * Provides necessary operations and logic for the system.
 */
public class JsonUtil {

    public static String escapeHtml(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    /**
     * Executes the getGson operation.
     * @return the resulting object or status
     */
    /**
     * Executes the getGson operation.
     * @return the resulting object or status
     */
    public static Gson getGson() {
        return GSON;
    }

    /**
     * Read JSON from request body and convert to specified type

     */
    /**
     * Executes the readRequestBody operation.
     * @param req the req argument
     * @param clazz the clazz argument
     * @return the resulting object or status
     */
    /**
     * Executes the readRequestBody operation.
     * @param req the req argument
     * @param clazz the clazz argument
     * @return the resulting object or status
     */
    public static <T> T readRequestBody(HttpServletRequest req, Class<T> clazz) throws IOException {
        req.setCharacterEncoding("UTF-8");
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return GSON.fromJson(sb.toString(), clazz);
    }

    /**
     * Read raw JSON object from request body

     */
    /**
     * Executes the readRequestBodyAsJson operation.
     * @param req the req argument
     * @return the resulting object or status
     */
    /**
     * Executes the readRequestBodyAsJson operation.
     * @param req the req argument
     * @return the resulting object or status
     */
    public static JsonObject readRequestBodyAsJson(HttpServletRequest req) throws IOException {
        req.setCharacterEncoding("UTF-8");
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return GSON.fromJson(sb.toString(), JsonObject.class);
    }

    /**
     * Write JSON to response

     */
    /**
     * Executes the writeJson operation.
     * @param resp the resp argument
     * @param data the data argument
     */
    /**
     * Executes the writeJson operation.
     * @param resp the resp argument
     * @param data the data argument
     */
    private static void redact(com.google.gson.JsonElement value){
        if(value==null||value.isJsonNull())return;
        if(value.isJsonObject()){value.getAsJsonObject().remove("password");value.getAsJsonObject().remove("storageKey");for(var child:value.getAsJsonObject().asMap().values())redact(child);}
        else if(value.isJsonArray())for(var child:value.getAsJsonArray())redact(child);
    }
    public static void writeJson(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            com.google.gson.JsonElement tree=GSON.toJsonTree(data).deepCopy();redact(tree);out.print(GSON.toJson(tree));
            out.flush();
        }
    }

    /**
     * Write error message to response

     */
    /**
     * Executes the writeError operation.
     * @param resp the resp argument
     * @param status the status argument
     * @param message the message argument
     */
    /**
     * Executes the writeError operation.
     * @param resp the resp argument
     * @param status the status argument
     * @param message the message argument
     */
    public static void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        JsonObject err = new JsonObject();
        err.addProperty("error", message);
        writeJson(resp, err);
    }

    /**
     * Write success message to response

     */
    /**
     * Executes the writeSuccess operation.
     * @param resp the resp argument
     * @param message the message argument
     */
    /**
     * Executes the writeSuccess operation.
     * @param resp the resp argument
     * @param message the message argument
     */
    public static void writeSuccess(HttpServletResponse resp, String message) throws IOException {
        JsonObject ok = new JsonObject();
        ok.addProperty("success", true);
        ok.addProperty("message", message);
        writeJson(resp, ok);
    }

    /**
     * Get path info, remove leading slash

     */
    /**
     * Executes the getPathInfo operation.
     * @param req the req argument
     * @return the resulting object or status
     */
    /**
     * Executes the getPathInfo operation.
     * @param req the req argument
     * @return the resulting object or status
     */
    public static String getPathInfo(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) return "";
        if (pathInfo.startsWith("/")) pathInfo = pathInfo.substring(1);
        return pathInfo;
    }
}
