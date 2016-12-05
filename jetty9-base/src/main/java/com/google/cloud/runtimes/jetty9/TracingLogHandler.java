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

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.cloud.MonitoredResource;
import com.google.cloud.logging.AsyncLoggingHandler;
import com.google.cloud.logging.LogEntry.Builder;
import com.google.cloud.logging.LoggingOptions;

import org.eclipse.jetty.server.Request;

import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * A Google Cloud Logging Handler extended with a request traceid label.
 */
public class TracingLogHandler extends AsyncLoggingHandler {

  private final ThreadLocal<Boolean> flushing = new ThreadLocal<>();
  private final MonitoredResource monitored;
  private final String instanceid;
  
  /**
   * Construct a TracingLogHandler for "jetty.log"
   */
  public TracingLogHandler() {
    this(
        firstNonNull(
            LogManager.getLogManager().getProperty(TracingLogHandler.class.getName() + ".log"),
            "jetty.log"),
        null,
        MonitoredResource.newBuilder("gae_app")
            .addLabel("project_id", System.getenv("GCLOUD_PROJECT"))
            .addLabel("module_id", System.getenv("GAE_SERVICE"))
            .addLabel("version_id", System.getenv("GAE_VERSION")).build());
  }

  /**
   * Construct a TracingLogHandler.
   * 
   * @param logName Name of the log
   * @param options LoggingOptions to access Google cloud API
   * @param resource The resource to log against
   */
  public TracingLogHandler(String logName, LoggingOptions options, MonitoredResource resource) {
    super(logName, options, resource);
    monitored = resource;
    instanceid = System.getenv("GAE_INSTANCE");
  }

  @Override
  public synchronized void publish(LogRecord record) {
    // check we are not already flushing logs
    if (!Boolean.TRUE.equals(flushing.get())) {
      super.publish(record);
    }
  }

  @Override
  protected void enhanceLogEntry(Builder builder, LogRecord record) {
    super.enhanceLogEntry(builder, record);
    String traceid = RequestContextScope.getCurrentTraceid();
    builder.setResource(monitored);
    if (traceid != null) {
      builder.addLabel("appengine.googleapis.com/trace_id", traceid);
    } else {
      Request request = RequestContextScope.getCurrentRequest();
      if (request != null) {
        builder.addLabel("http-scheme", request.getScheme());
        builder.addLabel("http-method", request.getMethod());
        builder.addLabel("http-uri", request.getOriginalURI());
        builder.addLabel("http-remote-addr", request.getRemoteAddr());
      }
    }
    if (instanceid != null) {
      builder.addLabel("appengine.googleapis.com/instance_name", instanceid);
    }
  }

  @Override
  public synchronized void flush() {
    try {
      flushing.set(Boolean.TRUE);
      super.flush();
    } finally {
      flushing.set(Boolean.FALSE);
    }
  }
}
