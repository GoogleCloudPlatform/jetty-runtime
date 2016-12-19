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

package com.google.cloud.runtimes.jetty.test.smoke;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.cloud.runtime.jetty.test.AbstractIntegrationTest;
import com.google.cloud.runtime.jetty.util.HttpUrlUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

public abstract class AbstractHelloIntegrationTest extends AbstractIntegrationTest {

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
