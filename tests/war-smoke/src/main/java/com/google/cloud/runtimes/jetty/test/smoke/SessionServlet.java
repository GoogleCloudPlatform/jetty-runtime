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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(urlPatterns = {"/session/*"})
public class SessionServlet extends HttpServlet {

  private ObjectMapper objectMapper;

  public SessionServlet() {
    objectMapper = new ObjectMapper();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/plain");

    String action = req.getParameter("a");
    action = action.trim();
    if (action == null) {
      resp.getWriter().println("ERR: no action");
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    } else if ("create".equalsIgnoreCase(action)) {
      HttpSession session = req.getSession(true);
      // persist arbitrary session attributes
      String attributes = req.getParameter("attributes");
      if (attributes != null) {
        Map<String, String> attributeMap = objectMapper.readValue(attributes,
            new TypeReference<HashMap<String,String>>(){});
        for (Entry<String, String> e : attributeMap.entrySet()) {
          req.getSession().setAttribute(e.getKey(), e.getValue());
        }
      }
      resp.getWriter().println("SESSION: id=" + session.getId());
    } else if ("delete".equalsIgnoreCase(action)) {
      HttpSession session = req.getSession(false);
      if (session == null) {
        resp.getWriter().println("ERR: no session");
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } else {
        session.invalidate();
        resp.getWriter().println("SESSION: invalidated");
      }
    } else if ("change".equalsIgnoreCase(action)) {
      HttpSession session = req.getSession(false);
      if (session == null) {
        resp.getWriter().println("ERR: no session");
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } else {
        String newId = req.changeSessionId();
        resp.getWriter().println("SESSION: id=" + newId);
      }
    } else if ("dump".equalsIgnoreCase(action)) {
      HttpSession session = req.getSession(false);
      if (session == null) {
        resp.getWriter().println("ERR: no session");
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } else {
        Enumeration<String> attributeNames = session.getAttributeNames();
        Map<String, String> attrsMap = new HashMap<>();
        while (attributeNames.hasMoreElements()) {
          String attributeName = attributeNames.nextElement();
          attrsMap.put(attributeName, (String) session.getAttribute(attributeName));
        }
        objectMapper.writeValue(resp.getWriter(), attrsMap);
      }

    } else {
      resp.getWriter().println("ERR: unrecognized action");
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

  }


}
