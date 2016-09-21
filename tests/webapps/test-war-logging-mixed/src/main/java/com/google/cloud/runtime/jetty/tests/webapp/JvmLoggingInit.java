package com.google.cloud.runtime.jetty.tests.webapp;

import java.io.IOException;
import java.net.URL;
import java.util.logging.LogManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class JvmLoggingInit implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    initJavaUtilLogging(sce.getServletContext(), "/WEB-INF/logging.properties");
  }

  private void initJavaUtilLogging(ServletContext servletContext, String resourceName)
  {
    try
    {
      URL url = servletContext.getResource(resourceName);
      if (url == null)
      {
        throw new RuntimeException("Unable to init java.util.logging. " + resourceName + " not found");
      }
      LogManager.getLogManager().readConfiguration(url.openStream());
    }
    catch (IOException e)
    {
      throw new RuntimeException("Unable to init java.util.logging. read failure in " + resourceName, e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    /* do nothing */
  }
}
