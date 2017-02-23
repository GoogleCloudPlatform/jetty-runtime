package com.google.cloud.runtimes.jetty.runtimes_common_test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

public class LoggingServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // TODO test stackdriver logging.
    // see https://github.com/GoogleCloudPlatform/runtimes-common/tree/master/integration_tests#logging

  }

}
