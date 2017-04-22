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
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public class RunnerHandler extends HttpServlet {
  private static final Logger LOGGER = Log.getLogger( PerfRunner.class );

  final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  final PerfRunner perfRunner;

  public RunnerHandler( PerfRunner perfRunner ) {
    this.perfRunner = perfRunner;
  }

  @Override
  protected void doGet( HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException {
    if (this.perfRunner.runStatus.getEta() != PerfRunner.Eta.FINISHED) {
      LOGGER.warn( "previous run not finished" );
      response.getWriter().write( "previous run not finished" );
      response.setStatus( HttpServletResponse.SC_CONFLICT );
      return;
    }
    LOGGER.info( "restart a new run" );
    this.perfRunner.service.execute(() ->
    {
      try {
        RunnerHandler.this.perfRunner.run();
      } catch ( Exception e ) {
        LOGGER.warn( "Unable to start a new run:" + e.getMessage(), e );
      }
    }
    );
  }

  @Override
  protected void doPost( HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException {
    if (this.perfRunner.runStatus.getEta() != PerfRunner.Eta.FINISHED) {
      LOGGER.warn( "previous run not finished" );
      response.getWriter().write( "previous run not finished" );
      response.setStatus( HttpServletResponse.SC_CONFLICT );
      return;
    }
    String json = IOUtils.toString( request.getReader() );
    if (json != null && json.length() > 0) {
      PerfRunner.LoadGeneratorStarterConfig loadGeneratorStarterConfig =
          objectMapper.readValue( json, PerfRunner.LoadGeneratorStarterConfig.class );
    }
    LOGGER.info( "restart a new run: " + (json == null ? " null " : " new config ") );
  }
}
