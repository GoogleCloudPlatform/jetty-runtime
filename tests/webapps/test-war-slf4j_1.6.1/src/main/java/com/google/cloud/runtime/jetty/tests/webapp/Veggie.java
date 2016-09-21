package com.google.cloud.runtime.jetty.tests.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Example class that uses jakarta commons-logging api
 */
public class Veggie implements Runnable
{
    private static final Log LOG = LogFactory.getLog(Veggie.class);
    
    public void run()
    {
        LOG.debug(LogInit.VERSION + " some commons-logging debug");
        LOG.info(LogInit.VERSION + " some commons-logging info");
        LOG.warn(LogInit.VERSION + " some commons-logging warning");
        LOG.error(LogInit.VERSION + " some commons-logging error", new RuntimeException("Gone Bad"));
    }
}
