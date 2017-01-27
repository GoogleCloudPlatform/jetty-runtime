//
//  ========================================================================
//  Copyright (c) 1995-2016 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package com.webtide.jetty.load.generator.web;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
@WebServlet( "/upload" )
public class UploadServlet
    extends HttpServlet
{

    private static final Logger LOGGER = LoggerFactory.getLogger( UploadServlet.class );

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
        throws ServletException, IOException
    {
        String answer = "GET does nothing";
        LOGGER.info( answer );
        resp.getWriter().print( answer );
    }

    @Override
    protected void doPut( HttpServletRequest req, HttpServletResponse resp )
        throws ServletException, IOException
    {
        //Path tmp = Files.createTempFile( "loadgenerator", ".jetty" );
        //try (OutputStream outputStream = Files.newOutputStream( tmp ))
        //{
        IOUtils.copy( req.getInputStream(), resp.getOutputStream() );
        //}

    }
}
