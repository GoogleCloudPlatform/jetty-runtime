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

import org.junit.Assert;
import org.junit.Test;

public class DeploymentITCase
{
    @Test
    public void testGet() throws IOException
    {
        // Trigger events
        HttpURLConnection http = HttpURLUtil.openTo(AppDeployment.SERVER_URI.resolve("/logging"));
        Assert.assertThat(http.getResponseCode(), is(200));
        
        // Fetch logging events on server
        List<RemoteLog.Entry> logs = RemoteLog.getLogs(AppDeployment.MODULE_ID, AppDeployment.VERSION_ID);
        
        List<String> expectedEntries = new ArrayList<>();
        
        String classMethodFile = "com.google.cloud.runtime.jetty.tests.webapp.LoggingServlet.doGet (LoggingServlet.java)";
        
        expectedEntries.add(String.format("[INFO ] %s - (slf4j-1.7.20) GET requested", classMethodFile));
        expectedEntries.add(String.format("[WARN ] %s - (slf4j-1.7.20) Slightly warn, with a chance of log events", classMethodFile));
        expectedEntries.add(String.format("[ERROR] %s - (slf4j-1.7.20) Nothing is (intentionally) being output by this Servlet", classMethodFile));
        
        RemoteLog.assertHasEntries(logs, expectedEntries);
        
        RemoteLog.Entry entry = RemoteLog.findEntry(logs, String.format("[ERROR] %s - (slf4j-1.7.20) Whoops (intentionally) causing a Throwable", classMethodFile));
        assertThat("Multi-Line Log", entry.getTextPayload(), containsString("com.google.cloud.runtime.jetty.tests.webapp.GoneBadException: The app went bad"));
        assertThat("Multi-Line Log", entry.getTextPayload(), containsString("at com.google.cloud.runtime.jetty.tests.webapp.LoggingServlet.doGet(LoggingServlet.java"));
    }
}
