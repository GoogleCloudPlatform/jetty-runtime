package com.google.cloud.runtimes.jetty.tests;


import com.google.cloud.runtime.jetty.testing.AppDeployment;
import com.google.cloud.runtime.jetty.testing.HttpUrlUtil;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class JspRemoteIntegrationTest extends AbstractJspIntegrationTest {

  @BeforeClass
  public static void isServerUp() {
    HttpUrlUtil.waitForServerUp(AppDeployment.SERVER_URI, 5, TimeUnit.MINUTES);
  }

  @Test
  public void testJspEnvironment() throws IOException {
    assertTestJspEnvironment(AppDeployment.SERVER_URI.resolve("/jsp/dump.jsp?foo=bar"));
  }

}
