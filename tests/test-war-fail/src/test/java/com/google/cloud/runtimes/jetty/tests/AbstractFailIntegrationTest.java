package com.google.cloud.runtimes.jetty.tests;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.cloud.runtime.jetty.testing.HttpUrlUtil;

import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;

public abstract class AbstractFailIntegrationTest {

  public abstract void testGetFail() throws IOException;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  /**
   * Simple test validating a response code and content.
   *
   * @param target  assembled URI to run test against
   * @throws IOException test in error
   */
  public void assertTestFail(URI target) throws IOException {

    thrown.expect(ConnectException.class);
    thrown.expectMessage("Connection refused");

    assertThat(target.getPath(), containsString("/broken"));

    HttpURLConnection http = HttpUrlUtil.openTo(target);
    assertThat(http.getResponseCode(), is(400));
  }
}
