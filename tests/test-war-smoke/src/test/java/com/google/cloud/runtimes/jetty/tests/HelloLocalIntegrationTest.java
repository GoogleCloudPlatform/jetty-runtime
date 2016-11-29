package com.google.cloud.runtimes.jetty.tests;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class HelloLocalIntegrationTest extends AbstractHelloIntegrationTest {
  @BeforeClass
  public static void before() throws Exception {
    // give app time to start up
    Thread.sleep(2000);
  }

  @Test
  public void testGetHello() throws IOException {
    String testPort = System.getProperty("app.deploy.port");
    Objects.requireNonNull(testPort, "app.deploy.port");

    try {
      URI container = new URI("http://localhost:" + testPort + "/hello");

      assertTestGet(container);
    } catch (URISyntaxException e) {
      throw new IOException(e);
    }
  }
}
