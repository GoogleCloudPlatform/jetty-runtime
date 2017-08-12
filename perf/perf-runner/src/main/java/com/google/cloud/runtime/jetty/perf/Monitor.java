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

public class Monitor {
  public static Start start() {
    return new Start();
  }

  private static class Base {
    public final int cores = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
    public final long time = System.nanoTime();
    //public final ZonedDateTime date = ZonedDateTime.now();
    public final long jitTime = ManagementFactory.getCompilationMXBean().getTotalCompilationTime();
    public final long cpuTime = CpuTime.get();
  }

  public static class Start extends Base {
    private Start() {
    }

    public Stop stop() {
      return new Stop(this);
    }
  }

  public static class Stop extends Base {
    public final Start start;
    public final long deltaTime;
    public final long deltaJitTime;
    public final long deltaCpuTime;
    public final double cpuPercent;

    private Stop(Start start) {
      this.start = start;
      this.deltaTime = time - start.time;
      this.deltaJitTime = jitTime - start.jitTime;
      this.deltaCpuTime = cpuTime - start.cpuTime;
      this.cpuPercent = deltaTime == 0 ? //
          Double.NaN : (double) deltaCpuTime * 100 / deltaTime / cores;
    }
  }
}
