package com.google.cloud.runtime.jetty.tests.webapp;

import java.io.FileNotFoundException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;

/**
 * Servlet implementation class LoggingServlet
 */
public class LoggingServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final String LOGID = "LoggingServlet(log4j-1.1.3)";
    private Category log = Category.getInstance(LoggingServlet.class);

    /**
     * @see HttpServlet#HttpServlet()
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

        log.fatal(LOGID + " Whoops (intentionally) causing a Throwable",severe);
    }
}
