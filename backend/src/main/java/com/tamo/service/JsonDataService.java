package com.tamo.service;

import com.google.gson.Gson;
import com.tamo.model.DataStore;
import com.tamo.util.JsonUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * 线程安全的 JSON 文件读写核心服务（单例模式）
 */
public class JsonDataService {

    private static JsonDataService instance;
    private final String dataFilePath;
    private DataStore cache;
    private final Object lock = new Object();

    private JsonDataService(String dataFilePath) {
        this.dataFilePath = dataFilePath;
        this.cache = loadFromFile();
    }

    public static synchronized JsonDataService getInstance(String basePath) {
        if (instance == null) {
            String path = basePath + File.separator + "data.json";
            instance = new JsonDataService(path);
        }
        return instance;
    }

    public static synchronized JsonDataService getInstance() {
        if (instance == null) {
            throw new IllegalStateException("JsonDataService 尚未初始化，请先调用 getInstance(basePath)");
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
     * 保存数据到文件
     */
    private void saveToFile() {
        try {
            String json = JsonUtil.getGson().toJson(cache);
            Files.writeString(Paths.get(dataFilePath), json, StandardCharsets.UTF_8);
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
}
