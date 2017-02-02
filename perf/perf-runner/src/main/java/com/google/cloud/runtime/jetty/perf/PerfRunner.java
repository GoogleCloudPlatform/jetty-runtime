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

import com.beust.jcommander.JCommander;
import org.mortbay.jetty.load.generator.CollectorInformations;
import org.mortbay.jetty.load.generator.latency.LatencyTimeListener;
import org.mortbay.jetty.load.generator.report.GlobalSummaryListener;
import org.mortbay.jetty.load.generator.report.SummaryReport;
import org.mortbay.jetty.load.generator.responsetime.ResponseTimeListener;
import org.mortbay.jetty.load.generator.starter.LoadGeneratorStarter;
import org.mortbay.jetty.load.generator.starter.LoadGeneratorStarterArgs;

import java.util.Arrays;

/**
 * Runner performance testing
 */
public class PerfRunner {
  public static void main(String[] args) throws Exception {
    System.out.println( "args:" + Arrays.asList(args) );

    LoadGeneratorStarterArgs runnerArgs = new LoadGeneratorStarterArgs();

    try {
      JCommander jcommander = new JCommander( runnerArgs, args );
      if ( runnerArgs.isHelp() ) {
        jcommander.usage();
        return;
      }
    }
    catch ( Exception e ) {
      e.printStackTrace();
      new JCommander( runnerArgs ).usage();
      return;
    }

    LoadGeneratorRunner runner = new LoadGeneratorRunner( runnerArgs );

    String warmupNumberArg = runnerArgs.getParams().get( "warmup.number" );
    int warmupNumber = 5;
    try {
      warmupNumber = Integer.parseInt( warmupNumberArg );
    } catch ( NumberFormatException e ) {
      // ignore and use default
      System.out.println( "error parsing warmup number arg '" + warmupNumberArg
                              + "' so use default " + warmupNumber  );
    }
    if ( warmupNumber>0 ) {
      runner.run( warmupNumber );
      System.out.println( "warmup done" );
    } else {
      System.out.println( "NO warmup" );
    }
    // reset the global recorder
    runner.globalSummaryListener = new GlobalSummaryListener();


    if (runnerArgs.getRunningTime() > 0 && runnerArgs.getRunningTimeUnit() != null) {
      runner.run( runnerArgs.getRunningTime(), runnerArgs.getRunningTimeUnit(), false );
    }  else {
      runner.run( runnerArgs.getRunIteration(), false );
    }

    CollectorInformations collectorInformations = runner.getResponseTimeSummary();

    System.out.println(  );
    System.out.println(  );
    System.out.println( "----------------------------------------------------");
    System.out.println( "-----------    Result Summary     ------------------");
    System.out.println( "----------------------------------------------------");
    System.out.println( "" + collectorInformations.toString() );
    System.out.println( "----------------------------------------------------");
    System.out.println(  );

    System.exit( 0 );
    return;
  }

  private static class LoadGeneratorRunner extends LoadGeneratorStarter {

    private GlobalSummaryListener globalSummaryListener = new GlobalSummaryListener();

    public LoadGeneratorRunner( LoadGeneratorStarterArgs runnerArgs ) {
      super( runnerArgs );
    }

    @Override
    public ResponseTimeListener[] getResponseTimeListeners() {
      return new ResponseTimeListener[]{globalSummaryListener};
    }

    @Override
    public LatencyTimeListener[] getLatencyTimeListeners() {
      return new LatencyTimeListener[0];
    }

    public CollectorInformations getResponseTimeSummary() {
      return new CollectorInformations( globalSummaryListener.getResponseTimeHistogram() );
    }

  }

}
