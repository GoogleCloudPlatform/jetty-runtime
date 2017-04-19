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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.cloud.runtime.jetty.perf.PerfRunner;
import com.google.cloud.runtime.jetty.util.HttpUrlUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class PerfCheckTest {

  private HttpClient httpClient;

  private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Before
  public void initialize() throws Exception {
    this.httpClient = new HttpClient(new SslContextFactory(true) );
    httpClient.start();
  }

  @After
  public void shutdown() throws Exception {
    if (!this.httpClient.isStopped()) {
      this.httpClient.stop();
    }
  }

  @Test
  public void checkQps() throws Exception {
    String runnerName = System.getProperty("app.deploy.runner");
    Assert.assertNotNull(runnerName);
    String projectId = System.getProperty("app.deploy.project");
    Assert.assertNotNull(projectId);
    String testQpsRange = System.getProperty("test.qps.range");
    Assert.assertNotNull(testQpsRange);

    // construct the remote uri
    StringBuilder baseUriString = new StringBuilder();
    baseUriString.append("https://")
        .append(runnerName)
        .append("-dot-");
    baseUriString.append(projectId)
        .append(".")
        .append(System.getProperty("app.deploy.host","appspot.com"));
    PerfRunner.RunStatus runStatus = getStatus( baseUriString.toString() );
    System.out.println( "qps:" + runStatus.getQps() //
                            + ", ave latency:" + runStatus.getAveLatency() //
                            + ", latency 50:" + runStatus.getLatency50() //
                            + ", latency 90:" + runStatus.getLatency90());

    String[] testTargetQps = testQpsRange.split("-");


    Assert.assertFalse("average qps below required levels: " + testTargetQps[0],
        runStatus.getQps() < Long.parseLong(testTargetQps[0]));
    Assert.assertFalse("average qps above required levels: " + testTargetQps[1],
        runStatus.getQps() > Long.parseLong(testTargetQps[1]));


  }

  private PerfRunner.RunStatus getStatus( String baseUri) throws Exception {
    // FIXME could depends on time unit as well
    long timeoutMinutes = Long.getLong( "running.time", 15 );
    long start = new Date().getTime();
    while (true) {
      ContentResponse contentResponse = httpClient.newRequest( baseUri + "/status" ).send();
      if (contentResponse.getStatus() == 200) {
        String json = contentResponse.getContentAsString();
        System.out.println( "json:" + json );
        PerfRunner.RunStatus runStatus = objectMapper.readValue( json, PerfRunner.RunStatus.class );
        System.out.println( "runStatus:" + runStatus.getEta() );
        if (runStatus.getEta() == PerfRunner.Eta.FINISHED) {
          return runStatus;
        }
      }
      if (new Date().getTime() - start
          > TimeUnit.MILLISECONDS.convert( timeoutMinutes, TimeUnit.MINUTES ) ) {
        throw new RuntimeException( "cannot get status 200 after "
                                        + timeoutMinutes + " minutes" );
      }
      // still not finished so we wait
      Thread.sleep( 10000 );
    }
  }

  @Test
  @Ignore
  public void checkQpsWithLogs() throws Exception {

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
  public void checkLatency() throws Exception {

    String runnerName = System.getProperty("app.deploy.runner");
    Assert.assertNotNull(runnerName);
    String projectId = System.getProperty("app.deploy.project");
    Assert.assertNotNull(projectId);
    String testLatencyRange = System.getProperty("test.latency.range");
    Assert.assertNotNull(testLatencyRange);

    // construct the remote uri
    StringBuilder baseUriString = new StringBuilder();
    baseUriString.append("https://")
        .append(runnerName)
        .append("-dot-");
    baseUriString.append(projectId)
        .append(".")
        .append(System.getProperty("app.deploy.host","appspot.com"));
    PerfRunner.RunStatus runStatus = getStatus( baseUriString.toString() );
    System.out.println( "qps:" + runStatus.getQps() //
        + ", ave latency:" + runStatus.getAveLatency() //
        + ", latency 50:" + runStatus.getLatency50() //
        + ", latency 90:" + runStatus.getLatency90());

    String[] testTargetLatencies = testLatencyRange.split("-");


    Assert.assertFalse("average latency below required levels: " + testTargetLatencies[0],
        runStatus.getAveLatency() < Long.parseLong(testTargetLatencies[0]));
    Assert.assertFalse("average latency above required levels: " + testTargetLatencies[1],
        runStatus.getAveLatency() > Long.parseLong(testTargetLatencies[1]));


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
