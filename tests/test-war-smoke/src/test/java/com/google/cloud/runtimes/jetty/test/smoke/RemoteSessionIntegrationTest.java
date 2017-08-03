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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.runtime.jetty.test.AbstractIntegrationTest;
import com.google.cloud.runtime.jetty.test.annotation.RemoteOnly;
import com.google.cloud.runtime.jetty.util.HttpUrlUtil;
import com.google.common.collect.ImmutableMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Tests that is only valid when running in remote-mode when gcloud datastore
 * is available.
 *
 */
public class RemoteSessionIntegrationTest extends AbstractIntegrationTest {

  class SessionData {
    String id;
    String cookie;

    public SessionData(String id, String cookie) {
      this.id = id;
      this.cookie = cookie;
    }
  }

  Datastore datastore;
  KeyFactory keyFactory;
  ObjectMapper objectMapper;

  @Before
  public void setUp() throws Exception {
    datastore = DatastoreOptions.newBuilder()
        .setProjectId(System.getProperty("app.deploy.project"))
        .build()
        .getService();
    keyFactory = datastore.newKeyFactory().setKind("GCloudSession");
    objectMapper = new ObjectMapper();
  }

  @Test
  @RemoteOnly
  public void testLifeCycle() throws IOException {
    SessionData session = createSession(null);

    Entity entity = datastore.get(keyFactory.newKey("_0.0.0.0_" + session.id));
    assertNotNull(entity);

    //invalidate it
    String sessionCookie = session.cookie.replaceFirst("(\\W)(P|p)ath=", "$1\\$Path=");
    URI target = getUri().resolve("/session/?a=delete");

    HttpURLConnection http = HttpUrlUtil.openTo(target);
    http.setRequestProperty("Cookie", sessionCookie);

    assertThat(http.getResponseCode(), is(200));

    String responseBody = HttpUrlUtil.getResponseBody(http);
    assertNotNull(responseBody);
    assertThat(responseBody, containsString("invalidated"));

    entity = datastore.get(keyFactory.newKey("_0.0.0.0_" + session.id));
    assertNull(entity);
  }

  @Test
  @RemoteOnly
  public void testSessionDeserialization() throws IOException {

    Map<String, String> sessionAttrs = ImmutableMap.of(
        "key1", "val1",
        "rand", Double.toString(Math.random())
    );
    SessionData session = createSession(sessionAttrs);

    URI target = getUri().resolve("/session?a=dump");
    HttpURLConnection http = HttpUrlUtil.openTo(target);
    http.setRequestProperty("Cookie", session.cookie);
    String responseBody = HttpUrlUtil.getResponseBody(http);

    // assert that the response body is the same as the session we serialized earlier
    Map<String, String> deserializedAttrs = objectMapper.readValue(responseBody,
        new TypeReference<Map<String, String>>(){});
    assertEquals(sessionAttrs.keySet().size(), deserializedAttrs.keySet().size());
    for (String key : sessionAttrs.keySet()) {
      assertTrue(deserializedAttrs.containsKey(key));
      assertEquals(sessionAttrs.get(key), deserializedAttrs.get(key));
    }
  }

  private SessionData createSession(Map<String, String> attributes) throws IOException {
    String uri = "/session/?a=create";
    if (attributes != null) {
      String json = objectMapper.writeValueAsString(attributes);
      uri += "&attributes=" + URLEncoder.encode(json, "UTF-8");
    }
    URI target = getUri().resolve(uri);

    assertThat(target.getPath(), containsString("/session"));

    HttpURLConnection http = HttpUrlUtil.openTo(target);
    assertThat(http.getResponseCode(), is(200));

    String responseBody = HttpUrlUtil.getResponseBody(http);
    assertThat(responseBody, containsString("SESSION: id="));
    String id = responseBody.substring(responseBody.indexOf("=") + 1);
    assertNotNull(id);
    id = id.trim();
    String sessionCookie = http.getHeaderField("Set-Cookie");
    assertThat(sessionCookie, containsString("JSESSIONID"));
    assertThat(sessionCookie, containsString(id));
    return new SessionData(id, sessionCookie);
  }

}
