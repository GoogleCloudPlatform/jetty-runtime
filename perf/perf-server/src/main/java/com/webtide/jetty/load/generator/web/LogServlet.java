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

package com.webtide.jetty.load.generator.web;

import com.google.api.gax.core.Page;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.Logging.EntryListOption;
import com.google.cloud.logging.LoggingOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

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

  private static final Logger LOGGER = LoggerFactory.getLogger( LogServlet.class );

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/plain");

    Map<String, String[]> params = req.getParameterMap();
    // pull parameters
    String moduleId = params.get("module_id")[0];
    if (moduleId == null) {
      resp.sendError(400, "module_id parameter required");
    }

    String textPayload = params.get("text_payload")[0];
    if ( textPayload == null ) {
      resp.sendError(400, "text_payload parameter required");
    }

    LoggingOptions options = LoggingOptions.getDefaultInstance();

    String filter = "resource.type=gae_app";
    filter += " AND resource.labels.module_id=" + moduleId;
    filter += " AND textPayload:'" + textPayload + "'";
    String timestamp = req.getParameter( "timestamp" );
    if (timestamp != null) {
      filter += " AND timestamp >= '" + URLEncoder.encode(timestamp) + "'";
    }

    try (Logging logging = options.getService()) {
      LOGGER.info( "start list log entries with filter: {}", filter);
      long start = System.currentTimeMillis();
      Page<LogEntry> entries = logging.listLogEntries( EntryListOption.filter( filter));
      long end = System.currentTimeMillis();
      LOGGER.info( "time to find logs {} for filter {}", (end - start), filter);
      Iterator<LogEntry> entryIterator = entries.iterateAll().iterator();
      PrintWriter out = resp.getWriter();
      while (entryIterator.hasNext()) {
        out.println(entryIterator.next());
      }
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
  }
}