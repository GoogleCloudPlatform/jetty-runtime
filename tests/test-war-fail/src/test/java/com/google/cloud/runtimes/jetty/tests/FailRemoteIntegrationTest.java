package com.google.cloud.runtimes.jetty.tests;


import com.google.cloud.runtime.jetty.testing.AppDeployment;
import com.google.cloud.runtime.jetty.testing.HttpUrlUtil;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class FailRemoteIntegrationTest extends AbstractFailIntegrationTest {

  @BeforeClass
  public static void isServerUp() {
    HttpUrlUtil.waitForServerUp(AppDeployment.SERVER_URI, 5, TimeUnit.MINUTES);
  }

  @Test
  public void testGetFail() throws IOException {
    assertTestFail(AppDeployment.SERVER_URI.resolve("/broken/"));
  }

}
