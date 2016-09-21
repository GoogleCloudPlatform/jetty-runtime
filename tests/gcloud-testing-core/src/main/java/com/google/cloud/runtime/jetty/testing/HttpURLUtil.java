package com.google.cloud.runtime.jetty.testing;

import org.eclipse.jetty.toolchain.test.IO;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class HttpURLUtil {
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
    System.err.println("HttpURLUtil.openTo(" + uri + ")");
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
