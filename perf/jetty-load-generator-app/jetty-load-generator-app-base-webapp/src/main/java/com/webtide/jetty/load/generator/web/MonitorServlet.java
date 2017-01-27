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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.eclipse.jetty.toolchain.perf.PlatformMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */

public class MonitorServlet
    extends HttpServlet
{

    private static final Logger LOG = LoggerFactory.getLogger( MonitorServlet.class );

    private final PlatformMonitor platformMonitor = new PlatformMonitor();

    private PlatformMonitor.Start start;

    private PlatformMonitor.Stop stop;

    boolean _restrictToLocalhost = true; // defaults to true


    @Override
    public void init()
        throws ServletException
    {
        if ( getInitParameter( "restrictToLocalhost" ) != null )
        {
            _restrictToLocalhost = "true".equals( getInitParameter( "restrictToLocalhost" ) );
        }
    }

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
        throws ServletException, IOException
    {

        if ( _restrictToLocalhost )
        {
            if ( !isLoopbackAddress( req.getRemoteAddr() ) )
            {
                resp.sendError( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
                return;
            }
        }

        if ( Boolean.valueOf( req.getParameter( "start" ) ) )
        {
            start = platformMonitor.start();
            return;
        }

        if ( Boolean.valueOf( req.getParameter( "stop" ) ) )
        {
            stop = platformMonitor.stop();
            return;
        }

        if ( Boolean.valueOf( req.getParameter( "stats" ) ) )
        {
            if ( start == null )
            {
                resp.sendError( HttpServletResponse.SC_BAD_REQUEST );
                return;
            }

            if ( stop == null )
            {
                stop = platformMonitor.stop();
            }
            sendResponse( resp );
        }
    }


    protected void sendResponse( HttpServletResponse response )
        throws IOException
    {
        //start.
        //stop.

        try
        {
            Map<String, Object> run = new LinkedHashMap<>();
            Map<String, Object> config = new LinkedHashMap<>();
            run.put( "config", config );
            config.put( "cores", start.cores );
            config.put( "totalMemory", new Result( start.gibiBytes( start.totalMemory ), "GiB" ) );
            config.put( "os", start.os );
            config.put( "jvm", start.jvm );
            config.put( "totalHeap", new Result( start.gibiBytes( start.heap.getMax() ), "GiB" ) );
            config.put( "date", new Date( start.date ).toString() );
            Map<String, Object> results = new LinkedHashMap<>();
            run.put( "results", results );
            results.put( "cpu", new Result( stop.percent( stop.cpuTime, stop.time ) / start.cores, "%" ) );
            results.put( "jitTime", new Result( stop.jitTime, "ms" ) );
            Map<String, Object> gc = new LinkedHashMap<>();
            results.put( "gc", gc );
            gc.put( "youngCount", stop.youngCount );
            gc.put( "youngTime", new Result( stop.youngTime, "ms" ) );
            gc.put( "oldCount", stop.oldCount );
            gc.put( "oldTime", new Result( stop.oldTime, "ms" ) );
            gc.put( "youngGarbage", new Result( stop.mebiBytes( stop.edenBytes + stop.survivorBytes ), "MiB" ) );
            gc.put( "oldGarbage", new Result( stop.mebiBytes( stop.tenuredBytes ), "MiB" ) );

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable( SerializationFeature.INDENT_OUTPUT );
            mapper.writeValue( response.getOutputStream(), run );
        }
        catch ( Exception e )
        {
            e.printStackTrace( response.getWriter() );
            throw new IOException( e.getMessage(), e );
        }

    }


    private boolean isLoopbackAddress( String address )
    {
        try
        {
            InetAddress addr = InetAddress.getByName( address );
            return addr.isLoopbackAddress();
        }
        catch ( UnknownHostException e )
        {
            LOG.warn( "Warning: attempt to access statistics servlet from " + address, e );
            return false;
        }
    }


    private static class Result
        extends HashMap<String, Object>
    {
        public Result( Object value, String unit )
        {
            super( 2 );
            put( "value", value );
            put( "unit", unit );
        }
    }

}
