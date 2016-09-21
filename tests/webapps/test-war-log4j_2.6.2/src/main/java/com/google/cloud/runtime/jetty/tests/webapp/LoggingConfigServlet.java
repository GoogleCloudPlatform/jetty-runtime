package com.google.cloud.runtime.jetty.tests.webapp;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that tweaks the existing configuration.
 */
@WebServlet(urlPatterns = {"/logging-config"})
public class LoggingConfigServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final String LOGID = LoggingConstants.LOGID;
    private Logger log = LogManager.getLogger(LoggingConfigServlet.class);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        // Attempt to reconfigure the level of the root logger
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig rootConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

        // Set the level on root
        rootConfig.setLevel(Level.WARN);
        ctx.updateLoggers();
        log.info(LOGID + " Set root level to WARN");
        log.warn(LOGID + " Set root level to WARN");
        
        // Set the level directly
        LoggerConfig selfConfig = config.getLoggerConfig(log.getName());
        selfConfig.setLevel(Level.WARN);
        log.info(LOGID + " Set self level to WARN");
        log.warn(LOGID + " Set self level to WARN");
        
        // Attempt to add a new console appender
        ConsoleAppender appender = ConsoleAppender.newBuilder()
            .setLayout(PatternLayout.newBuilder()
                .withPattern("[CONFIGURED] %p (%c{1}) %m%n")
                .build())
            .build();
        selfConfig.addAppender(appender, null, null);

        log.info(LOGID + " Added ConsoleAppender");
        log.warn(LOGID + " Added ConsoleAppender");
    }
}
