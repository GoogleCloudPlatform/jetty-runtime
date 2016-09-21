package com.google.cloud.runtime.jetty.tests.webapp;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class LogInit implements ServletContextListener
{
    public static final String VERSION = "(slf4j-1.6.1)";
    
    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        /* do nothing */
    }
}
