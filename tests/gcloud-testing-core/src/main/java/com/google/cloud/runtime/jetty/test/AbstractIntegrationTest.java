/*
 * Copyright (C) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.runtime.jetty.test;

import com.google.cloud.runtime.jetty.util.HttpUrlUtil;

import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AbstractIntegrationTest {

  private static int DEFAULT_LOCAL_TIMEOUT_SECONDS = 10;
  private static int DEFAULT_REMOTE_TIMEOUT_SECONDS = 300;
  private static int TIMEOUT_SECONDS;
  private static final URI SERVER_URI;

  static {

    if ( System.getProperty("test.local") != null ) {
      String testPort = System.getProperty("app.deploy.port");

      TIMEOUT_SECONDS = DEFAULT_LOCAL_TIMEOUT_SECONDS;
      SERVER_URI = URI.create("http://localhost:" + testPort);
      Assert.assertNotNull("local uri required", SERVER_URI);

    } else if ( System.getProperty("test.remote") != null ) {
      String projectId = System.getProperty("app.deploy.project");
      String version = System.getProperty("app.deploy.version");

      Objects.requireNonNull(projectId, "app.deploy.project");
      Objects.requireNonNull(version, "app.deploy.version");

      String serviceId = null;

      Path appYamlPath = MavenTestingUtils.getProjectFilePath("src/main/appengine/app.yaml");
      if (Files.exists(appYamlPath)) {
        try (BufferedReader reader = Files.newBufferedReader(appYamlPath, StandardCharsets.UTF_8)) {
          Yaml yaml = new Yaml();
          Map map = (Map) yaml.load(reader);
          serviceId = (String) map.get("service");
          if (serviceId == null) {
            serviceId = (String) map.get("module");
          }
        } catch (IOException e) {
          throw new RuntimeException("Unable to parse app.yaml", e);
        }
      }

      StringBuilder uri = new StringBuilder();
      uri.append("https://");
      uri.append(version).append("-dot-");
      if (serviceId != null) {
        uri.append(serviceId).append("-dot-");
      }
      uri.append(projectId);
      uri.append(".appspot.com/");

      TIMEOUT_SECONDS = DEFAULT_REMOTE_TIMEOUT_SECONDS;
      SERVER_URI = URI.create(uri.toString());
    } else {
      throw new IllegalArgumentException("test.local or test.remote property required");
    }
  }

  /**
   * Get the URI to test with.
   * @return the server URI
   */
  public URI getUri() {
    return SERVER_URI;
  }

  /**
   * Waits for the server to start which needs to be completed for unit
   * tests to run.
   *
   * @throws Exception catch-all for issues checking server startup
   */
  @BeforeClass
  public static void waitForServerUp() throws Exception {
    HttpUrlUtil.waitForServerUp(SERVER_URI, TIMEOUT_SECONDS, TimeUnit.SECONDS);
  }
}
