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

import com.google.cloud.runtime.jetty.test.annotation.Local;
import com.google.cloud.runtime.jetty.test.annotation.Remote;

import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class ContainerRunner extends BlockJUnit4ClassRunner {
  private boolean localTestsEnabled = false;
  private boolean remoteTestsEnabled = false;

  @SuppressWarnings("javadoc")
  public ContainerRunner(Class<?> klass) throws InitializationError {
    super(klass);

    this.localTestsEnabled = isEnabled("test.local", false);
    this.remoteTestsEnabled = isEnabled("test.remote", false);

    if (localTestsEnabled && remoteTestsEnabled) {
      throw new InitializationError("local and remote tests can not be run together");
    }
  }

  private boolean isEnabled(String key, boolean def) {
    String val = System.getProperty(key);
    if (val == null) {
      // not declared
      return def;
    }

    if (val.length() == 0) {
      // declared, but with no value
      return true;
    }

    // declared, parse value
    return Boolean.parseBoolean(val);
  }

  @Override
  protected void runChild(FrameworkMethod method, RunNotifier notifier) {
    Description description = describeChild(method);

    // check Ignore first
    if (method.getAnnotation(Ignore.class) != null) {
      notify("@Ignore", description);
      notifier.fireTestIgnored(description);
      return;
    }

    if (localTestsEnabled && method.getAnnotation(Local.class) != null) {
      notify("@Local", description);
      super.runChild(method, notifier);
      return;
    }

    if (remoteTestsEnabled && method.getAnnotation(Remote.class) != null) {
      notify("@Remote", description);
      super.runChild(method, notifier);
      return;
    }

    notify("NonContainerTest", description);
    super.runChild(method, notifier);
  }

  private void notify(String msg, Description description) {
    System.err.printf("[ContainerRunner] %s %s.%s()%n", msg, description.getClassName(),
        description.getMethodName());
  }
}
