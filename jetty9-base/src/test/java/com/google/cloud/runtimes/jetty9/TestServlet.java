//
// ========================================================================
// Copyright (c) 1995-2016 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
//
// The Eclipse Public License is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
//
// You may elect to redistribute this code under either of these licenses.
// ========================================================================
//

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

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    log.info("doGet info");
    getServletContext().log("doGet ServletContext.log");

    if (request.getParameter("ex") != null) {
      try {
        throw (Throwable) Class.forName(request.getParameter("ex")).newInstance();
      } catch (ServletException | IOException e) {
        throw e;
      } catch (Throwable e) {
        throw new ServletException(e);
      }
    }
    response.getWriter().println("Log Test");
  }
}
