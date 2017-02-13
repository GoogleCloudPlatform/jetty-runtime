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

package com.google.cloud.runtimes.jetty.test.smoke;

import com.google.cloud.Page;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.Logging.EntryListOption;
import com.google.cloud.logging.LoggingOptions;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet to query the Stackdriver logging for the gae_app smoke module.
 * An ID string is passed as the pathInfo and is used to filter the textPayload
 * and all log records found are included in the text response.
 */
@WebServlet(urlPatterns = {"/log/*"})
public class LogServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/plain");

    LoggingOptions options = LoggingOptions.getDefaultInstance();
    
    String filter = "resource.type=gae_app" + " AND resource.labels.module_id=smoke";
    String id = req.getPathInfo();
    if (id.length() > 1 && id.startsWith("/")) {
      id = id.substring(1);
      filter += " AND textPayload:" + id;
    }

    PrintWriter out = resp.getWriter();
    out.println("Log Entries " + filter + ":");
    try (Logging logging = options.getService()) {
      Page<LogEntry> entries = logging.listLogEntries(EntryListOption.filter(filter));
      Iterator<LogEntry> entryIterator = entries.iterateAll();
      while (entryIterator.hasNext()) {
        out.println(entryIterator.next());
      }
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
  }
}