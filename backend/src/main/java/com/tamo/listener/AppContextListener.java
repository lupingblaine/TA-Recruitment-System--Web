package com.tamo.listener;

import com.tamo.service.EmailService;
import com.tamo.service.JsonDataService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
/**
 * Implementation of AppContextListener.
 * Provides necessary operations and logic for the system.
 */
public class AppContextListener implements ServletContextListener {
    private java.util.concurrent.ScheduledExecutorService expiryWorker;
    @Override public void contextDestroyed(ServletContextEvent sce) {
        if(expiryWorker!=null) expiryWorker.shutdownNow();
    }

    @Override
    /**
     * Executes the contextInitialized operation.
     * @param sce the sce argument
     */
    /**
     * Executes the contextInitialized operation.
     * @param sce the sce argument
     */
    public void contextInitialized(ServletContextEvent sce) {
        JsonDataService.ensureInitialized(JsonDataService.resolveClassesPath(sce.getServletContext()));

        expiryWorker=java.util.concurrent.Executors.newSingleThreadScheduledExecutor(task->{
            Thread thread=new Thread(task,"assignment-expiry");thread.setDaemon(true);return thread;
        });
        expiryWorker.scheduleWithFixedDelay(()->{
            try {JsonDataService.getInstance().processAssignmentExpiry();}
            catch(Exception e){sce.getServletContext().log("Assignment expiry check failed; will retry.",e);}
        },0,15,java.util.concurrent.TimeUnit.SECONDS);

        String imagesPath = sce.getServletContext().getRealPath("/static/images");
        if (imagesPath == null) {
            imagesPath = new java.io.File("src/main/webapp/static/images").getAbsolutePath();
        }
        EmailService.getInstance().setImagesBasePath(imagesPath);
    }
}
