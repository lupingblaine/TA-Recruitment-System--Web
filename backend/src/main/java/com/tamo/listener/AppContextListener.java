package com.tamo.listener;

import com.tamo.service.EmailService;
import com.tamo.service.JsonDataService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            JsonDataService.ensureInitialized(JsonDataService.resolveClassesPath(sce.getServletContext()));
        } catch (IllegalStateException e) {
            System.err.println("[AppContextListener] " + e.getMessage());
        }

        String imagesPath = sce.getServletContext().getRealPath("/static/images");
        if (imagesPath == null) {
            imagesPath = new java.io.File("src/main/webapp/static/images").getAbsolutePath();
        }
        EmailService.getInstance().setImagesBasePath(imagesPath);
    }
}
