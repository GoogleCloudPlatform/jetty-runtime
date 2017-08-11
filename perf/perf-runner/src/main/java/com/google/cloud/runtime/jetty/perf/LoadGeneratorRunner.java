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

import static com.google.cloud.runtime.jetty.perf.PerfRunner.fromNanostoMillis;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.mortbay.jetty.load.generator.HTTP1ClientTransportBuilder;
import org.mortbay.jetty.load.generator.HTTP2ClientTransportBuilder;
import org.mortbay.jetty.load.generator.HTTPClientTransportBuilder;
import org.mortbay.jetty.load.generator.LoadGenerator;
import org.mortbay.jetty.load.generator.Resource;
import org.mortbay.jetty.load.generator.listeners.CollectorInformations;
import org.mortbay.jetty.load.generator.listeners.QpsListenerDisplay;
import org.mortbay.jetty.load.generator.listeners.RequestQueuedListenerDisplay;
import org.mortbay.jetty.load.generator.listeners.latency.LatencyTimeDisplayListener;
import org.mortbay.jetty.load.generator.listeners.report.GlobalSummaryListener;
import org.mortbay.jetty.load.generator.starter.LoadGeneratorStarter;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class LoadGeneratorRunner extends LoadGeneratorStarter {

  private static final Logger LOGGER = Log.getLogger( LoadGeneratorRunner.class );
  String host;
  PerfRunner.LoadGeneratorStarterConfig runnerArgs;

  private GlobalSummaryListener globalSummaryListener = new GlobalSummaryListener() //
      .addExcludeHttpStatusFamily( 100, 300, 500 ); //

  private ExecutorService executorService;

  QpsListenerDisplay qpsListenerDisplay = //
      // FIXME those values need to be configurable!! //
      new QpsListenerDisplay( 10, 10, TimeUnit.SECONDS);

  RequestQueuedListenerDisplay requestQueuedListenerDisplay = //
      // FIXME those values need to be configurable!! //
      new RequestQueuedListenerDisplay(10, 10, TimeUnit.SECONDS);

  LatencyTimeDisplayListener latencyTimeDisplayListener =
      new LatencyTimeDisplayListener( 10, 10, TimeUnit.SECONDS )
          .addExcludeHttpStatusFamily( 100, 300, 500 ); //

  public LoadGeneratorRunner( PerfRunner.LoadGeneratorStarterConfig runnerArgs,
                              ExecutorService executorService, PerfRunner perfRunner ) {
    this.runnerArgs = runnerArgs;
    this.executorService = executorService;
    this.latencyTimeDisplayListener.addValueListener( ( latencyHistogram, totalHistogram )  -> {
      LOGGER.info( "host '" + host );
      LOGGER.info( "----------------------------------------------------");
      LOGGER.info( "perfmetric_run:total:" + latencyHistogram.getTotalCount());
      LOGGER.info( "perfmetric_run:max_latency:"
                     + fromNanostoMillis( latencyHistogram.getMaxValue()) );
      LOGGER.info( "perfmetric_run:min_latency:"
                     + fromNanostoMillis( latencyHistogram.getMinValue()) );
      LOGGER.info( "perfmetric_run:ave_latency:"
                     + fromNanostoMillis( Math.round( latencyHistogram.getMean())) );
      LOGGER.info( "perfmetric_run:50_latency:"
                     + fromNanostoMillis( latencyHistogram.getValueAtPercentile( 50 )));
      LOGGER.info( "perfmetric_run:90_latency:"
                     + fromNanostoMillis( latencyHistogram.getValueAtPercentile( 90 )));
      LOGGER.info( "----------------------------------------------------");

      long timeInSeconds =
          TimeUnit.SECONDS.convert( totalHistogram.getEndTimeStamp() //
                                      - totalHistogram.getStartTimeStamp(), //
                                  TimeUnit.MILLISECONDS );
      long qps = totalHistogram.getTotalCount() / timeInSeconds;
      synchronized ( perfRunner.runStatus ) {
        perfRunner.runStatus = //
          new PerfRunner.RunStatus( perfRunner.runId, //
                                    latencyHistogram.getTotalCount(), //
                                      fromNanostoMillis( latencyHistogram.getMaxValue() ), //
                                      fromNanostoMillis(
                                          latencyHistogram.getMinValue() ), //
                                      fromNanostoMillis(
                                          Math.round( latencyHistogram.getMean() ) ), //
                                      fromNanostoMillis(
                                          latencyHistogram.getValueAtPercentile( 50 ) ), //
                                      fromNanostoMillis(
                                          latencyHistogram.getValueAtPercentile( 90 ) ), //
                                      PerfRunner.Eta.RUNNING, //
                                      qps ) //
                .startDate( perfRunner.runStartDate );
      }
      perfRunner.bigQueryRecord( perfRunner.runStatus );
    });
  }

  protected List<Request.Listener> getListeners() {
    return Arrays.asList(qpsListenerDisplay, //
                                  requestQueuedListenerDisplay, //
                                  latencyTimeDisplayListener, //
                                  globalSummaryListener);
  }

  protected List<LoadGenerator.Listener> getLoadGeneratorListeners() {
    return Arrays.asList(qpsListenerDisplay, requestQueuedListenerDisplay);
  }

  protected List<Resource.Listener> getResourceListeners() {
    return Arrays.asList(globalSummaryListener, latencyTimeDisplayListener);
  }

  public CollectorInformations getLatencyTimeSummary() {
    return new CollectorInformations( globalSummaryListener.getLatencyTimeHistogram() //
                                          .getIntervalHistogram() );
  }

  public GlobalSummaryListener getGlobalSummaryListener() {
    return globalSummaryListener;
  }

  public QpsListenerDisplay getQpsListenerDisplay() {
    return qpsListenerDisplay;
  }

  public ExecutorService getExecutorService() {
    return this.executorService;
  }

  public HTTPClientTransportBuilder getHttpClientTransportBuilder() {
    boolean useRateLimiter = Boolean.parseBoolean( runnerArgs.getParams().get( "useRateLimiter" ) );
    int transactionRate = runnerArgs.getResourceRate();
    switch ( runnerArgs.getTransport() ) {
      case HTTP:
      case HTTPS: {
        if ( transactionRate > 1 && useRateLimiter ) {
          LOGGER.info( "use RateLimiter" );
          return new Http1RateLimiter( transactionRate ) //
              .selectors( runnerArgs.getSelectors() );
        } else {
          LOGGER.info( "NOT use RateLimiter" );
          return new HTTP1ClientTransportBuilder().selectors( runnerArgs.getSelectors() );
        }
      }
      case H2C:
      case H2: {
        if ( transactionRate > 1 && useRateLimiter ) {
          LOGGER.info( "use RateLimiter" );
          return new Http2RateLimiter( transactionRate ) //
              .selectors( runnerArgs.getSelectors() );
        } else {
          LOGGER.info( "NOT use RateLimiter" );
          return new HTTP2ClientTransportBuilder().selectors( runnerArgs.getSelectors() );
        }
      } default: {
        // nothing this weird case already handled by #provideClientTransport
      }

    }
    throw new IllegalArgumentException( "unknown getHttpClientTransportBuilder" );
  }
}
