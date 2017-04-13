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
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.mortbay.jetty.load.generator.LoadGenerator;
import org.mortbay.jetty.load.generator.Resource;
import org.mortbay.jetty.load.generator.listeners.CollectorInformations;
import org.mortbay.jetty.load.generator.listeners.QpsListenerDisplay;
import org.mortbay.jetty.load.generator.listeners.RequestQueuedListenerDisplay;
import org.mortbay.jetty.load.generator.listeners.Utils;
import org.mortbay.jetty.load.generator.listeners.latency.LatencyTimeDisplayListener;
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
    ensureNetwork(runnerArgs,20);

    LoadGeneratorRunner runner = new LoadGeneratorRunner( runnerArgs );
    String hostname = "";
    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch ( Exception e ) {
      LOGGER.info( "ignore cannot get hostname:" + e.getMessage() );
    }
    runner.host = hostname;
    LOGGER.info( "start load test" );
    runner.run();
    LOGGER.info( "load test done" );

    CollectorInformations latencyTimeSummary = runner.getLatencyTimeSummary();

    long totalRequestCommitted = latencyTimeSummary.getTotalCount();
    long start = latencyTimeSummary.getStartTimeStamp();
    long end = latencyTimeSummary.getEndTimeStamp();

    LOGGER.info( "" );
    LOGGER.info( "" );
    LOGGER.info( "----------------------------------------------------");
    LOGGER.info( "--------    Latency Time Summary     ---------------");
    LOGGER.info( "----------------------------------------------------");
    LOGGER.info( "" + latencyTimeSummary.toString() );
    LOGGER.info( "----------------------------------------------------");
    LOGGER.info( "" );
    LOGGER.info( "----------------------------------------------------");
    LOGGER.info( "-----------     Estimated QPS     ------------------");
    LOGGER.info( "----------------------------------------------------");
    long timeInSeconds = TimeUnit.SECONDS.convert( end - start, TimeUnit.MILLISECONDS );
    long qps = totalRequestCommitted / timeInSeconds;
    LOGGER.info( "host '" + hostname + "' estimated QPS : " + qps );
    LOGGER.info( "----------------------------------------------------");
    LOGGER.info( "perfmetric:max_latency:"
                     + fromNanostoMillis(latencyTimeSummary.getMaxValue()));
    LOGGER.info( "perfmetric:min_latency:"
                     + fromNanostoMillis(latencyTimeSummary.getMinValue()));
    LOGGER.info( "perfmetric:ave_latency:"
                     + fromNanostoMillis(Math.round(latencyTimeSummary.getMean())));
    LOGGER.info( "perfmetric:50_latency:"
                     + fromNanostoMillis(latencyTimeSummary.getValue50()));
    LOGGER.info( "perfmetric:90_latency:"
                     + fromNanostoMillis(latencyTimeSummary.getValue90()));
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


  private static long fromNanostoMillis(long nanosValue) {
    return TimeUnit.MILLISECONDS.convert( nanosValue, TimeUnit.NANOSECONDS );
  }

  private static class LoadGeneratorRunner extends LoadGeneratorStarter {

    private String host;
    private GlobalSummaryListener globalSummaryListener = new GlobalSummaryListener();

    private QpsListenerDisplay qpsListenerDisplay = //
        // FIXME those values need to be configurable!! //
        new QpsListenerDisplay(10, 10, TimeUnit.SECONDS);

    private RequestQueuedListenerDisplay requestQueuedListenerDisplay = //
        // FIXME those values need to be configurable!! //
        new RequestQueuedListenerDisplay(10, 10, TimeUnit.SECONDS);

    private LatencyTimeDisplayListener latencyTimeDisplayListener =
        new LatencyTimeDisplayListener( 10, 10, TimeUnit.SECONDS );

    public LoadGeneratorRunner( LoadGeneratorStarterArgs runnerArgs ) {
      super( runnerArgs );
      this.latencyTimeDisplayListener.addValueListener( histogram -> {
        LOGGER.info( "host '" + host );
        LOGGER.info( "----------------------------------------------------");
        LOGGER.info( "perfmetric_run:total:" + histogram.getTotalCount());
        LOGGER.info( "perfmetric_run:max_latency:"
                         + fromNanostoMillis( histogram.getMaxValue()) );
        LOGGER.info( "perfmetric_run:min_latency:"
                         + fromNanostoMillis( histogram.getMinValue()) );
        LOGGER.info( "perfmetric_run:ave_latency:"
                         + fromNanostoMillis( Math.round( histogram.getMean())) );
        LOGGER.info( "perfmetric_run:50_latency:"
                         + fromNanostoMillis( histogram.getValueAtPercentile( 50 )));
        LOGGER.info( "perfmetric_run:90_latency:"
                         + fromNanostoMillis( histogram.getValueAtPercentile( 90 )));
        LOGGER.info( "----------------------------------------------------");
      } );
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
      return new Resource.Listener[]{globalSummaryListener, latencyTimeDisplayListener };
    }

    public CollectorInformations getLatencyTimeSummary() {
      return new CollectorInformations( globalSummaryListener.getLatencyTimeHistogram() //
                                            .getIntervalHistogram() );
    }
  }

  public static void ensureNetwork(LoadGeneratorStarterArgs runnerArgs, int trynumber)
      throws Exception {
    // we do one request until we are sure the network is here
    // olamy: for some reasons the target network can be unreachable
    int connectTry = 0;
    while (connectTry < trynumber) {
      HttpClient httpClient = new HttpClient( new SslContextFactory( true ) );
      try {
        httpClient.start();
        httpClient //
            .newRequest( runnerArgs.getHost(), runnerArgs.getPort() ) //
            .scheme( runnerArgs.getScheme() ) //
            .path( "/" ) //
            .send();
        LOGGER.info( "connect to target host ok" );
        return;
      } catch ( Exception e ) {
        LOGGER.warn("cannot query target:" + Utils.toString( e ) );
        if (httpClient != null) {
          httpClient.stop();
        }
        throw e;
      }
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
