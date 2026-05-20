package com.tamo.service;

import com.tamo.model.DataStore;
import com.tamo.util.JsonUtil;

import com.tamo.model.LogEntry;
import jakarta.servlet.ServletContext;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 线程安全的 JSON 文件读写核心服务（单例模式）
 */
public class JsonDataService {

    private static JsonDataService instance;
    private final String dataFilePath;
    private String sourceFilePath;
    private DataStore cache;
    private final Object lock = new Object();

    private JsonDataService(String dataFilePath) {
        this.dataFilePath = dataFilePath;
        this.cache = loadFromFile();
        resolveSourcePath();
    }

    private void resolveSourcePath() {
        try {
            URL resource = getClass().getClassLoader().getResource("data.json");
            if (resource != null && "file".equals(resource.getProtocol())) {
                Path classesPath = Paths.get(resource.toURI());
                Path projectRoot = classesPath.getParent();
                while (projectRoot != null && !Files.exists(projectRoot.resolve("pom.xml"))) {
                    projectRoot = projectRoot.getParent();
                }
                if (projectRoot != null) {
                    Path srcPath = projectRoot.resolve("src/main/resources/data.json");
                    if (Files.exists(srcPath)) {
                        this.sourceFilePath = srcPath.toString();
                    }
                }
            }
        } catch (Exception e) {
            this.sourceFilePath = null;
        }
    }

    /**
     * 解析 data.json 所在目录。嵌入式 Tomcat 下 getRealPath 常为 null，需回退到 target/classes。
     */
    public static String resolveClassesPath(ServletContext ctx) {
        if (ctx != null) {
            String realPath = ctx.getRealPath("/WEB-INF/classes");
            if (realPath != null) {
                return realPath;
            }
        }
        File targetClasses = new File("target/classes");
        if (targetClasses.isDirectory()) {
            return targetClasses.getAbsolutePath();
        }
        try {
            URL resource = JsonDataService.class.getClassLoader().getResource("data.json");
            if (resource != null && "file".equals(resource.getProtocol())) {
                return Paths.get(resource.toURI()).getParent().toString();
            }
        } catch (Exception ignored) {
        }
        throw new IllegalStateException("Cannot resolve WEB-INF/classes directory for data.json");
    }

    public static synchronized JsonDataService getInstance(String basePath) {
        if (instance == null) {
            if (basePath == null || basePath.isBlank()) {
                throw new IllegalArgumentException("basePath must not be null or blank");
            }
            String path = basePath + File.separator + "data.json";
            instance = new JsonDataService(path);
            System.out.println("[JsonDataService] data file: " + path);
        }
        return instance;
    }

    /** 确保单例已初始化（Main 或 Listener 启动时调用） */
    public static synchronized JsonDataService ensureInitialized(String basePath) {
        return getInstance(basePath);
    }

    public static synchronized JsonDataService getInstance() {
        if (instance == null) {
            throw new IllegalStateException("JsonDataService is not initialized. Call getInstance(basePath) first.");
        }
        return instance;
    }

    /**
     * 从文件加载数据
     */
    private DataStore loadFromFile() {
        try {
            Path path = Paths.get(dataFilePath);
            if (!Files.exists(path)) {
                // 尝试从 classpath 加载
                try (InputStream is = getClass().getClassLoader().getResourceAsStream("data.json")) {
                    if (is != null) {
                        String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                        DataStore ds = JsonUtil.getGson().fromJson(json, DataStore.class);
                        // 写入文件以供后续修改
                        Files.createDirectories(path.getParent());
                        Files.writeString(path, json, StandardCharsets.UTF_8);
                        return ds;
                    }
                }
                System.err.println("数据文件不存在: " + dataFilePath);
                return new DataStore();
            }
            String json = Files.readString(path, StandardCharsets.UTF_8);
            return JsonUtil.getGson().fromJson(json, DataStore.class);
        } catch (Exception e) {
            System.err.println("加载数据文件失败: " + e.getMessage());
            e.printStackTrace();
            return new DataStore();
        }
    }

    /**
     * 保存数据到文件 — 同时写入 target/classes 和 src/main/resources
     */
    private void saveToFile() {
        try {
            String json = JsonUtil.getGson().toJson(cache);
            Files.writeString(Paths.get(dataFilePath), json, StandardCharsets.UTF_8);
            if (sourceFilePath != null) {
                Files.writeString(Paths.get(sourceFilePath), json, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            System.err.println("保存数据文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取数据存储（只读操作直接获取即可）
     */
    public DataStore getData() {
        synchronized (lock) {
            return cache;
        }
    }

    /**
     * 保存修改后的数据
     */
    public void save() {
        synchronized (lock) {
            saveToFile();
        }
    }

    /**
     * 重新加载数据（如需刷新）
     */
    public void reload() {
        synchronized (lock) {
            this.cache = loadFromFile();
        }
    }

    /**
     * 生成自增 ID
     */
    public String generateId(String prefix, java.util.List<?> list) {
        synchronized (lock) {
            return prefix + String.format("%03d", list.size() + 1);
        }
    }

    /**
     * Append an audit log entry and persist.
     */
    public void addLog(String action, String type, String actor, String actorId, String details, String ip) {
        synchronized (lock) {
            LogEntry entry = new LogEntry();
            int nextNum = cache.getLogs() != null ? cache.getLogs().size() + 1 : 1;
            entry.setId("EVT-" + (9000 + nextNum));
            entry.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            entry.setAction(action);
            entry.setType(type);
            entry.setActor(actor != null ? actor : "System");
            entry.setActorId(actorId);
            entry.setDetails(details);
            entry.setIp(ip != null ? ip : "127.0.0.1");
            if (cache.getLogs() == null) {
                cache.setLogs(new java.util.ArrayList<>());
            }
            cache.getLogs().add(entry);
            saveToFile();
        }
    }
}
