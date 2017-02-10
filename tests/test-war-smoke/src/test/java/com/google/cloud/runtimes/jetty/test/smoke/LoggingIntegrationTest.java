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
import com.google.cloud.runtime.jetty.test.annotation.RemoteOnly;
import com.google.cloud.runtime.jetty.util.HttpUrlUtil;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class LoggingIntegrationTest extends AbstractIntegrationTest {
  @Test
  @RemoteOnly
  public void testForwardedIgnored() throws Exception {
    String id = Long.toHexString(System.nanoTime());
    URI target = getUri().resolve("/dump/info/" + id);
    assertThat(target.getPath(), containsString("/dump/info/" + id));

    HttpURLConnection http = HttpUrlUtil.openTo(target);
    assertThat(http.getResponseCode(), is(200));
    String responseBody = HttpUrlUtil.getResponseBody(http);
    
    assertThat(responseBody, containsString(id));
    
    TimeUnit.SECONDS.sleep(1);
    
    target = getUri().resolve("/log/" + id);

    http = HttpUrlUtil.openTo(target);
    assertThat(http.getResponseCode(), is(200));
    responseBody = HttpUrlUtil.getResponseBody(http);
        
    BufferedReader in = new BufferedReader(new StringReader(responseBody));
    String line = in.readLine();
    assertThat(line,containsString("Log Entries "));
    assertThat(line,containsString("textPayload:" + id));
    
    line = in.readLine();
    assertThat(line,containsString("JUL.info:/dump/info/" + id));
    
    line = in.readLine();
    assertThat(line,containsString("ServletContext.log:/dump/info/" + id));
  }
}
