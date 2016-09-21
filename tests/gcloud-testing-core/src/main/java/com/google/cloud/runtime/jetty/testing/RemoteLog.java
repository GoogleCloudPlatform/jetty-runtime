package com.google.cloud.runtime.jetty.testing;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class RemoteLog {
  public static class Entry {
    String logName;
    String timeStamp;
    String textPayload;

    public String getTextPayload() {
      return textPayload;
    }

    public String getLogName() {
      return logName;
    }

    public String getTimeStamp() {
      return timeStamp;
    }
  }

  /**
   * Get the remote logs for the associated module and version.
   *
   * @param moduleId the module name
   * @param versionId the deployed version
   * @return the list of log entries
   * @throws IOException if unable to fetch remote log entries
   */
  public static List<Entry> getLogs(String moduleId, String versionId) throws IOException {
    String filter = String
        .format("resource.labels.module_id=\"%s\" AND resource.labels.version_id=\"%s\"", moduleId,
            versionId);

    List<Entry> entries = new ArrayList<>();
    Path logPath = MavenTestingUtils.getTargetPath("remote-module-version.log");
    try (OutputStream output = Files.newOutputStream(logPath, StandardOpenOption.CREATE)) {
      int retval = ProcessUtil.exec(output, "gcloud", "beta", "logging", "read", filter);

      if (retval != 0) {
        return entries;
      }
    } catch (InterruptedException | ExecutionException e) {
      throw new IOException("Unable to process gcloud beta logging read", e);
    }

    try (InputStream in = Files.newInputStream(logPath, StandardOpenOption.READ)) {
      Yaml yaml = new Yaml();
      for (Object yamlObj : yaml.loadAll(in)) {
        Map yamlMap = (Map) yamlObj;
        Entry entry = new Entry();
        entry.logName = (String) yamlMap.get("logName");
        entry.textPayload = (String) yamlMap.get("textPayload");
        entry.timeStamp = (String) yamlMap.get("timestamp");
        entries.add(entry);
      }
    }
    return entries;
  }

  /**
   * Ensure that the list of log entries has the expected entries.
   *
   * <p>
   *   Each expected entry is tested against the list of log entries
   *   via a {@link String#contains(CharSequence)} check.
   * </p>
   *
   * <p>
   *   Missing entries are reported back in the Assertion failure
   * </p>
   *
   * @param logs the list of log entries
   * @param expectedEntries the expected entries
   */
  public static void assertHasEntries(List<Entry> logs, List<String> expectedEntries) {
    assertThat("Logs", logs, notNullValue());
    assertThat("Logs.size", logs.size(), greaterThan(0));
    assertThat("Expected Entries", expectedEntries, notNullValue());
    assertThat("Expected Entries.size", expectedEntries.size(), greaterThan(0));
    List<String> expected = new ArrayList<>(expectedEntries);
    for (Entry entry : logs) {
      if (expected.isEmpty()) {
        return; // all good!
      }
      ListIterator<String> expectedIter = expected.listIterator();
      while (expectedIter.hasNext()) {
        String expectedText = expectedIter.next();
        if (entry.textPayload.contains(expectedText)) {
          expectedIter.remove();
        }
      }
    }

    if (expected.size() > 0) {
      StringBuilder err = new StringBuilder();
      err.append("Missing ").append(expected.size()).append(" expected entries");
      for (String expectedEntry : expected) {
        err.append(System.lineSeparator());
        err.append(expectedEntry);
      }
      assertThat(err.toString(), expected.size(), is(0));
    }
  }

  /**
   * Find a specific log entry conforming to the {@link String#contains(CharSequence)} of
   * the provided text.
   *
   * @param logs the list of entries to check in
   * @param text the text to search for
   * @return the first occurrence of text, or null if not found
   */
  public static Entry findEntry(List<Entry> logs, String text) {
    for (Entry entry : logs) {
      if (entry.textPayload.contains(text)) {
        return entry;
      }
    }

    return null;
  }
}
