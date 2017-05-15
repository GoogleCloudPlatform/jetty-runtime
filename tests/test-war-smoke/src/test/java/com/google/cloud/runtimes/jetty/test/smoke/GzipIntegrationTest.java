/*
 * Copyright (C) 2017 Google Inc.
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.cloud.runtime.jetty.test.AbstractIntegrationTest;
import com.google.cloud.runtime.jetty.util.HttpUrlUtil;

import org.junit.Test;

import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URI;

public class GzipIntegrationTest extends AbstractIntegrationTest {

  /**
   * Test that the server respond with gzip header when the client allows it.
   */
  @Test
  public void testGzip() throws IOException {
    URI target = getUri().resolve("/dump/");
    HttpURLConnection http = HttpUrlUtil.openTo(target);
    http.setRequestProperty("Accept-Encoding", "gzip");
    String encoding = http.getHeaderField("Content-Encoding");
    assertThat(encoding, is("gzip"));
  }

  /**
   * Test that the response is not compressed with gzip if the browser does not support it.
   */
  @Test
  public void testUnsupportedGzip() throws IOException {
    URI target = getUri().resolve("/dump/");
    HttpURLConnection http = HttpUrlUtil.openTo(target);
    http.setRequestProperty("Accept-Encoding", "");
    String encoding = http.getHeaderField("Content-Encoding");
    assertThat(encoding, is(nullValue()));
  }

}
