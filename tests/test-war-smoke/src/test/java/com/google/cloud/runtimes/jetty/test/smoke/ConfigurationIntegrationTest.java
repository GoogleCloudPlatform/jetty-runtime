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
import static org.hamcrest.Matchers.not;

import com.google.cloud.runtime.jetty.test.AbstractIntegrationTest;
import com.google.cloud.runtime.jetty.test.annotation.RemoteOnly;
import com.google.cloud.runtime.jetty.util.HttpUrlUtil;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

public class ConfigurationIntegrationTest extends AbstractIntegrationTest {

  /**
   * Simple test validating no directory listing.
   *
   * @throws IOException test in error
   */
  @Test
  @RemoteOnly
  public void testNoDirectoryListing() throws IOException {

    URI target = getUri().resolve("/directory/");

    assertThat(target.getPath(), containsString("/directory/"));

    HttpURLConnection http = HttpUrlUtil.openTo(target);
    assertThat(http.getResponseCode(), is(403));
  }

}
