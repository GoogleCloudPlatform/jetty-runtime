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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.cloud.runtime.jetty.testing.HttpUrlUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.junit.Assert;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Properties;
import java.util.StringTokenizer;

public abstract class AbstractJspIntegrationTest {

  public abstract void testJspEnvironment() throws IOException;

  /**
   * Simple test validating the JSP environment.
   *
   * @param target  assembled URI to run test against
   * @throws IOException test in error
   */
  public void assertTestJspEnvironment(URI target) throws IOException {

    assertThat(target.getPath(), containsString("/jsp/dump.jsp"));

    HttpURLConnection http = HttpUrlUtil.openTo(target);
    assertThat(http.getResponseCode(), is(200));

    Properties content = new Properties();

    content.load(http.getInputStream());

    Assert.assertEquals("unexpected properties", 5, content.size());
    Assert.assertEquals("/jsp/dump.jsp", content.getProperty("request-uri"));
    Assert.assertEquals("/jsp/dump.jsp", content.getProperty("servlet-path"));
    Assert.assertEquals("null", content.getProperty("path-info"));
    Assert.assertEquals("bar", content.getProperty("param-foo"));
    Assert.assertEquals("jstl support broken", "1", content.getProperty("jstl-example"));
  }
}
