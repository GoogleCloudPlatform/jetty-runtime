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

package com.google.cloud.runtime.jetty.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HttpUrlUtil {
  private static final Logger log = Logger.getLogger(HttpUrlUtil.class.getName());

  /**
   * Wait for a server to be "up" by requesting a specific GET resource
   * that should be returned in status code 200.
   * <p>
   * This will attempt a check for server up.
   * If any result other then response code 200 occurs, then
   * a 2s delay is performed until the next test.
   * Up to the duration/timeunit specified.
   * </p>
   *
   * @param uri      the URI to request
   * @param duration the time duration to wait for server up
   * @param unit     the time unit to wait for server up
   */
  public static void waitForServerUp(URI uri, int duration, TimeUnit unit) {

    log.info("Waiting for server up: " + uri);
    boolean ok = false;
    long wait = 0;
    long expiration = System.currentTimeMillis() + unit.toMillis(duration);
    while (!ok && System.currentTimeMillis() < expiration) {
      try {
        HttpURLConnection http = openTo(uri);
        int statusCode = http.getResponseCode();
        if (statusCode == HttpURLConnection.HTTP_OK) {
          ok = true;
          break;
        }
      } catch (MalformedURLException e) {
        throw new IllegalArgumentException("Invalid URI: " + uri.toString());
      } catch (IOException e) {
        log.log(Level.INFO, "Ignoring IOException: " + e);
        log.log(Level.FINEST, "Ignoring IOException", e);
      }
      try {
        wait += 500;
        log.log(Level.INFO, "Waiting " + wait + "ms for next attempt");
        TimeUnit.MILLISECONDS.sleep(wait);
      } catch (InterruptedException ignore) {
        System.err.println(ignore);
        // ignore
      }
    }

    log.info("Server up: " + ok);
  }

  /**
   * Open a new {@link HttpURLConnection} to the provided URI.
   * <p>
   * Note: will also set the 'User-Agent' to {@code jetty-runtime/gcloud-util-core}
   * </p>
   *
   * @param uri the URI to open to
   * @return the open HttpURLConnection
   * @throws IOException if unable to open the connection
   */
  public static HttpURLConnection openTo(URI uri) throws IOException {
    log.info("HttpUrlUtil.openTo(" + uri + ")");
    HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
    http.setRequestProperty("User-Agent", "jetty-runtime/gcloud-util-core");
    return http;
  }

  /**
   * Obtain the text (non-binary) response body from an {@link HttpURLConnection},
   * using the response provided charset.
   * <p>
   * Note: Normal HttpURLConnection doesn't use the provided charset properly.
   * </p>
   *
   * @param http the {@link HttpURLConnection} to obtain the response body from
   * @return the text of the response body
   * @throws IOException if unable to get the text of the response body
   */
  public static String getResponseBody(HttpURLConnection http) throws IOException {
    Charset responseEncoding = StandardCharsets.UTF_8;
    if (http.getContentEncoding() != null) {
      responseEncoding = Charset.forName(http.getContentEncoding());
    }

    return IOUtils.toString(http.getInputStream(), responseEncoding);
  }
}
