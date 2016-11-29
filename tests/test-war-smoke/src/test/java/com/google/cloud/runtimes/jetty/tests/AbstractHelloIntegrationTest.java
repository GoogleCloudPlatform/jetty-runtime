package com.google.cloud.runtimes.jetty.tests;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.cloud.runtime.jetty.testing.HttpUrlUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

public abstract class AbstractHelloIntegrationTest {

  public abstract void testGetHello() throws IOException;

  /**
   * Simple test validating a response code and content.
   *
   * @param target  assembled URI to run test against
   * @throws IOException test in error
   */
  public void assertTestGet(URI target) throws IOException {

    assertThat(target.getPath(), containsString("/hello"));

    HttpURLConnection http = HttpUrlUtil.openTo(target);
    assertThat(http.getResponseCode(), is(200));
    String responseBody = HttpUrlUtil.getResponseBody(http);
    assertThat(responseBody, containsString("Hello from Servlet 3.1"));
  }
}
