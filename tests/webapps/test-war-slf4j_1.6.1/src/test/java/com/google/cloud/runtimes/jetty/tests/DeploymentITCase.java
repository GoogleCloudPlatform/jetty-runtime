package com.google.cloud.runtimes.jetty.tests;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.cloud.runtime.jetty.testing.AppDeployment;
import com.google.cloud.runtime.jetty.testing.HttpURLUtil;
import com.google.cloud.runtime.jetty.testing.RemoteLog;
import com.google.cloud.runtime.jetty.tests.webapp.Fruit;
import com.google.cloud.runtime.jetty.tests.webapp.Meat;
import com.google.cloud.runtime.jetty.tests.webapp.Veggie;

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
        
        String[][] types = new String[][]{
                {Fruit.class.getSimpleName(), "log4j"},
                {Veggie.class.getSimpleName(), "commons-logging"},
                {Meat.class.getSimpleName(), "slf4j"}
        };
        
        for (int i = 0; i < types.length; i++)
        {
            String logName = types[i][0];
            String logType = types[i][1];
            
            List<String> expectedEntries = new ArrayList<>();
            
            expectedEntries.add(String.format("[DEBUG] %s - (slf4j-1.6.1) some %s debug", logName, logType));
            expectedEntries.add(String.format("[INFO ] %s - (slf4j-1.6.1) some %s info", logName, logType));
            expectedEntries.add(String.format("[WARN ] %s - (slf4j-1.6.1) some %s warning", logName, logType));
            
            RemoteLog.assertHasEntries(logs, expectedEntries);
            
            RemoteLog.Entry entry = RemoteLog.findEntry(logs,
                    String.format("[ERROR] %s - (slf4j-1.6.1) some %s error", logName, logType));
            assertThat("Multi-Line Log", entry.getTextPayload(), containsString("java.lang.RuntimeException: Gone Bad"));
            assertThat("Multi-Line Log", entry.getTextPayload(), containsString("at com.google.cloud.runtime.jetty.tests.webapp.LoggingServlet.doGet(LoggingServlet.java"));
        }
    }
}
