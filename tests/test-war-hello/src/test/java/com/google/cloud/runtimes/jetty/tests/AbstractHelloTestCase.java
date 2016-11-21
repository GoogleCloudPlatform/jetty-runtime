package com.google.cloud.runtimes.jetty.tests;

import com.google.cloud.runtime.jetty.testing.HttpUrlUtil;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;


public abstract class AbstractHelloTestCase {


  public abstract void testGet() throws IOException;

  public void assertTestGet(URI target) throws IOException
  {

      assertThat (target.getPath(), containsString("/hello") );

      HttpURLConnection http = HttpUrlUtil.openTo(target);
      assertThat(http.getResponseCode(), is(200));
      String responseBody = HttpUrlUtil.getResponseBody(http);
      assertThat(responseBody, containsString("Hello from Servlet 3.1"));
  }
}
