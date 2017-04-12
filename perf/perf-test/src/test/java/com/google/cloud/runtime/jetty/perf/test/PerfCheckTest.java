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

package com.google.cloud.runtime.jetty.perf.test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.cloud.runtime.jetty.util.HttpUrlUtil;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class PerfCheckTest {

  @Test
  public void checkQps() throws Exception {

    String serverName = System.getProperty("app.deploy.web");
    Assert.assertNotNull(serverName);
    String runnerName = System.getProperty("app.deploy.runner");
    Assert.assertNotNull(runnerName);
    String projectId = System.getProperty("app.deploy.project");
    Assert.assertNotNull(projectId);

    // construct the remote uri
    StringBuilder baseUriString = new StringBuilder();
    baseUriString.append("https://")
        .append(serverName)
        .append("-dot-");
    baseUriString.append(projectId)
        .append(".")
        .append(System.getProperty("app.deploy.host","appspot.com"))
        .append("/");

    URI baseUri = new URI(baseUriString.toString());

    StringBuilder qps = new StringBuilder();
    qps.append("/log?");
    qps.append("module_id=").append(runnerName);
    qps.append("&text_payload=").append("estimated%20QPS");

    URI qpsCheck = baseUri.resolve(qps.toString());

    System.out.println(qpsCheck);

    HttpURLConnection http = HttpUrlUtil.openTo(qpsCheck);
    assertThat(http.getResponseCode(), is(200));
    String responseBody = HttpUrlUtil.getResponseBody(http);

    List<String> lines =
        new BufferedReader(new StringReader(responseBody)).lines().collect(Collectors.toList());

    // estimated QPS : 1408

    lines.stream().forEach(s -> System.out.println(s.substring(s.lastIndexOf(":"))));

  }

}
