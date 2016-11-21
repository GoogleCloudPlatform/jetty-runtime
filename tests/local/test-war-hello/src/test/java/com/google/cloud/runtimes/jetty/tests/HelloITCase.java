package com.google.cloud.runtimes.jetty.tests;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.cloud.runtime.jetty.testing.HttpUrlUtil;

import org.junit.BeforeClass;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Objects;

public class HelloITCase
{
    @BeforeClass
    public static void before() throws Exception
    {
      Thread.sleep(1000);
    }

    @Test
    public void testGet() throws Exception
    {
        String testPort = System.getProperty("test.port");
        Objects.requireNonNull(testPort, "test.port");

        URI container = new URI("http://localhost:" + testPort);
        HttpURLConnection http = HttpUrlUtil.openTo(container.resolve("/hello/"));

        assertThat(http.getResponseCode(), is(200));
        String responseBody = HttpUrlUtil.getResponseBody(http);
        assertThat(responseBody, containsString("Hello from Servlet 3.1"));
    }
}
