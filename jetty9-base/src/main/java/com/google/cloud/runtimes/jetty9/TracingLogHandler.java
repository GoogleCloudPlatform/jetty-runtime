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
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.LogEntry.Builder;
import com.google.cloud.logging.LoggingOptions;

import java.util.logging.LogManager;
import java.util.logging.LogRecord;

public class TracingLogHandler extends AsyncLoggingHandler {

  public TracingLogHandler() {
    this(firstNonNull(
        LogManager.getLogManager().getProperty(TracingLogHandler.class.getName() + ".log"),
        "jetty.log"), null, null);
  }

  public TracingLogHandler(String logName, LoggingOptions options, MonitoredResource resource) {
    super(logName, options, resource);
  }

  @Override
  protected LogEntry buildEntryFor(LogRecord record, Builder builder) {
    String traceid = RequestContextScope.getCurrentTraceid();
    if (traceid != null) {
      builder.addLabel("traceid", traceid);
    }
    return super.buildEntryFor(record, builder);
  }
}
