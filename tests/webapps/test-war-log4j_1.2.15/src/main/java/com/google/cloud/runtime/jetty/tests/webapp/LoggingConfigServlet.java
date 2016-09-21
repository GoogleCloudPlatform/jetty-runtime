package com.google.cloud.runtime.jetty.tests.webapp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * Servlet that tweaks the existing configuration.
 */
@WebServlet(urlPatterns = {"/logging-config"})
public class LoggingConfigServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final String LOGID = LoggingConstants.LOGID;
    private Logger log = Logger.getLogger(LoggingConfigServlet.class);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        // Attempt to reconfigure the level of the root logger
        Logger root = Logger.getRootLogger();
        root.setLevel(Level.WARN);
        log.info(LOGID + " Set root level to WARN");
        log.warn(LOGID + " Set root level to WARN");
        
        // Set the level directly
        log.setLevel(Level.WARN);
        log.info(LOGID + " Set self level to WARN");
        log.warn(LOGID + " Set self level to WARN");
        
        // Attempt to add a new console appender
        Layout layout = new PatternLayout("[CONFIGURED] %p (%c{1}) %m%n");
        ConsoleAppender appender = new ConsoleAppender(layout);
        log.addAppender(appender);
        
        log.info(LOGID + " Added ConsoleAppender");
        log.warn(LOGID + " Added ConsoleAppender");
    }
}
