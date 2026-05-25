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
 * Thread-safe JSON file read/write core service (Singleton pattern)
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
     * Resolve the directory containing data.json. getRealPath may be null in embedded Tomcat, fall back to target/classes.
     */
    /**
     * Executes the resolveClassesPath operation.
     * @param ctx the ctx argument
     * @return the resulting object or status
     */
    /**
     * Executes the resolveClassesPath operation.
     * @param ctx the ctx argument
     * @return the resulting object or status
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

    /**
     * Executes the getInstance operation.
     * @param basePath the basePath argument
     * @return the resulting object or status
     */
    /**
     * Executes the getInstance operation.
     * @param basePath the basePath argument
     * @return the resulting object or status
     */
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

    /** Ensure the singleton is initialized (called by Main or Listener on startup). */
    public static synchronized JsonDataService ensureInitialized(String basePath) {
        return getInstance(basePath);
    }

    /**
     * Executes the getInstance operation.
     * @return the resulting object or status
     */
    /**
     * Executes the getInstance operation.
     * @return the resulting object or status
     */
    public static synchronized JsonDataService getInstance() {
        if (instance == null) {
            throw new IllegalStateException("JsonDataService is not initialized. Call getInstance(basePath) first.");
        }
        return instance;
    }

    /**
     * Load data from file

     */
    private DataStore loadFromFile() {
        try {
            Path path = Paths.get(dataFilePath);
            if (!Files.exists(path)) {
                // Try to load from classpath
                try (InputStream is = getClass().getClassLoader().getResourceAsStream("data.json")) {
                    if (is != null) {
                        String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                        DataStore ds = JsonUtil.getGson().fromJson(json, DataStore.class);
                        // Write to file for subsequent modifications
                        Files.createDirectories(path.getParent());
                        Files.writeString(path, json, StandardCharsets.UTF_8);
                        return ds;
                    }
                }
                System.err.println("Data file does not exist: " + dataFilePath);
                return new DataStore();
            }
            String json = Files.readString(path, StandardCharsets.UTF_8);
            return JsonUtil.getGson().fromJson(json, DataStore.class);
        } catch (Exception e) {
            System.err.println("Failed to load data file: " + e.getMessage());
            e.printStackTrace();
            return new DataStore();
        }
    }

    /**
     * Save data to file — write to both target/classes and src/main/resources
     */
    private void saveToFile() {
        try {
            String json = JsonUtil.getGson().toJson(cache);
            Files.writeString(Paths.get(dataFilePath), json, StandardCharsets.UTF_8);
            if (sourceFilePath != null) {
                Files.writeString(Paths.get(sourceFilePath), json, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            System.err.println("Failed to save data file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get data store (read-only operations can directly get)
     */
    /**
     * Executes the getData operation.
     * @return the resulting object or status
     */
    /**
     * Executes the getData operation.
     * @return the resulting object or status
     */
    public DataStore getData() {
        synchronized (lock) {
            return cache;
        }
    }

    /**
     * Save modified data
     */
    /**
     * Executes the save operation.
     */
    /**
     * Executes the save operation.
     */
    public void save() {
        synchronized (lock) {
            saveToFile();
        }
    }

    /**
     * Reload data (if refresh is needed)
     */
    /**
     * Executes the reload operation.
     */
    /**
     * Executes the reload operation.
     */
    public void reload() {
        synchronized (lock) {
            this.cache = loadFromFile();
        }
    }

    /**
     * Generate auto-increment ID
     */
    /**
     * Executes the generateId operation.
     * @param prefix the prefix argument
     * @param list the list argument
     * @return the resulting object or status
     */
    /**
     * Executes the generateId operation.
     * @param prefix the prefix argument
     * @param list the list argument
     * @return the resulting object or status
     */
    public String generateId(String prefix, java.util.List<?> list) {
        synchronized (lock) {
            int max = 0;
            if (list != null) {
                for (Object item : list) {
                    String id = extractEntityId(item);
                    if (id != null && id.startsWith(prefix)) {
                        String suffix = id.substring(prefix.length());
                        try {
                            max = Math.max(max, Integer.parseInt(suffix));
                        } catch (NumberFormatException ignored) {
                            // non-numeric suffix — skip
                        }
                    }
                }
            }
            return prefix + String.format("%03d", max + 1);
        }
    }

    private static String extractEntityId(Object item) {
        if (item == null) return null;
        try {
            Object id = item.getClass().getMethod("getId").invoke(item);
            return id != null ? id.toString() : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    /**
     * Append an audit log entry and persist.
     */
    /**
     * Executes the addLog operation.
     * @param action the action argument
     * @param type the type argument
     * @param actor the actor argument
     * @param actorId the actorId argument
     * @param details the details argument
     * @param ip the ip argument
     */
    /**
     * Executes the addLog operation.
     * @param action the action argument
     * @param type the type argument
     * @param actor the actor argument
     * @param actorId the actorId argument
     * @param details the details argument
     * @param ip the ip argument
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
