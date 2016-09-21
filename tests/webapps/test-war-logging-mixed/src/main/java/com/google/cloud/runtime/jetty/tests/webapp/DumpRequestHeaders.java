package com.google.cloud.runtime.jetty.tests.webapp;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Component generating java.util.logging events
 */
@WebServlet(urlPatterns = {"/dump/*"})
public class DumpRequestHeaders extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(DumpRequestHeaders.class.getName());

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Do something that generates a multiline logging event
    StringBuilder result = new StringBuilder();

    // Request Line
    result.append(request.getMethod()).append(' ');
    result.append(request.getRequestURI()).append(' ');
    result.append(request.getProtocol()).append("\r\n");

    // Request Headers
    Enumeration<String> headerNames = request.getHeaderNames();

    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      result.append(headerName).append(": ");

      Enumeration<String> values = request.getHeaders(headerName);
      boolean delim = false;
      while (values.hasMoreElements()) {
        if (delim) {
          result.append(", ");
        }
        // We don't care about proper quoting of values for this example
        result.append(values.nextElement());
        delim = true;
      }
      result.append("\r\n");
    }

    // Create logging event
    LOG.info(result.toString());

    // Send something back to the client
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("text/plain");
    response.getWriter().print(result.toString());
  }
}
