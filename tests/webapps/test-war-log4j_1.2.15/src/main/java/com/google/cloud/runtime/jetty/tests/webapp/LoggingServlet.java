package com.google.cloud.runtime.jetty.tests.webapp;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class LoggingServlet
 */
@WebServlet(urlPatterns = {"/logging"})
public class LoggingServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private Logger log = Logger.getLogger(LoggingServlet.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoggingServlet()
    {
        log.debug(LoggingConstants.LOGID + " initialized");
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        log.info(LoggingConstants.LOGID + " GET requested");

        log.warn(LoggingConstants.LOGID + " Slightly warn, with a chance of log events");

        log.error(LoggingConstants.LOGID + " Nothing is (intentionally) being output by this Servlet");

        IOException severe = new FileNotFoundException("A file cannot be found");

        log.fatal(LoggingConstants.LOGID + " Whoops (intentionally) causing a Throwable",severe);
    }
}
