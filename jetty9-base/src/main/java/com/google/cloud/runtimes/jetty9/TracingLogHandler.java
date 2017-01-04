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

import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * A Logging Handler extended with a request traceId label.
 * The traceId is passed in the LogRecord as an additional 
 * message format parameter.  This must be stripped by the
 * formatter before formatting the message, as is done by
 * the {@link TracingLogFormatter}.
 * @see TracingLogFormatter
 */
public class TracingLogHandler extends ConsoleHandler {

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
  
  /**
   * Construct a TracingLogHandler.
   */
  public TracingLogHandler() {
    configure();
  }

  // Private method to configure a StreamHandler from LogManager
  // properties and/or default values as specified in the class
  // javadoc.
  private void configure() {
    LogManager manager = LogManager.getLogManager();
    String cname = getClass().getName();

    String formatter = manager.getProperty(cname + ".formatter");
    if (formatter != null && formatter.equals(TracingLogFormatter.class.getName())) {
      throw new IllegalStateException("Use String 'format' rather than Class 'fomatter' for "
          + TracingLogFormatter.class.getName());
    }

    String format = manager.getProperty(cname + ".format");
    setFormatter(new TracingLogFormatter(format));    
  }

  @Override
  public synchronized void publish(LogRecord record) {
    String traceId = getCurrentTraceId();
    if (traceId != null) {
      // Encode the traceId as an additional message format 
      // parameter for the LogRecord.  This should be removed by
      // the formatter before formatting the message and used to
      // format the LogRecord itself
      Object[] params = record.getParameters();
      if (params == null) {
        record.setParameters(new Object[] {new TraceId(traceId)});
      } else {
        params = Arrays.copyOf(params,params.length + 1);
        params[params.length - 1] = new TraceId(traceId);
        record.setParameters(params);
      }
    }
    super.publish(record);
  }
  
  /**
   * Class to identify and hold the traceId in the
   * LogRecord parameters array.
   */
  public static class TraceId {
    private final String traceId;

    public TraceId(String traceId) {
      this.traceId = traceId;
    }

    public String toString() {
      return traceId;
    }
  }

}
