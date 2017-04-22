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

package com.google.cloud.runtime.jetty.perf;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class RunConfigHandler
    extends HttpServlet{
  final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  final PerfRunner perfRunner;

  public RunConfigHandler( PerfRunner perfRunner ) {
    this.perfRunner = perfRunner;
  }

  @Override
  protected void doGet( HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException {
    objectMapper.writeValue( response.getOutputStream(), this.perfRunner.runnerArgs );
  }

}
