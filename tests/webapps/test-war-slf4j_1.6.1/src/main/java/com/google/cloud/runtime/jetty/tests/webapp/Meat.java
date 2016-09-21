package com.google.cloud.runtime.jetty.tests.webapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example class that uses slf4j api
 */
public class Meat implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger(Meat.class);
    
    public void run()
    {
        LOG.debug(LogInit.VERSION + " some slf4j debug");
        LOG.info(LogInit.VERSION + " some slf4j info");
        LOG.warn(LogInit.VERSION + " some slf4j warning");
        LOG.error(LogInit.VERSION + " some slf4j error", new RuntimeException("Gone Bad"));
    }
}
