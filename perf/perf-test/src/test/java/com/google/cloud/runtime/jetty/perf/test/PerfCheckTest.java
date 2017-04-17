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

import com.google.cloud.runtime.jetty.util.HttpUrlUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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
    qps.append("&text_payload=").append(URLEncoder.encode("estimated QPS"));
    appendTimestamp( qps );

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

  @Test
  @Ignore
  public void checkLatency() throws Exception {

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

    StringBuilder url = new StringBuilder();
    url.append("/log?");
    url.append("module_id=").append(runnerName);
    url.append("&text_payload=").append(URLEncoder.encode("perfmetric:ave_latency:"));
    appendTimestamp( url );

    URI qpsCheck = baseUri.resolve(url.toString());

    System.out.println("param filer:" + qpsCheck);

    HttpURLConnection http = HttpUrlUtil.openTo(qpsCheck);
    assertThat(http.getResponseCode(), is(200));
    String responseBody = HttpUrlUtil.getResponseBody(http);

    List<String> lines =
        new BufferedReader(new StringReader(responseBody)).lines().collect(Collectors.toList());

    // estimated QPS : 1408

    lines.stream().forEach(s -> System.out.println(s.substring(s.lastIndexOf(":"))));

  }

  private void appendTimestamp(StringBuilder stringBuilder) {
    // last 1H
    Instant now = Instant.now();
    DateTimeFormatter dtf = new DateTimeFormatterBuilder()
        .appendValue( ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
        .appendLiteral('-')
        .appendValue(ChronoField.MONTH_OF_YEAR, 2)
        .appendLiteral('-')
        .appendValue(ChronoField.DAY_OF_MONTH, 2)
        .appendLiteral('T')
        .appendValue(ChronoField.HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
        .optionalStart()
        .appendLiteral(':')
        .appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();

    System.out.println("now:" + dtf //DateTimeFormatter.ISO_OFFSET_DATE_TIME
                            .withZone( ZoneId.systemDefault() ).format( Instant.now() ) );
    String timestamp = dtf.withZone( ZoneId.systemDefault() )
        .format(now); // .minus( 1, ChronoUnit.HOURS )

    stringBuilder.append( "&timestamp=" ).append( timestamp );
  }

}
