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

import org.mortbay.jetty.load.generator.LoadGenerator;
import org.mortbay.jetty.load.generator.Resource;
import org.mortbay.jetty.load.generator.listeners.CollectorInformations;
import org.mortbay.jetty.load.generator.listeners.QpsListenerDisplay;
import org.mortbay.jetty.load.generator.listeners.RequestQueuedListenerDisplay;
import org.mortbay.jetty.load.generator.listeners.report.GlobalSummaryListener;
import org.mortbay.jetty.load.generator.starter.LoadGeneratorStarter;
import org.mortbay.jetty.load.generator.starter.LoadGeneratorStarterArgs;

import java.net.InetAddress;
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
    LOGGER.info( "start load test" );
    runner.run();
    LOGGER.info( "load test done" );

    CollectorInformations collectorInformations = runner.getResponseTimeSummary();

    long totalRequestCommitted = collectorInformations.getTotalCount();
    long start = collectorInformations.getStartTimeStamp();
    long end = collectorInformations.getEndTimeStamp();

    String hostname = "";
    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch ( Exception e ) {
      LOGGER.info( "ignore cannot get hostname:" + e.getMessage() );
    }

    LOGGER.info( "" );
    LOGGER.info( "" );
    LOGGER.info( "----------------------------------------------------");
    LOGGER.info( "-----------    Result Summary     ------------------");
    LOGGER.info( "----------------------------------------------------");
    LOGGER.info( "" + collectorInformations.toString() );
    LOGGER.info( "----------------------------------------------------");
    LOGGER.info( "" );
    LOGGER.info( "----------------------------------------------------");
    LOGGER.info( "-----------     Estimated QPS     ------------------");
    LOGGER.info( "----------------------------------------------------");
    long timeInSeconds = TimeUnit.SECONDS.convert( end - start, TimeUnit.MILLISECONDS );
    long qps = totalRequestCommitted / timeInSeconds;
    LOGGER.info( "host '" + hostname + "' estimated QPS : " + qps );
    LOGGER.info( "----------------------------------------------------");
    LOGGER.info( "" );

    // well it's only for test
    String returnExit = runnerArgs.getParams().get( "returnExit" );
    if (returnExit != null || Boolean.parseBoolean( returnExit )) {
      LOGGER.info( "returnExit" );
      return;
    }

    while ( true ) {
      Thread.sleep( 60000 );
    }


  }

  private static class LoadGeneratorRunner extends LoadGeneratorStarter {

    private GlobalSummaryListener globalSummaryListener = new GlobalSummaryListener();

    private QpsListenerDisplay qpsListenerDisplay = //
        // FIXME those values need to be configurable!! //
        new QpsListenerDisplay(10, 30, TimeUnit.SECONDS);

    private RequestQueuedListenerDisplay requestQueuedListenerDisplay = //
        // FIXME those values need to be configurable!! //
        new RequestQueuedListenerDisplay(10, 30, TimeUnit.SECONDS);

    public LoadGeneratorRunner( LoadGeneratorStarterArgs runnerArgs ) {
      super( runnerArgs );
    }

    @Override
    protected Request.Listener[] getListeners() {

      return new Request.Listener[]{qpsListenerDisplay, requestQueuedListenerDisplay};
    }

    @Override
    protected LoadGenerator.Listener[] getLoadGeneratorListeners() {
      return new LoadGenerator.Listener[]{qpsListenerDisplay, requestQueuedListenerDisplay};
    }

    @Override
    protected Resource.Listener[] getResourceListeners() {
      return new Resource.Listener[]{globalSummaryListener};
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
