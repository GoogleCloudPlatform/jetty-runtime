package com.google.cloud.runtime.jetty.tests.webapp;

/**
 * WebApp specific Exception.
 * <p>
 * Here to make sure that when server centralized logging is used
 * it doesn't have a classpath / classloader choke on this kind of log.
 * </p>
 */
@SuppressWarnings("serial")
public class GoneBadException extends RuntimeException
{
    public GoneBadException(String msg)
    {
        super(msg);
    }
}
