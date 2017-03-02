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
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.mortbay.jetty.load.generator.CollectorInformations;
import org.mortbay.jetty.load.generator.LoadGenerator;
import org.mortbay.jetty.load.generator.QpsListenerDisplay;
import org.mortbay.jetty.load.generator.latency.LatencyTimeListener;
import org.mortbay.jetty.load.generator.report.GlobalSummaryListener;
import org.mortbay.jetty.load.generator.responsetime.ResponseTimeListener;
import org.mortbay.jetty.load.generator.starter.LoadGeneratorStarter;
import org.mortbay.jetty.load.generator.starter.LoadGeneratorStarterArgs;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Runner performance testing
 */
public class PerfRunner {

  private static final Logger LOGGER = Log.getLogger( PerfRunner.class );  
  
  public static void main(String[] args) throws Exception {
    
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

    getValuesFromEnvVar( runnerArgs );
    LOGGER.info( "runnerArgs:" + runnerArgs.toString() );
    LoadGeneratorRunner runner = new LoadGeneratorRunner( runnerArgs );

    String warmupNumberArg = runnerArgs.getParams().get( "warmup.number" );
    int warmupNumber = 5;
    try {
      warmupNumber = Integer.parseInt( warmupNumberArg );
    } catch ( NumberFormatException e ) {
      // ignore and use default
      LOGGER.info( "error parsing warmup number arg '" + warmupNumberArg
                              + "' so use default " + warmupNumber  );
    }
    LOGGER.info( "warmup number {}", warmupNumber );
    if ( warmupNumber > 0 ) {
      runner.run( warmupNumber, false );
      LOGGER.info( "warmup done" );
    } else {
      LOGGER.info( "NO warmup" );
    }
    // reset the global recorder
    runner.globalSummaryListener = new GlobalSummaryListener();


    if (runnerArgs.getRunningTime() > 0 && runnerArgs.getRunningTimeUnit() != null) {
      LOGGER.info( "starting load for {} {}", //
                   runnerArgs.getRunningTime(), //
                   runnerArgs.getRunningTimeUnit());
      runner.run( runnerArgs.getRunningTime(), runnerArgs.getRunningTimeUnit(), true );
    }  else {
      LOGGER.info( "starting load for {} iterations", runnerArgs.getRunIteration());
      runner.run( runnerArgs.getRunIteration(), true );
    }

    LOGGER.info( "load test done" );

    CollectorInformations collectorInformations = runner.getResponseTimeSummary();
    LOGGER.info( "" );
    LOGGER.info( "" );
    LOGGER.info( "----------------------------------------------------");
    LOGGER.info( "-----------    Result Summary     ------------------");
    LOGGER.info( "----------------------------------------------------");
    LOGGER.info( "" + collectorInformations.toString() );
    LOGGER.info( "----------------------------------------------------");
    LOGGER.info( "" );

    // well it's only for test
    String noSysExit = runnerArgs.getParams().get( "noSysExit" );
    if (noSysExit == null || !Boolean.parseBoolean( noSysExit )) {
      LOGGER.info( "System.exit(0)" );
      System.exit( 0 );
    }
    LOGGER.info( "no System.exit" );
    return;
  }

  private static class LoadGeneratorRunner extends LoadGeneratorStarter {

    private GlobalSummaryListener globalSummaryListener = new GlobalSummaryListener();

    public LoadGeneratorRunner( LoadGeneratorStarterArgs runnerArgs ) {
      super( runnerArgs );
    }

    @Override
    protected Request.Listener[] getListeners() {
      // FIXME those values need to be configurable!!
      return new Request.Listener[]{new QpsListenerDisplay( 5, 30, TimeUnit.SECONDS)};
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
      return new CollectorInformations( globalSummaryListener.getResponseTimeHistogram() //
                                            .getIntervalHistogram() );
    }



  }

  /**
   * as we can get values from envvar with Docker using -e
   * @param runnerArgs the args to enhance
   */
  private static void getValuesFromEnvVar(LoadGeneratorStarterArgs runnerArgs) {
    Map<String,String> env = System.getenv();
    String host = env.get( "PERF_HOST" );
    if (host != null ) {
      runnerArgs.setHost(host);
    }

    String port = env.get( "PERF_PORT" );
    if (port != null ) {
      runnerArgs.setPort(Integer.parseInt( port ));
    }

    String users = env.get( "PERF_USERS" );
    if (users != null ) {
      runnerArgs.setUsers(Integer.parseInt( users ));
    }

    String transactionRate = env.get( "PERF_TRANSACTION_RATE" );
    if (transactionRate != null ) {
      runnerArgs.setTransactionRate(Integer.parseInt( transactionRate ));
    }

    String transport = env.get( "PERF_TRANSPORT" );
    if (transport != null ) {
      runnerArgs.setTransport(transport);
    }

    String runningTime = env.get( "PERF_RUNNING_TIME" );
    if (runningTime != null ) {
      runnerArgs.setRunningTime(Integer.parseInt( runningTime ));
    }

    String runningTimeUnit = env.get( "PERF_RUNNING_TIME_UNIT" );
    if (runningTimeUnit != null ) {
      runnerArgs.setRunningTimeUnit(runningTimeUnit);
    }

    String runIteration = env.get( "PERF_RUN_ITERATION" );
    if (runIteration != null ) {
      runnerArgs.setRunIteration(Integer.parseInt( runIteration ));
    }

  }

}
