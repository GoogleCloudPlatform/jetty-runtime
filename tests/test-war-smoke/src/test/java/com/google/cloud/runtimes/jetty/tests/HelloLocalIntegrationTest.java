/*
 * Copyright (C) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.runtimes.jetty.tests;

import com.google.cloud.runtime.jetty.testing.HttpUrlUtil;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class HelloLocalIntegrationTest extends AbstractHelloIntegrationTest {

  private static URI testUri;
  private static String testPort;

  static {
    testPort = System.getProperty("app.deploy.port");
    Objects.requireNonNull(testPort, "app.deploy.port");
  }

  @BeforeClass
  public static void before() throws Exception {

    testUri = new URI("http://localhost:" + testPort);

    HttpUrlUtil.waitForServerUp(testUri, 10000, TimeUnit.MILLISECONDS);
  }

  @Test
  public void testGetHello() throws IOException {
    assertTestGet(testUri.resolve("/hello"));
  }
}
