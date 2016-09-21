package com.google.cloud.runtimes.jetty.tests;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.cloud.runtime.jetty.testing.AppDeployment;
import com.google.cloud.runtime.jetty.testing.HttpURLUtil;
import com.google.cloud.runtime.jetty.testing.RemoteLog;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class DeploymentITCase
{
    @Test
    public void testGet() throws IOException
    {
        HttpURLConnection http = HttpURLUtil.openTo(AppDeployment.SERVER_URI.resolve("/logging"));
        assertThat(http.getResponseCode(), is(200));
        
        // Fetch logging events on server
        List<RemoteLog.Entry> logs = RemoteLog.getLogs(AppDeployment.MODULE_ID, AppDeployment.VERSION_ID);
        
        List<String> expectedEntries = new ArrayList<>();
        expectedEntries.add("WARNING: LoggingServlet(commons-logging-1.0.3) Nothing is (intentionally) being output by this Servlet");
        expectedEntries.add("WARNING: LoggingServlet(commons-logging-1.0.3) Slightly warn, with a chance of log events");
        expectedEntries.add("INFO: LoggingServlet(commons-logging-1.0.3) GET requested");
        
        RemoteLog.assertHasEntries(logs, expectedEntries);
        
        RemoteLog.Entry entry = RemoteLog.findEntry(logs, "SEVERE: LoggingServlet(commons-logging-1.0.3) Whoops (intentionally) causing a Throwable");
        assertThat("Multi-Line Log", entry.getTextPayload(), containsString("java.io.FileNotFoundException: A file cannot be found"));
        assertThat("Multi-Line Log", entry.getTextPayload(), containsString("at com.google.cloud.runtime.jetty.tests.webapp.LoggingServlet.doGet(LoggingServlet.java"));
    }
}
