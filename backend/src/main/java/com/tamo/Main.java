package com.tamo;

import com.tamo.service.EmailService;
import com.tamo.service.JsonDataService;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        // 嵌入式 Tomcat 下 Listener 的 getRealPath 可能为 null，必须在启动前初始化数据层
        String classesDir = new File("target/classes").getAbsolutePath();
        JsonDataService.ensureInitialized(classesDir);

        String imagesDir = new File("src/main/webapp/static/images").getAbsolutePath();
        EmailService.getInstance().setImagesBasePath(imagesDir);

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();

        String webappDir = new File("src/main/webapp").getAbsolutePath();
        Context ctx = tomcat.addWebapp("/tamo", webappDir);
        ctx.setParentClassLoader(Main.class.getClassLoader());

        var resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", classesDir, "/"));
        ctx.setResources(resources);

        tomcat.start();
        System.out.println("========================================");
        System.out.println("  TA&MO 后端已启动: http://localhost:8080/tamo");
        System.out.println("  落地页: http://localhost:8080/tamo/index.jsp");
        System.out.println("  登录页: http://localhost:8080/tamo/auth/login.jsp");
        System.out.println("========================================");
        tomcat.getServer().await();
    }
}
