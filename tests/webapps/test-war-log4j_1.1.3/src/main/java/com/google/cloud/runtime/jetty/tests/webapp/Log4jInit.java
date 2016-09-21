package com.google.cloud.runtime.jetty.tests.webapp;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Category;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;

public class Log4jInit implements ServletContextListener
{
    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        Category root = Category.getRoot();
        ConsoleAppender stderr = new ConsoleAppender(new PatternLayout("[%p] - %m%n"));
        stderr.setTarget("System.err");
        root.addAppender(stderr);
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        /* do nothing */
    }
}
