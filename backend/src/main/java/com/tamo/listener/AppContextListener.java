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
        String basePath = sce.getServletContext().getRealPath("/WEB-INF/classes");
        if (basePath != null) {
            JsonDataService.getInstance(basePath);
        }

        String imagesPath = sce.getServletContext().getRealPath("/static/images");
        if (imagesPath != null) {
            EmailService.getInstance().setImagesBasePath(imagesPath);
        }
    }
}
