package com.google.cloud.runtime.jetty.tests.webapp;

import org.apache.log4j.Logger;

/**
 * Example class that uses log4j api
 */
public class Fruit implements Runnable
{
    private static final Logger LOG = Logger.getLogger(Fruit.class);
    
    public void run()
    {
        LOG.debug(LogInit.VERSION + " some log4j debug");
        LOG.info(LogInit.VERSION + " some log4j info");
        LOG.warn(LogInit.VERSION + " some log4j warning");
        LOG.error(LogInit.VERSION + " some log4j error", new RuntimeException("Gone Bad"));
    }
}
