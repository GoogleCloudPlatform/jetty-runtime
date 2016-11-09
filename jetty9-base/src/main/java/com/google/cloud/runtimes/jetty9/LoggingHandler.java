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

import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LoggingHandler extends Handler {

  @Override
  public void publish(LogRecord record) {
    String traceid = RequestContextScope.getCurrentTraceid();
    System.err.printf("%s:%s:%s:%s:%s%n", new Date(record.getMillis()), traceid, record.getLevel(),
        record.getLoggerName(), record.getMessage());
  }

  @Override
  public void flush() {}

  @Override
  public void close() throws SecurityException {}

}
