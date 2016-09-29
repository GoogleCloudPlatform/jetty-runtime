package com.google.cloud.runtime.jetty.testing;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class RemoteLogTest {
  @Test
  @Ignore("This is module, and version specific (only useful for limited local testing)")
  public void testGetLogs() throws IOException {
    List<RemoteLog.Entry> logs = RemoteLog.getLogs("javautillogging", "20160914-143225");
    assertThat("Log Entries", logs.size(), greaterThan(1000));

    for (RemoteLog.Entry entry : logs) {
      System.out.printf("%s: %s%n", entry.timeStamp, entry.textPayload);
    }
  }
}
