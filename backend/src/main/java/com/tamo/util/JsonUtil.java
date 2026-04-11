package com.tamo.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class JsonUtil {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    public static Gson getGson() {
        return GSON;
    }

    /**
     * 从请求体读取 JSON 并转换为指定类型
     */
    public static <T> T readRequestBody(HttpServletRequest req, Class<T> clazz) throws IOException {
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
    public static JsonObject readRequestBodyAsJson(HttpServletRequest req) throws IOException {
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
    public static void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        JsonObject err = new JsonObject();
        err.addProperty("error", message);
        writeJson(resp, err);
    }

    /**
     * 向响应写入成功信息
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
    public static String getPathInfo(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) return "";
        if (pathInfo.startsWith("/")) pathInfo = pathInfo.substring(1);
        return pathInfo;
    }
}
