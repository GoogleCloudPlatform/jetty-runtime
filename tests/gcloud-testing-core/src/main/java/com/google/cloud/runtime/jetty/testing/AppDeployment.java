package com.google.cloud.runtime.jetty.testing;

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

public final class AppDeployment {
  public static final String PROJECT_ID;
  public static final String VERSION_ID;
  public static final String MODULE_ID;
  public static final URI SERVER_URI;

  static {
    String projectId = System.getProperty("app.deploy.project");
    String version = System.getProperty("app.deploy.version");

    Objects.requireNonNull(projectId, "app.deploy.project");
    Objects.requireNonNull(version, "app.deploy.version");

    PROJECT_ID = projectId;
    VERSION_ID = version;

    String moduleId = null;

    Path appYamlPath = MavenTestingUtils.getProjectFilePath("src/main/appengine/app.yaml");
    if (Files.exists(appYamlPath)) {
      try (BufferedReader reader = Files.newBufferedReader(appYamlPath, StandardCharsets.UTF_8)) {
        Yaml yaml = new Yaml();
        Map map = (Map) yaml.load(reader);
        moduleId = (String) map.get("module");
      } catch (IOException e) {
        throw new RuntimeException("Unable to parse app.yaml", e);
      }
    }

    MODULE_ID = moduleId;

    StringBuilder uri = new StringBuilder();
    uri.append("https://");
    uri.append(version).append("-dot-");
    if (moduleId != null) {
      uri.append(moduleId).append("-dot-");
    }
    uri.append(projectId);
    uri.append(".appspot.com/");

    SERVER_URI = URI.create(uri.toString());
  }
}
