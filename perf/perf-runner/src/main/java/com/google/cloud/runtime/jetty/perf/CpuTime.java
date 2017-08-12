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

package com.google.cloud.runtime.jetty.perf;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class CpuTime {

  private static final Provider provider;

  static {
    Provider temp;
    try {
      Class<?> klass = Class.forName("java.lang.ProcessHandle");
      temp = new Jdk9Provider( klass, klass.getMethod( "current").invoke( null));
    } catch (Throwable x) {
      temp = new Jdk8Provider();
    }
    provider = temp;
  }

  public static long get() {
    return provider.getCpuTime().orElse( Duration.ZERO).toNanos();
  }

  private interface Provider {
    Optional<Duration> getCpuTime();
  }

  private static class Jdk9Provider implements Provider {
    private final Class<?> klass;
    private final Object process;

    public Jdk9Provider( Class<?> klass, Object process) {
      this.klass = klass;
      this.process = process;
    }

    @Override
    public Optional<Duration> getCpuTime() {
      try {
        Method method = klass.getMethod("info");
        Object info = method.invoke(process);
        return (Optional<Duration>)method.getReturnType() //
            .getMethod("totalCpuDuration").invoke(info);
      } catch (Throwable x) {
        return Optional.empty();
      }
    }
  }

  private static class Jdk8Provider implements Provider {
    @Override
    public Optional<Duration> getCpuTime() {
      return Optional.of(ManagementFactory.getOperatingSystemMXBean()) //
                     .filter(os -> os instanceof com.sun.management.OperatingSystemMXBean) //
                     .map(os -> (com.sun.management.OperatingSystemMXBean)os) //
                     .map(os -> Duration.of(os.getProcessCpuTime(), ChronoUnit.NANOS));
    }
  }
}
