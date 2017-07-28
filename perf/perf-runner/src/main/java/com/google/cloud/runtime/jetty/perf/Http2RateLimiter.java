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

package com.google.cloud.runtime.jetty.perf;

import com.google.common.util.concurrent.RateLimiter;

import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.HttpDestination;

import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpChannelOverHTTP2;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.http2.client.http.HttpConnectionOverHTTP2;

import org.mortbay.jetty.load.generator.HTTPClientTransportBuilder;

/**
 * Helper builder to provide an http2. {@link HttpClientTransport}
 */
public class Http2RateLimiter implements HTTPClientTransportBuilder {
  private int selectors = 1;
  private int sessionRecvWindow = 16 * 1024 * 1024;
  private int streamRecvWindow = 16 * 1024 * 1024;

  private int resourceRate;

  public Http2RateLimiter( int resourceRate ) {
    this.resourceRate = resourceRate;
  }

  public Http2RateLimiter selectors( int selectors) {
    this.selectors = selectors;
    return this;
  }

  public int getSelectors() {
    return selectors;
  }

  public Http2RateLimiter sessionRecvWindow( int sessionRecvWindow) {
    this.sessionRecvWindow = sessionRecvWindow;
    return this;
  }

  public int getSessionRecvWindow() {
    return sessionRecvWindow;
  }

  public Http2RateLimiter streamRecvWindow( int streamRecvWindow) {
    this.streamRecvWindow = streamRecvWindow;
    return this;
  }

  public int getStreamRecvWindow() {
    return streamRecvWindow;
  }

  @Override
  public HttpClientTransport build() {
    HTTP2Client http2Client = new HTTP2Client();
    // Chrome uses 15 MiB session and 6 MiB stream windows.
    // Firefox uses 12 MiB session and stream windows.
    http2Client.setInitialSessionRecvWindow(getSessionRecvWindow());
    http2Client.setInitialStreamRecvWindow(getStreamRecvWindow());
    http2Client.setSelectors(getSelectors());
    final RateLimiter rateLimiter = RateLimiter.create( resourceRate );
    return new TransportOverHttpRateLimiter( http2Client, rateLimiter);
  }

  private static class TransportOverHttpRateLimiter extends HttpClientTransportOverHTTP2 {
    private final RateLimiter rateLimiter;

    public TransportOverHttpRateLimiter( HTTP2Client client, RateLimiter rateLimiter ) {
      super( client );
      this.rateLimiter = rateLimiter;
    }

    @Override
    protected HttpConnectionOverHTTP2 newHttpConnection( HttpDestination destination,
                                                         Session session ) {
      return super.newHttpConnection( destination, session );
    }
  }


  private static class HttpConnectionOverHttpRateLimiter extends HttpConnectionOverHTTP2 {
    private final RateLimiter rateLimiter;

    public HttpConnectionOverHttpRateLimiter( HttpDestination destination, Session session,
                                              RateLimiter rateLimiter ) {
      super( destination, session );
      this.rateLimiter = rateLimiter;
    }

    @Override
    protected HttpChannelOverHttpRateLimiter newHttpChannel( boolean push ) {
      return new HttpChannelOverHttpRateLimiter( getHttpDestination(), this,
                                                 getSession(), push, rateLimiter );
    }
  }

  private static class HttpChannelOverHttpRateLimiter extends HttpChannelOverHTTP2 {
    private final RateLimiter rateLimiter;

    public HttpChannelOverHttpRateLimiter( HttpDestination destination,
                                           HttpConnectionOverHTTP2 connection,
                                           Session session,
                                           boolean push,
                                           RateLimiter rateLimiter ) {
      super( destination, connection, session, push );
      this.rateLimiter = rateLimiter;
    }

    @Override
    public void send() {
      rateLimiter.acquire();
      super.send();
    }
  }
}
