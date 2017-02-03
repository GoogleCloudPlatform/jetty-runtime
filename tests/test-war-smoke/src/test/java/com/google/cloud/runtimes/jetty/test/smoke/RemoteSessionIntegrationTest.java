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
import static org.junit.Assert.assertNull;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.runtime.jetty.test.AbstractIntegrationTest;
import com.google.cloud.runtime.jetty.test.annotation.RemoteOnly;
import com.google.cloud.runtime.jetty.util.HttpUrlUtil;

import org.hamcrest.core.IsNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;


/**
 * Tests that is only valid when running in remote-mode when gcloud datastore
 * is available.
 *
 */
public class RemoteSessionIntegrationTest extends AbstractIntegrationTest {

  Datastore datastore;
  KeyFactory keyFactory;

  @Before
  public void setUp() throws Exception {
    datastore = DatastoreOptions.getDefaultInstance().getService();
    keyFactory = datastore.newKeyFactory().setKind("GCloudSession");
  }
  
  
  @After
  public void tearDown() throws Exception {
  }
  
  
  @Test
  @RemoteOnly
  public void testLifeCycle() throws IOException {

    //create it
    URI target = getUri().resolve("/session/?a=create");

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
 
    Entity entity = datastore.get(keyFactory.newKey("_0.0.0.0_" + id));
    assertNotNull(entity);
    
    
    //invalidate it
    sessionCookie = sessionCookie.replaceFirst("(\\W)(P|p)ath=", "$1\\$Path=");
    target = getUri().resolve("/session/?a=delete");
    
    http = HttpUrlUtil.openTo(target);
    http.setRequestProperty("Cookie", sessionCookie);
    
    assertThat(http.getResponseCode(), is(200));

    responseBody = HttpUrlUtil.getResponseBody(http);
    assertNotNull(responseBody);
    assertThat(responseBody, containsString("invalidated"));
    
    entity = datastore.get(keyFactory.newKey("_0.0.0.0_" + id));
    assertNull(entity);
  }
}
