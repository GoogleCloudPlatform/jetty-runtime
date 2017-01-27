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

package com.webtide.jetty.load.generator.web;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Simple servlet upload.
 */
@WebServlet( "/upload" )
public class UploadServlet extends HttpServlet {

  private static final Logger LOGGER = LoggerFactory.getLogger( UploadServlet.class );

  @Override
  protected void doGet( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, IOException {
    String answer = "GET does nothing";
    LOGGER.info( answer );
    resp.getWriter().print( answer );
  }

  @Override
  protected void doPut( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, IOException {
    IOUtils.copy( req.getInputStream(), resp.getOutputStream() );
  }
}
