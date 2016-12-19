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

package com.google.cloud.runtime.jetty.util;

import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * Accessible AppDeployment information constants.
 * <p>
 * Will pull information from the system environment and also the
 * active project's app.yaml in order to build the server deployed
 * URI and variables.
 * </p>
 */
public final class AppDeployment {
  public static final String PROJECT_ID;
  public static final String VERSION_ID;
  public static final String SERVICE_ID;
  public static final URI SERVER_URI;

  static {
    String projectId = System.getProperty("app.deploy.project");
    String version = System.getProperty("app.deploy.version");

    Objects.requireNonNull(projectId, "app.deploy.project");
    Objects.requireNonNull(version, "app.deploy.version");

    PROJECT_ID = projectId;
    VERSION_ID = version;

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

    SERVICE_ID = serviceId;

    StringBuilder uri = new StringBuilder();
    uri.append("https://");
    uri.append(version).append("-dot-");
    if (serviceId != null) {
      uri.append(serviceId).append("-dot-");
    }
    uri.append(projectId);
    uri.append(".appspot.com/");

    SERVER_URI = URI.create(uri.toString());
  }
}
