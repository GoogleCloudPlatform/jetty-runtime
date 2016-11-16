/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.runtimes.jetty9;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandler.Context;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestContextScope implements ContextHandler.ContextScopeListener {
  static final Logger logger = Logger.getLogger(RequestContextScope.class.getName());

  private static final String X_CLOUD_TRACE = "x-cloud-trace-context";
  private static final ThreadLocal<String> traceid = new ThreadLocal<>();
  private static final ThreadLocal<Deque<Request>> requestStack = 
      new ThreadLocal<Deque<Request>>() {
    @Override
    protected Deque<Request> initialValue() {
      return new ArrayDeque<>();
    }
  };

  @Override
  public void enterScope(Context context, Request request, Object reason) {
    if (request != null) {
      Deque<Request> stack = requestStack.get();
      if (stack.isEmpty()) {
        String id = (String) request.getAttribute(X_CLOUD_TRACE);
        if (id == null) {
          id = request.getHeader(X_CLOUD_TRACE);
          if (id != null) {
            int slash = id.indexOf('/');
            if (slash >= 0) {
              id = id.substring(0, slash);
            }
            request.setAttribute(X_CLOUD_TRACE, id);
          }
        }
        traceid.set(id);
      }
      stack.push(request);
    }
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("enterScope " + context);
    }
  }

  @Override
  public void exitScope(Context context, Request request) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("exitScope " + context);
    }
    if (request != null) {
      requestStack.get().pop();
    }
  }

  /** Run a Runnable in the scope of a traceid.
   * @param traceid The trace ID
   * @param runnable the runnable
   */
  public static void runWith(String traceid, Runnable runnable) {
    String original = RequestContextScope.traceid.get();
    RequestContextScope.traceid.set(traceid);
    try {
      runnable.run();
    } finally {
      RequestContextScope.traceid.set(original);
    }
  }
  
  public static Request getCurrentRequest() {
    return requestStack.get().peek();
  }

  public static String getCurrentTraceid() {
    return traceid.get();
  }
}
