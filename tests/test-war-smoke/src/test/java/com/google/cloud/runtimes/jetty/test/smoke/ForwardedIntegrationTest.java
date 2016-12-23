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
import com.google.cloud.runtime.jetty.test.annotation.LocalOnly;
import com.google.cloud.runtime.jetty.util.HttpUrlUtil;

import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

public class ForwardedIntegrationTest extends AbstractIntegrationTest {

  /**
   * Validate proxy proto header is handled.
   *
   * @throws IOException test in error
   */
  @Test
  @LocalOnly
  public void testLocalProto() throws IOException {
    URI target = getUri().resolve("/dump/info");
    assertThat(target.getPath(), containsString("/dump/info"));

    HttpURLConnection http = HttpUrlUtil.openTo(target);
    http.setRequestProperty("X-Forwarded-Proto", "https");

    assertThat(http.getResponseCode(), is(200));
    String responseBody = HttpUrlUtil.getResponseBody(http);
    assertThat(responseBody, containsString("scheme=https(secure=true)"));
  }
  
  /**
   * Validate proxy for header is handled.
   *
   * @throws IOException test in error
   */
  @Test
  @LocalOnly
  public void testLocal() throws IOException {
    URI target = getUri().resolve("/dump/info");
    assertThat(target.getPath(), containsString("/dump/info"));

    HttpURLConnection http = HttpUrlUtil.openTo(target);
    http.setRequestProperty("X-Forwarded-For", "1.2.3.4,5.6.7.8");

    assertThat(http.getResponseCode(), is(200));
    String responseBody = HttpUrlUtil.getResponseBody(http);
    assertThat(responseBody, containsString("remoteHost/Addr:port=1.2.3.4"));
  }

}
