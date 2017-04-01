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

package com.google.cloud.runtimes.jetty.integration;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/exception"})
public class ExceptionServlet extends HttpServlet {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  static class ExceptionRequest {
    public String token;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    ExceptionRequest exceptionRequest
        = objectMapper.readValue(req.getReader(), ExceptionRequest.class);

    // Print an exception stack trace containing the provided token. This should be picked up by
    // Stackdriver exception monitoring.
    new RuntimeException(String.format(
        "Sample runtime exception for integration test. Token is %s", exceptionRequest.token))
        .printStackTrace();
  }

}
