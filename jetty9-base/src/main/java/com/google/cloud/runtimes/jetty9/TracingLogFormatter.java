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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class TracingLogFormatter extends Formatter {

  private final String format;
  private final Date date = new Date();

  /**
   * Construct TracingLogFormater.
   * Only called from TracingLogHandler
   * @param format The format to log
   */
  TracingLogFormatter(String format) {
    this.format = format != null 
      ? format 
      : "%1$tc:%4$s:%2$s: %5$s traceId=%7$s %6$s%n";
  }

  @Override
  public synchronized String format(LogRecord record) {
    date.setTime(record.getMillis());
    String source;
    if (record.getSourceClassName() != null) {
      source = record.getSourceClassName();
      if (record.getSourceMethodName() != null) {
        source += " " + record.getSourceMethodName();
      }
    } else {
      source = record.getLoggerName();
    }

    // format the message
    String message = formatMessage(record);
    
    // format any associated Throwable.
    String throwable = "";
    if (record.getThrown() != null) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      pw.println();
      record.getThrown().printStackTrace(pw);
      // TODO Should the thrown cause and/or suppressed exceptions also be logged?
      pw.close();
      throwable = sw.toString();
    }
    return String.format(
        format, 
        /* %1 */ date, 
        /* %2 */ source, 
        /* %3 */ record.getLoggerName(), 
        /* %4 */ record.getLevel().toString(),
        /* %5 */ message, 
        /* %6 */ throwable, 
        /* %7 */ TracingLogHandler.getCurrentTraceId());
  }
}
