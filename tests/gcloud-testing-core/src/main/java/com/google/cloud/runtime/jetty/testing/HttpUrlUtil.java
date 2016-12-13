package com.google.cloud.runtime.jetty.testing;

import org.eclipse.jetty.toolchain.test.IO;
import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
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
    System.err.println("Waiting for server up: " + uri);
    boolean waiting = true;
    long expiration = System.currentTimeMillis() + unit.toMillis(duration);
    while (waiting && System.currentTimeMillis() < expiration) {
      try {
        System.out.print(".");
        HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
        http.setRequestProperty("User-Agent", "jetty-runtime/gcloud-testing-core(server-up)");
        int statusCode = http.getResponseCode();
        if (statusCode != HttpURLConnection.HTTP_OK) {
          log.log(Level.FINER, "Waiting 2s for next attempt");
          TimeUnit.SECONDS.sleep(2);
        } else {
          waiting = false;
        }
      } catch (MalformedURLException e) {
        Assert.fail("Invalid URI: " + uri.toString());
      } catch (IOException e) {
        log.log(Level.FINEST, "Ignoring IOException", e);
      } catch (InterruptedException ignore) {
        // ignore
      }
    }
    System.err.println();
    System.err.println("Server seems to be up.");
  }

  /**
   * Open a new {@link HttpURLConnection} to the provided URI.
   * <p>
   * Note: will also set the 'User-Agent' to {@code jetty-runtime/gcloud-testing-core}
   * </p>
   *
   * @param uri the URI to open to
   * @return the open HttpURLConnection
   * @throws IOException if unable to open the connection
   */
  public static HttpURLConnection openTo(URI uri) throws IOException {
    log.info("HttpUrlUtil.openTo(" + uri + ")");
    HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
    http.setRequestProperty("User-Agent", "jetty-runtime/gcloud-testing-core");
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

    try (InputStream in = http.getInputStream();
        InputStreamReader reader = new InputStreamReader(in, responseEncoding);
        StringWriter writer = new StringWriter()) {
      IO.copy(reader, writer);
      return writer.toString();
    }
  }
}
