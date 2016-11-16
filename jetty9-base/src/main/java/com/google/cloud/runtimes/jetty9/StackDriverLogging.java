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
    Logger.getLogger("").info("root info!");
    log.info("test info!");
    log.fine("test fine!");

    ((TracingLogHandler) Logger.getLogger("").getHandlers()[0]).flush();
    System.err.println("flushed");

    Thread.sleep(5000);
  }

  public static void main(String... args) throws Exception {
    init();
  }
}
