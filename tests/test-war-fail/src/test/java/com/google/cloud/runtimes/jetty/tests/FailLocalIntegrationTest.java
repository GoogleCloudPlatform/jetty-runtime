package com.google.cloud.runtimes.jetty.tests;

import com.google.cloud.runtime.jetty.testing.HttpUrlUtil;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class FailLocalIntegrationTest extends AbstractFailIntegrationTest {

  private static URI testUri;
  private static String testPort;

  static {
    testPort = System.getProperty("app.deploy.port");
    Objects.requireNonNull(testPort, "app.deploy.port");
  }

  @BeforeClass
  public static void before() throws Exception {

    testUri = new URI("http://localhost:" + testPort);

    HttpUrlUtil.waitForServerUp(testUri, 5000, TimeUnit.MILLISECONDS);
  }

  @Test
  public void testGetFail() throws IOException {
    assertTestFail(testUri.resolve("/broken"));
  }
}
