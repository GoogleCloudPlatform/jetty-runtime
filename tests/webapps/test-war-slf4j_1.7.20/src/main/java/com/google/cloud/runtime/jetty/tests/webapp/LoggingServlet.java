package com.google.cloud.runtime.jetty.tests.webapp;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class LoggingServlet
 */
public class LoggingServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final String VERSION = "(slf4j-1.7.20)";
    private Logger log = LoggerFactory.getLogger(LoggingServlet.class);

    /**
     * Default constructor.
     */
    public LoggingServlet()
    {
        log.debug(VERSION + " initialized");
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        log.info(VERSION + " GET requested");

        log.warn(VERSION + " Slightly warn, with a chance of log events");

        log.error(VERSION + " Nothing is (intentionally) being output by this Servlet");

        Throwable severe = new GoneBadException("The app went bad");

        log.error(VERSION + " Whoops (intentionally) causing a Throwable",severe);
    }

}
