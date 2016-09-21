package com.google.cloud.runtime.jetty.tests.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Component generating commons-logging events
 */
@WebServlet(urlPatterns = {"/counting/*"})
public class HeaderNameCount extends HttpServlet {
  private static final Log LOG = LogFactory.getLog(HeaderNameCount.class);

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Do something that generates a large multiline logging event
    int[] counts = new int[128];
    Arrays.fill(counts, 0);

    Enumeration<String> headerNameIter = request.getHeaderNames();

    while (headerNameIter.hasMoreElements()) {
      String headerName = headerNameIter.nextElement();
      for (char c : headerName.toCharArray()) {
        if (c >= 32 && c <= 126) { // utf8/iso88591/ascii for characters <space> thru ~
          counts[c]++;
        }
      }
    }

    // Write multiline logging event
    StringBuilder result = new StringBuilder();
    result.append("# Header Name Character Distribution").append(System.lineSeparator());

    Map<String, Integer> map = new HashMap<>();

    for (int i = 32; i < 126; i++) {
      if (counts[i] > 0) {
        String key = Character.toString((char) i);
        Integer count = map.get(key);
        if (count == null) {
          count = new Integer(0);
        }
        count++;
        map.put(key, count);
      }
    }

    Yaml yaml = new Yaml();
    result.append(yaml.dumpAsMap(map));

    LOG.info(result.toString());

    // Send something back to the client
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("text/plain");
    response.getWriter().println(result.toString());
  }
}
