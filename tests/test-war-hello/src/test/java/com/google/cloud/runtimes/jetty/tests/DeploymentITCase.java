package com.google.cloud.runtimes.jetty.tests;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.eclipse.jetty.toolchain.test.IO;
import org.junit.Test;

public class DeploymentITCase
{
    private static final URI serverURI;
    
    static
    {
        String projectId = System.getProperty("app.deploy.project");
        String version = System.getProperty("app.deploy.version");
        
        Objects.requireNonNull(projectId, "app.deploy.project");
        Objects.requireNonNull(version, "app.deploy.version");
        
        serverURI = URI.create(String.format("https://%s-dot-%s.appspot.com/", version, projectId));
    }
    
    private String getResponseBody(HttpURLConnection http) throws IOException
    {
        Charset responseEncoding = StandardCharsets.UTF_8;
        if (http.getContentEncoding() != null)
        {
            responseEncoding = Charset.forName(http.getContentEncoding());
        }
        
        try (InputStream in = http.getInputStream();
             InputStreamReader reader = new InputStreamReader(in, responseEncoding);
             StringWriter writer = new StringWriter())
        {
            IO.copy(reader, writer);
            return writer.toString();
        }
    }
    
    @Test
    public void testGet() throws IOException
    {
        HttpURLConnection http = (HttpURLConnection) serverURI.resolve("/hello/").toURL().openConnection();
        assertThat(http.getResponseCode(), is(200));
        
        String responseBody = getResponseBody(http);
        assertThat(responseBody, containsString("Hello from Servlet 3.1"));
    }
}
