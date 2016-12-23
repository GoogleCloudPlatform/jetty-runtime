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
import static org.junit.Assert.assertNotNull;


import com.google.cloud.runtime.jetty.test.AbstractIntegrationTest;
import com.google.cloud.runtime.jetty.test.annotation.LocalOnly;
import com.google.cloud.runtime.jetty.util.HttpUrlUtil;

import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;


/**
 * Trivial test in local mode container only to check that the default,
 * non-clustered session management works.
 *
 */
public class LocalSessionIntegrationTest extends AbstractIntegrationTest {

 
  @Test
  @LocalOnly
  public void testCreate() throws IOException {

    URI target = getUri().resolve("/session/?a=create");

    assertThat(target.getPath(), containsString("/session"));

    HttpURLConnection http = HttpUrlUtil.openTo(target);
    assertThat(http.getResponseCode(), is(200));
    String responseBody = HttpUrlUtil.getResponseBody(http);
    assertThat(responseBody, containsString("SESSION: id="));
    String id = responseBody.substring(responseBody.indexOf("=") + 1);
    assertNotNull(id);
    id = id.trim();
    String setCookie = http.getHeaderField("Set-Cookie");
    assertThat(setCookie, containsString("JSESSIONID"));
    assertThat(setCookie, containsString(id));
  }

}
