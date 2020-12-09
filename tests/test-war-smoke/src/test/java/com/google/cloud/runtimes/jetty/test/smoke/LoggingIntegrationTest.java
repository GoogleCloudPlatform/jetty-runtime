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

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import com.google.api.gax.paging.Page;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.Logging.EntryListOption;
import com.google.cloud.logging.LoggingOptions;
import com.google.cloud.logging.Severity;
import com.google.cloud.runtime.jetty.test.AbstractIntegrationTest;
import com.google.cloud.runtime.jetty.test.annotation.RemoteOnly;
import com.google.cloud.runtime.jetty.util.HttpUrlUtil;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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
    String traceId = lines.stream().filter(s -> s.startsWith("X-Cloud-Trace-Context: ")).findFirst()
        .get().split("[ /]")[1];
    assertThat(traceId, Matchers.notNullValue());

    // wait for logs to propagate
    Thread.sleep(30 * 1000);

    LoggingOptions options = LoggingOptions.newBuilder()
        .setProjectId(System.getProperty("app.deploy.project"))
        .build();

    String moduleId = System.getProperty("app.deploy.service");
    String filter =
        "resource.type=gae_app AND resource.labels.module_id=" + moduleId
        + " AND textPayload:" + id;

    int expected = 2;
    try (Logging logging = options.getService()) {
      Page<LogEntry> entries = logging.listLogEntries(EntryListOption.filter(filter));
      for (LogEntry entry : entries.iterateAll()) {
        if (entry.getSeverity() == Severity.INFO) {
          assertThat(entry.getLogName(), is("gae_app.log"));
          assertThat(entry.getResource().getType(), is("gae_app"));
          assertThat(entry.getResource().getLabels().get("module_id"), is(moduleId));
          assertThat(entry.getResource().getLabels().get("zone"), Matchers.notNullValue());
          String[] traceResults = entry.getTrace().toString().split("/");
          assertThat(traceResults[traceResults.length - 1], is(traceId));
          assertThat(entry.getLabels().get("appengine.googleapis.com/instance_name"),
              Matchers.notNullValue());

          if (entry.getPayload().toString().contains("JUL.info:/dump/info/")) {
            expected--;
            assertThat(entry.getPayload().toString(), Matchers.containsString(id));
          }
          if (entry.getPayload().toString().contains("ServletContext.log:/dump/info")) {
            expected--;
            assertThat(entry.getPayload().toString(), Matchers.containsString(id));
          }
        }
      }
      assertThat(expected, is(0));
    }
  }

  @Test
  public void testClassPath() throws Exception {

    URI target = getUri().resolve("/classloader");
    HttpURLConnection http = HttpUrlUtil.openTo(target);
    String responseBody = HttpUrlUtil.getResponseBody(http);
    assertThat(responseBody, http.getResponseCode(), is(200));

    Assert.assertThat(responseBody, Matchers.containsString("Found classes = 0 (0 expected)"));
    Assert.assertThat(responseBody, Matchers.containsString("Not found classes = 7 (7 expected)"));
  }
}
