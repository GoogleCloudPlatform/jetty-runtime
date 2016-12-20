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

import com.google.cloud.runtime.jetty.test.annotation.LocalOnly;
import com.google.cloud.runtime.jetty.test.annotation.RemoteOnly;

import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * Custom test runner that provides support for restricting test cases based on the execution
 * environment.
 *
 * @Test is used to flag a method as a test case, these test cases will in either execution mode.
 * @RemoteOnly suppresses execution local mode.
 * @LocalOnly suppresses execution in remote mode.
 */
public class LocalRemoteTestRunner extends BlockJUnit4ClassRunner {
  private boolean localTestsEnabled = false;
  private boolean remoteTestsEnabled = false;
  private String mode;


  public LocalRemoteTestRunner(Class<?> klass) throws InitializationError {
    super(klass);

    mode = System.getProperty("test.mode");

    this.localTestsEnabled = "local".equals(mode);
    this.remoteTestsEnabled = "remote".equals(mode);

    if (!localTestsEnabled && !remoteTestsEnabled) {
      throw new InitializationError("either local or remote testing must be enabled");
    }
  }

  @Override
  protected void runChild(FrameworkMethod method, RunNotifier notifier) {
    Description description = describeChild(method);

    // check Ignore first
    if (method.getAnnotation(Ignore.class) != null) {
      notify("@Ignore", description);
      notifier.fireTestIgnored(description);

    } else if (localTestsEnabled && method.getAnnotation(RemoteOnly.class) != null) {

      // if running in local mode and @RemoteOnly annotation exists, ignore
      notify("Skip @RemoteOnly", description);
      notifier.fireTestIgnored(description);
    } else if (remoteTestsEnabled && method.getAnnotation(LocalOnly.class) != null) {

      // if running in remote mode and @LocalOnly annotation exists, ignore
      notify("Skip @LocalOnly", description);
      notifier.fireTestIgnored(description);
    } else {
      // default is run in either mode
      notify("Test[" + mode + "]", description);
      super.runChild(method, notifier);
    }
  }

  private void notify(String msg, Description description) {
    System.err.printf("[LocalRemoteTestRunner] %s %s.%s()%n", msg, description.getClassName(),
        description.getMethodName());
  }
}
