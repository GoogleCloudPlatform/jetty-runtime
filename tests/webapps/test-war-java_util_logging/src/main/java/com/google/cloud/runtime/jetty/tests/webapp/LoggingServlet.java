package com.google.cloud.runtime.jetty.tests.webapp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class LoggingServlet
 */
public class LoggingServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final String LOGID = "LoggingServlet(java.util.logging)";
    private Logger log = Logger.getLogger(LoggingServlet.class.getName());

    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoggingServlet()
    {
        log.log(Level.FINE,LOGID + " initialized");
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        log.log(Level.INFO,LOGID + " GET requested");

        log.log(Level.WARNING,LOGID + " Slightly warn, with a chance of log events");

        log.log(Level.WARNING,LOGID + " Nothing is (intentionally) being output by this Servlet");

        IOException severe = new FileNotFoundException("A file cannot be found");

        log.log(Level.SEVERE,LOGID + " Whoops (intentionally) causing a Throwable",severe);

    }
}
