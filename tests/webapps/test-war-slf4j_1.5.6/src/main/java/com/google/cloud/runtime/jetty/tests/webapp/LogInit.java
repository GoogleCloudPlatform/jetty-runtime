package com.google.cloud.runtime.jetty.tests.webapp;

import java.io.IOException;
import java.net.URL;
import java.util.logging.LogManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class LogInit implements ServletContextListener
{
    public static final String VERSION = "(slf4j-1.5.6)";
    
    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        initJavaUtilLogging("/WEB-INF/logging.properties");
    }
    
    @SuppressWarnings("Duplicates")
    private void initJavaUtilLogging(String resourceName)
    {
        try
        {
            URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);
            if (url == null)
            {
                throw new RuntimeException("Unable to init java.util.logging. " + resourceName + " not found");
            }
            LogManager.getLogManager().readConfiguration(url.openStream());
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to init java.util.logging. read failure in " + resourceName, e);
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        /* do nothing */
    }
}
