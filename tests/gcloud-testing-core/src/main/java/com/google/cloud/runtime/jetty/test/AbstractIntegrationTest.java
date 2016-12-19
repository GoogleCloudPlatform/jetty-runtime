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

package com.google.cloud.runtime.jetty.test;

import com.google.cloud.runtime.jetty.util.AppDeployment;
import com.google.cloud.runtime.jetty.util.HttpUrlUtil;

import org.junit.Assert;
import org.junit.BeforeClass;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class AbstractIntegrationTest {

  private static URI localUri;
  private static int localWait = 10; // seconds
  private static URI remoteUri;
  private static int remoteWait = 300; // seconds


  public URI getLocalUri() {
    return localUri;
  }

  public URI getRemoteUri() {
    return remoteUri;
  }

  @BeforeClass
  public static void waitForServerUp() throws Exception {

    // Local test property
    String testPort = System.getProperty("app.deploy.port");

    // Remote test property
    String projectId = System.getProperty("app.deploy.project");

    // Local wait for
    if ( testPort != null ) {

      localUri = new URI("http://localhost:" + testPort);
      Assert.assertNotNull("local uri required", localUri);

      HttpUrlUtil.waitForServerUp(localUri, localWait, TimeUnit.SECONDS);
    }

    // Remote wait for
    if (projectId != null ) {
      remoteUri = AppDeployment.SERVER_URI;
      Assert.assertNotNull("remote uri required", remoteUri);

      HttpUrlUtil.waitForServerUp(remoteUri, remoteWait, TimeUnit.SECONDS);
    }
  }
}
