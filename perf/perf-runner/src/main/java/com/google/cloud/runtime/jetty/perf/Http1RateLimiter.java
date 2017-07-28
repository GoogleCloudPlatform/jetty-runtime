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
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.client.SendFailure;
import org.eclipse.jetty.client.api.Connection;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.client.http.HttpConnectionOverHTTP;

import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.util.Promise;

import org.mortbay.jetty.load.generator.HTTPClientTransportBuilder;

/**
 * Helper builder to provide an http(s). {@link HttpClientTransport}
 */
public class Http1RateLimiter implements HTTPClientTransportBuilder {
  private int selectors = 1;
  private int resourceRate;

  public Http1RateLimiter( int resourceRate ) {
    this.resourceRate = resourceRate;
  }

  public Http1RateLimiter selectors( int selectors ) {
    this.selectors = selectors;
    return this;
  }

  public int getSelectors() {
    return selectors;
  }

  @Override
  public HttpClientTransport build() {
    RateLimiter rateLimiter = RateLimiter.create( resourceRate );
    return new TransportOverHttpRateLimiter( getSelectors(), rateLimiter );
  }

  private static class TransportOverHttpRateLimiter extends HttpClientTransportOverHTTP {
    private final RateLimiter rateLimiter;

    public TransportOverHttpRateLimiter( int selectors, RateLimiter rateLimiter ) {
      super( selectors );
      this.rateLimiter = rateLimiter;
    }

    @Override
    protected HttpConnectionOverHTTP newHttpConnection( EndPoint endPoint,
                                                        HttpDestination destination,
                                                        Promise<Connection> promise ) {
      return new HttpConnectionOverHttpRateLimiter( endPoint, destination, promise, rateLimiter );
    }
  }


  private static class HttpConnectionOverHttpRateLimiter extends HttpConnectionOverHTTP {
    private final RateLimiter rateLimiter;

    public HttpConnectionOverHttpRateLimiter( EndPoint endPoint,
                                              HttpDestination destination,
                                              Promise<Connection> promise,
                                              RateLimiter rateLimiter ) {
      super( endPoint, destination, promise );
      this.rateLimiter = rateLimiter;
    }

    @Override
    public void send( Request request, Response.CompleteListener listener ) {
      rateLimiter.acquire();
      super.send( request, listener );
    }

    @Override
    protected SendFailure send( HttpExchange exchange ) {
      double time = rateLimiter.acquire();
      return super.send( exchange );
    }
  }
}
