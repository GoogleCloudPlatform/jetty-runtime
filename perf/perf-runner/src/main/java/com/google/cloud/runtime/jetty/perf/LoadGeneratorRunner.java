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
import org.mortbay.jetty.load.generator.LoadGenerator;
import org.mortbay.jetty.load.generator.Resource;
import org.mortbay.jetty.load.generator.listeners.CollectorInformations;
import org.mortbay.jetty.load.generator.listeners.QpsListenerDisplay;
import org.mortbay.jetty.load.generator.listeners.RequestQueuedListenerDisplay;
import org.mortbay.jetty.load.generator.listeners.latency.LatencyTimeDisplayListener;
import org.mortbay.jetty.load.generator.listeners.report.GlobalSummaryListener;
import org.mortbay.jetty.load.generator.starter.LoadGeneratorStarter;
import org.mortbay.jetty.load.generator.starter.LoadGeneratorStarterArgs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class LoadGeneratorRunner extends LoadGeneratorStarter {

  private static final Logger LOGGER = Log.getLogger( LoadGeneratorRunner.class );
  String host;

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

  public LoadGeneratorRunner( LoadGeneratorStarterArgs runnerArgs,
                              ExecutorService executorService, PerfRunner perfRunner ) {
    super( runnerArgs );
    this.executorService = executorService;
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

      long timeInSeconds =
          TimeUnit.SECONDS.convert( histogram.getEndTimeStamp() //
                                      - histogram.getStartTimeStamp(), //
                                  TimeUnit.MILLISECONDS );
      long qps = histogram.getTotalCount() / timeInSeconds;
      synchronized ( perfRunner.runStatus ) {
        perfRunner.runStatus = //
          new PerfRunner.RunStatus( perfRunner.runId, //
                                    histogram.getTotalCount(), //
                                      fromNanostoMillis( histogram.getMaxValue() ), //
                                      fromNanostoMillis( histogram.getMinValue() ), //
                                      fromNanostoMillis( Math.round( histogram.getMean() ) ), //
                                      fromNanostoMillis( histogram.getValueAtPercentile( 50 ) ), //
                                      fromNanostoMillis( histogram.getValueAtPercentile( 90 ) ), //
                                      PerfRunner.Eta.RUNNING, //
                                      qps ) //
                .startDate( perfRunner.runStartDate );
      }
      perfRunner.bigQueryRecord( perfRunner.runStatus );
    });
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

  public GlobalSummaryListener getGlobalSummaryListener() {
    return globalSummaryListener;
  }

  @Override
  public ExecutorService getExecutorService() {
    return this.executorService;
  }
}
