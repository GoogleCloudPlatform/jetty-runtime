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

import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class LoggingIntegrationTest extends AbstractIntegrationTest {
  
  
  @Test
  @RemoteOnly
  public void testLogging() throws Exception {

    // Create unique ID to relate request with log entries
    String id = Long.toHexString(System.nanoTime());

    // Hit the DumpServlet that will log the request path
    URI target = getUri().resolve("/dump/info/" + id);
    HttpURLConnection http = HttpUrlUtil.openTo(target);
    assertThat(http.getResponseCode(), is(200));
    String responseBody = HttpUrlUtil.getResponseBody(http);
    
    List<String> lines =
        new BufferedReader(new StringReader(responseBody)).lines().collect(Collectors.toList());
    assertThat(lines.stream().filter(s -> s.startsWith("requestURI=")).findFirst().get(),
        containsString(id));
    String traceId = lines.stream().filter(s -> s.startsWith("X-Cloud-Trace-Context: "))
        .findFirst().get().split("[ /]")[1];
    assertThat(traceId,Matchers.notNullValue());
    
    // Hit the LogServlet to query the resulting log entries
    target = getUri().resolve("/log/" + id);

    http = HttpUrlUtil.openTo(target);
    assertThat(http.getResponseCode(), is(200));
    responseBody = HttpUrlUtil.getResponseBody(http);
            
    BufferedReader in = new BufferedReader(new StringReader(responseBody));
    String line = in.readLine();
    assertThat(line,containsString("Log Entries "));
    assertThat(line,containsString("textPayload:" + id));
    
    line = in.readLine();
    while (line != null && !line.contains("severity=INFO")) {
      line = in.readLine();
    }
    assertThat(line,containsString("JUL.info:/dump/info/" + id));
    assertThat(line,containsString("appengine.googleapis.com/trace_id=" + traceId));
    assertThat(line,containsString("zone="));
    
    line = in.readLine();
    while (line != null && !line.contains("severity=INFO")) {
      line = in.readLine();
    }
    assertThat(line,containsString("ServletContext.log:/dump/info/" + id));
    assertThat(line,containsString("appengine.googleapis.com/trace_id=" + traceId));
    assertThat(line,containsString("zone="));
  }
}
