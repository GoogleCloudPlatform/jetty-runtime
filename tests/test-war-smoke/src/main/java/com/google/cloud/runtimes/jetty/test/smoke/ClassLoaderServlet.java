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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Test that classes are correctly hidden from webapp.
 */
@WebServlet(urlPatterns = {"/classloader"})
public class ClassLoaderServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/plain");

    String[] hidden = {
        "com.google.cloud.logging.Logging.class", 
        "org.eclipse.jetty.server.Server",
        "com.google.cloud.BaseService", 
        "io.netty.util.Constant.class"
        };

    int notFound = 0;
    int found = 0;

    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    for (String clazz : hidden) {
      try {
        loader.loadClass(clazz);
        resp.getWriter().printf("Context loaded %s%n", clazz);
        found++;
      } catch (ClassNotFoundException e) {
        resp.getWriter().printf("Context Not Found %s%n", clazz);
        notFound++;
      }
    }

    resp.getWriter().printf("Found classes = %s (0 expected)%n", found);
    resp.getWriter().printf("Not found classes = %s (%d expected)%n", notFound, hidden.length);
  }
}
