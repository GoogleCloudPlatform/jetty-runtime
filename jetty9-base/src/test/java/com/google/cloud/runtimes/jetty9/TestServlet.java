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

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
@WebServlet(urlPatterns = {"/", "/test/*"}, name = "TestServlet")
public class TestServlet extends HttpServlet {
  static final Logger log = Logger.getLogger(TestServlet.class.getName());

  @PostConstruct
  private void myPostConstructMethod() {
    log.info("preconstructed");
  }

  @Override
  public void init() throws ServletException {
    log.info("init info");
    getServletContext().log("init ServletContext.log");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    log.info("doGet info");
    getServletContext().log("doGet ServletContext.log");

    if (request.getParameter("ex") != null) {
      try {
        throw (Throwable) Class.forName(request.getParameter("ex")).newInstance();
      } catch (ServletException | IOException ex) {
        throw ex;
      } catch (Throwable th) {
        throw new ServletException(th);
      }
    }
    response.getWriter().println("Log Test");
  }
}
