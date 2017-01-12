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

package com.google.cloud.runtimes.jetty9;


import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.cloud.MonitoredResource;
import com.google.cloud.logging.AsyncLoggingHandler;
import com.google.cloud.logging.LogEntry.Builder;
import com.google.cloud.logging.LoggingOptions;

import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * A Google Cloud Logging Handler extended with a request traceid label.
 */
public class TracingLogHandler extends AsyncLoggingHandler {

  private static final ThreadLocal<String> traceId = new ThreadLocal<>();
  
  /**
   * Set the Trace ID associated with any logging done by 
   * the current thread.
   * @param id The traceID
   */
  public static void setCurrentTraceId(String id) {
    traceId.set(id);
  }

  /**
   * Get the Trace ID associated with any logging done by 
   * the current thread.
   * @return id The traceID
   */
  public static String getCurrentTraceId() {
    return traceId.get();
  }
  
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
        addLabel(addLabel(
            addLabel(MonitoredResource.newBuilder("gae_app"), 
                "project_id", "GCLOUD_PROJECT"),
                "module_id", "GAE_SERVICE"), 
                "version_id", "GAE_VERSION").build());
  }

  private static MonitoredResource.Builder addLabel(
      MonitoredResource.Builder builder, 
      String label,
      String env) {
    String value = System.getenv(env);
    if (value != null) {
      builder.addLabel(label, value);
    }
    return builder;
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
  protected void enhanceLogEntry(Builder builder, LogRecord record) {
    super.enhanceLogEntry(builder, record);
    String traceId = getCurrentTraceId();
    builder.setResource(monitored);
    if (traceId != null) {
      builder.addLabel("appengine.googleapis.com/trace_id", traceId);
    } 
    if (instanceid != null) {
      builder.addLabel("appengine.googleapis.com/instance_name", instanceid);
    }
  }
}

