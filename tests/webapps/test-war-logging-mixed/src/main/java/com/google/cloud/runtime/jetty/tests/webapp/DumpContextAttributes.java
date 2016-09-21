package com.google.cloud.runtime.jetty.tests.webapp;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Component generating log4j events
 */
@WebServlet(urlPatterns = {"/attr/context/*"})
public class DumpContextAttributes extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(DumpContextAttributes.class);

  @SuppressWarnings("Duplicates")
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Do something that generates a multiline logging event
    StringBuilder result = new StringBuilder();

    ServletContext context = request.getServletContext();
    Collection<String> attributeNames = Collections.list(context.getAttributeNames());

    result.append("# Context Attributes: ").append(attributeNames.size()).append(" entries");
    result.append(System.lineSeparator());

    Map<String, Map<String, String>> map = new HashMap<>();
    attributeNames.stream().sorted(String.CASE_INSENSITIVE_ORDER).forEach((attributeName) -> {
      Object attribute = context.getAttribute(attributeName);
      Map<String, String> types = new HashMap<>();
      if (attribute != null) {
        types.put("class", attribute.getClass().getName());
        types.put("value", attribute.toString());
      } else {
        types.put("class", "<null>");
      }
      map.put(attributeName, types);
    });

    Yaml yaml = new Yaml();
    result.append(yaml.dumpAsMap(map));

    // Create logging event
    LOG.info(result.toString());

    // Send something back to the client
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("text/plain");
    response.getWriter().print(result.toString());
  }
}
