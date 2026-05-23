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
     * 从请求体读取 JSON 并转换为指定类型
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
     * 从请求体读取原始 JSON 对象
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
     * 向响应写入 JSON
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
    public static void writeJson(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(GSON.toJson(data));
            out.flush();
        }
    }

    /**
     * 向响应写入错误信息
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
     * 向响应写入成功信息
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
     * 获取路径信息，去掉前导斜杠
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
