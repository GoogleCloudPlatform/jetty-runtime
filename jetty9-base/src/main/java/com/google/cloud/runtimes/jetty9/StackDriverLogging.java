package com.google.cloud.runtimes.jetty9;

import java.util.Arrays;
import java.util.logging.Logger;

public class StackDriverLogging {

  static void init() throws Exception {
    Logger log = Logger.getLogger(StackDriverLogging.class.getName());

    for (Logger l : new Logger[] {log,
        Logger.getLogger("sun.net.www.protocol.http.HttpURLConnection")}) {
      while (l != null) {
        System.err.printf("Log '%s' upl=%b %s%n", l.getName(), l.getUseParentHandlers(),
            Arrays.asList(l.getHandlers()));
        l = l.getParent();
      }
    }
    System.err.println("httpurl log");
    Logger.getLogger("sun.net.www.protocol.http.HttpURLConnection").info("test");
    System.err.println("some.random.log log");
    Logger.getLogger("some.random.log").info("test");
    System.err.println("com.google.cloud.runtimes.jetty9.StackDriverLogging log info");
    log.info("test info");
    System.err.println("com.google.cloud.runtimes.jetty9.StackDriverLogging log fine");
    log.fine("test fine");
    System.err.println("done");
  }

  public static void main(String... args) throws Exception {
    init();
  }
}
