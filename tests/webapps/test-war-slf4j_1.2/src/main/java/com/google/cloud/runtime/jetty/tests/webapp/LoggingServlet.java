package com.google.cloud.runtime.jetty.tests.webapp;

import java.io.FileNotFoundException;
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
    private static final String LOGID = "LoggingServlet(slf4j-1.2)";
    private Logger log = LoggerFactory.getLogger(LoggingServlet.class);

    /**
     * Default constructor.
     */
    public LoggingServlet()
    {
        log.debug(LOGID + " initialized");
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @SuppressWarnings("Duplicates")
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        log.info(LOGID + " GET requested");

        log.warn(LOGID + " Slightly warn, with a chance of log events");

        log.error(LOGID + " Nothing is (intentionally) being output by this Servlet");

        IOException severe = new FileNotFoundException("A file cannot be found");

        log.error(LOGID + " Whoops (intentionally) causing a Throwable",severe);
    }

}
