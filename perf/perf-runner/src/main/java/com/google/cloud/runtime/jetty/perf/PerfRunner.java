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

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;

import com.eaio.uuid.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang3.math.NumberUtils;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.StatisticsServlet;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import org.mortbay.jetty.load.generator.LoadGenerator;
import org.mortbay.jetty.load.generator.listeners.CollectorInformations;
import org.mortbay.jetty.load.generator.listeners.Utils;
import org.mortbay.jetty.load.generator.starter.LoadGeneratorStarterArgs;

import java.net.InetAddress;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Runner performance testing
 */
public class PerfRunner {

  private static final Logger LOGGER = Log.getLogger( PerfRunner.class );

  volatile RunStatus runStatus = new RunStatus( Eta.NOT_STARTED );

  volatile String runId;

  //"2014-08-19 12:41:35.220000"
  // FIXME olamy not TZ???? it generate an error??
  public static final DateTimeFormatter BIG_QUERY_DATE_FORMATTER = DateTimeFormatter //
                                                .ofPattern( "YYY-MM-dd HH:mm:ss.SSSSSS" ) //
                                                .withZone( ZoneId.systemDefault() );

  public static final String BIG_QUERY_DATASETID = "jetty_runtime";

  public static final String BIG_QUERY_TABLE_NAME = "loadgenerator_run";

  Server server;
  ServerConnector connector;
  StatisticsHandler statisticsHandler = new StatisticsHandler();

  Date runStartDate;
  Date runEndDate;

  LoadGeneratorStarterConfig loadGeneratorStarterConfig;
  ExecutorService service = Executors.newFixedThreadPool( 1 );

  protected BigQuery bigQuery;

  public static void main(String[] args) throws Exception {

    LoadGeneratorStarterConfig starterConfig = new LoadGeneratorStarterConfig();

    try {
      JCommander jcommander = new JCommander( starterConfig, args );
      if ( starterConfig.isHelp() ) {
        jcommander.usage();
        return;
      }
    }
    catch ( Exception e ) {
      e.printStackTrace();
      new JCommander( starterConfig ).usage();
      return;
    }

    getValuesFromEnvVar( starterConfig );
    LOGGER.info( "loadGeneratorStarterConfig:" + starterConfig.toString() );
    PerfRunner perfRunner = new PerfRunner().runnerArgs(starterConfig);
    String jettyRun = starterConfig.getParams().get( "jettyRun" );
    if (jettyRun != null && Boolean.parseBoolean( jettyRun )) {
      perfRunner.startJetty(starterConfig);
    }
    // not network access to target host so fail fast!
    ensureNetwork(starterConfig,5);
    // is bigquery setup?
    String bigQuery = starterConfig.getParams().get( "bigQuery" );
    if (bigQuery != null && Boolean.parseBoolean( bigQuery )) {
      LOGGER.info( "enable bigQuery recording" );
      perfRunner.bigQuery = setupBigQuery();
    }
    String runOnStart = starterConfig.getParams().get( "runOnStart" );
    if (runOnStart != null && Boolean.parseBoolean( runOnStart )) {
      perfRunner.run( starterConfig );
    }
    // well it's only for test
    String returnExit = starterConfig.getParams().get( "returnExit" );
    if (returnExit != null && Boolean.parseBoolean( returnExit )) {
      LOGGER.info( "returnExit" );
      return;
    }

    while ( true ) {
      Thread.sleep( 60000 );
    }

  }

  private PerfRunner runnerArgs(LoadGeneratorStarterConfig runnerArgs) {
    this.loadGeneratorStarterConfig = runnerArgs;
    return this;
  }

  public void run(LoadGeneratorStarterConfig config) throws Exception {
    LOGGER.info( "start load test" );
    this.runStartDate = new Date();
    synchronized ( this.runStatus ) {
      this.runStatus.startDate = this.runStartDate;
      this.runStatus.eta = Eta.RUNNING;
    }
    this.runId = new UUID().toString();
    // we reuse previous resource profile
    ExecutorService executorService = Executors.newCachedThreadPool();
    try {
      LoadGeneratorRunner runner = new LoadGeneratorRunner( config, executorService, this );
      LoadGenerator.Builder builder = LoadGeneratorRunner.prepare( config );
      builder.executor( runner.getExecutorService() );
      runner.getLoadGeneratorListeners().forEach( listener -> builder.listener( listener ) );
      runner.getResourceListeners().forEach( listener -> builder.resourceListener( listener ) );
      runner.getListeners().forEach( listener -> builder.requestListener( listener ) );
      String hostname = "";
      try {
        hostname = InetAddress.getLocalHost().getHostName();
      } catch ( Exception e ) {
        LOGGER.info( "ignore cannot get hostname:" + e.getMessage() );
      }
      Instant startInstant = Instant.now();
      runner.host = hostname;
      try {
        LoadGeneratorRunner.run( builder );
        this.runEndDate = new Date();
        synchronized ( this.runStatus ) {
          this.runStatus.endDate = this.runEndDate;
          this.runStatus.eta = Eta.FINISHED;
        }
        Instant endInstant = Instant.now();
        LOGGER.info( "load test done start {} end {}", startInstant, endInstant );
      } catch ( Exception e ) {
        Instant endInstant = Instant.now();
        LOGGER.info( "fail running the load start {} end {} message: {}", //
                     startInstant, endInstant, e.getMessage() );
        e.printStackTrace();
      } finally {
        runner.latencyTimeDisplayListener.onEnd( null );
      }

      CollectorInformations latencyTimeSummary = runner.getLatencyTimeSummary();

      long totalRequestCommitted = runner.getGlobalSummaryListener().getRequestCommitTotal();
      long start = latencyTimeSummary.getStartTimeStamp();
      long end = latencyTimeSummary.getEndTimeStamp();

      LOGGER.info( "" );
      LOGGER.info( "" );
      LOGGER.info( "----------------------------------------------------" );
      LOGGER.info( "--------    Latency Time Summary     ---------------" );
      LOGGER.info( "----------------------------------------------------" );
      LOGGER.info( "" + latencyTimeSummary.toString() );
      LOGGER.info( "----------------------------------------------------" );
      LOGGER.info( "" );
      LOGGER.info( "----------------------------------------------------" );
      LOGGER.info( "-----------     Estimated QPS     ------------------" );
      LOGGER.info( "----------------------------------------------------" );
      long timeInSeconds = TimeUnit.SECONDS.convert( end - start, TimeUnit.MILLISECONDS );
      long qps = totalRequestCommitted / timeInSeconds;
      LOGGER.info( "host '" + hostname + "' estimated QPS : " + qps );
      LOGGER.info( "----------------------------------------------------" );
      LOGGER.info( "perfmetric:max_latency:" //
                       + fromNanostoMillis( latencyTimeSummary.getMaxValue() ) );
      LOGGER.info( "perfmetric:min_latency:" //
                       + fromNanostoMillis( latencyTimeSummary.getMinValue() ) );
      LOGGER.info( "perfmetric:ave_latency:" //
                       + fromNanostoMillis( Math.round( latencyTimeSummary.getMean() ) ) );
      LOGGER.info( "perfmetric:50_latency:" //
                       + fromNanostoMillis( latencyTimeSummary.getValue50() ) );
      LOGGER.info( "perfmetric:90_latency:" //
                       + fromNanostoMillis( latencyTimeSummary.getValue90() ) );
      LOGGER.info( "----------------------------------------------------" );
      LOGGER.info( "response 1xx family: "
                       + runner.getGlobalSummaryListener().getResponses1xx().longValue() );
      LOGGER.info( "response 2xx family: "
                       + runner.getGlobalSummaryListener().getResponses2xx().longValue() );
      LOGGER.info( "response 3xx family: "
                       + runner.getGlobalSummaryListener().getResponses3xx().longValue() );
      LOGGER.info( "response 4xx family: "
                       + runner.getGlobalSummaryListener().getResponses4xx().longValue() );
      LOGGER.info( "response 5xx family: "
                       + runner.getGlobalSummaryListener().getResponses5xx().longValue() );
      LOGGER.info( "" );

      synchronized ( this.runStatus ) {
        this.runStatus =
            new RunStatus( this.runId, //
                           latencyTimeSummary.getTotalCount(), //
                            fromNanostoMillis( latencyTimeSummary.getMaxValue() ), //
                            fromNanostoMillis( latencyTimeSummary.getMinValue() ), //
                            fromNanostoMillis( Math.round( latencyTimeSummary.getMean() ) ), //
                            fromNanostoMillis( latencyTimeSummary.getValue50() ), //
                            fromNanostoMillis( latencyTimeSummary.getValue90() ), //
                            Eta.FINISHED, //
                            qps ) //
            .startDate( this.runStartDate ) //
            .endDate( this.runEndDate );
      }
      // last record
      bigQueryRecord( this.runStatus );
    } finally {
      executorService.shutdownNow();
    }
  }

  public PerfRunner startJetty(LoadGeneratorStarterConfig runnerArgs) throws Exception {

    QueuedThreadPool serverThreads = new QueuedThreadPool();
    serverThreads.setName( "server" );
    server = new Server( serverThreads );
    server.setSessionIdManager( new DefaultSessionIdManager(server) );
    connector = new ServerConnector( server, new HttpConnectionFactory( new HttpConfiguration() ) );
    String jettyPort = runnerArgs.getParams().get( "jettyPort" );
    int port = NumberUtils.toInt( jettyPort, 8080 );
    connector.setPort( port );

    server.addConnector( connector );
    server.setHandler( statisticsHandler );
    ServletContextHandler statsContext = new ServletContextHandler( statisticsHandler, "/" );
    statsContext.addServlet( new ServletHolder( new StatisticsServlet() ), "/stats" );
    RunStatusHandler runStatusHandler = new RunStatusHandler(this);
    statsContext.addServlet( new ServletHolder( runStatusHandler ), "/status" );
    RunnerHandler runnerHandler = new RunnerHandler(this);
    statsContext.addServlet( new ServletHolder( runnerHandler ), "/run" );
    RunConfigHandler runConfigHandler = new RunConfigHandler(this);
    statsContext.addServlet( new ServletHolder( runConfigHandler ), "/config" );
    statsContext.setSessionHandler( new SessionHandler() );
    server.start();
    return this;
  }

  protected static BigQuery setupBigQuery() {
    BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();
    Dataset dataset = bigQuery.getDataset( BIG_QUERY_DATASETID );
    // create dataset if not exists
    if (dataset == null) {
      dataset = bigQuery.create( DatasetInfo.newBuilder( BIG_QUERY_DATASETID ).build() );
    }

    TableId tableId = TableId.of( BIG_QUERY_DATASETID, BIG_QUERY_TABLE_NAME );
    Table table = bigQuery.getTable( tableId);
    // create table if not exists
    if (table == null) {
      LOGGER.info("Creating big query table: '{}'", tableId);
      Field runId = Field.of( "runId", Field.Type.string());
      Field timestampField = Field.of( "timestamp", Field.Type.timestamp());
      Field startDateField = Field.of( "startDate", Field.Type.timestamp());
      Field endDateField = Field.of( "endDate", Field.Type.timestamp());
      Field etaField = Field.of( "eta", Field.Type.string());
      Field requestNumber = Field.of( "requestNumber", Field.Type.integer());
      Field maxLatency = Field.of( "maxLatency", Field.Type.integer());
      Field minLatency = Field.of( "minLatency", Field.Type.integer());
      Field aveLatency = Field.of( "aveLatency", Field.Type.integer());
      Field latency50 = Field.of( "latency50", Field.Type.integer());
      Field latency90 = Field.of( "latency90", Field.Type.integer());
      Field qps = Field.of( "qps", Field.Type.integer());
      Schema schema = Schema.of(runId, timestampField, startDateField, endDateField, //
                                etaField, requestNumber, maxLatency, minLatency, //
                                aveLatency, latency50, latency90, qps);
      table = bigQuery.create( TableInfo.of( tableId, StandardTableDefinition.of( schema)));
    }
    return bigQuery;
  }

  protected static Map<String, Object> toBigQueryRows( RunStatus runStatus ) {
    if (runStatus == null) {
      return null;
    }
    Map<String, Object> row = new HashMap<>(11);
    row.put( "runId", runStatus.runId );
    if (runStatus.timestamp != null) {
      row.put( "timestamp", BIG_QUERY_DATE_FORMATTER.format( runStatus.timestamp.toInstant()) );
    }
    if (runStatus.startDate != null) {
      row.put( "startDate", BIG_QUERY_DATE_FORMATTER.format( runStatus.startDate.toInstant()) );
    }
    if (runStatus.endDate != null) {
      row.put( "endDate", BIG_QUERY_DATE_FORMATTER.format( runStatus.endDate.toInstant()) );
    }
    row.put( "eta", runStatus.eta == null ? "" : runStatus.eta.toString() );
    row.put( "requestNumber", runStatus.requestNumber );
    row.put( "maxLatency", runStatus.maxLatency );
    row.put( "minLatency", runStatus.minLatency );
    row.put( "aveLatency", runStatus.aveLatency );
    row.put( "latency50", runStatus.latency5 );
    row.put( "latency90", runStatus.latency9 );
    row.put( "qps", runStatus.qps );
    return row;
  }

  // TODO async mode?
  protected void bigQueryRecord( RunStatus runStatus ) {
    if (this.bigQuery != null) {
      Map<String, Object> row = toBigQueryRows( runStatus );
      TableId tableId = TableId.of( BIG_QUERY_DATASETID, BIG_QUERY_TABLE_NAME);
      InsertAllRequest insertRequest =
          InsertAllRequest.newBuilder(tableId).addRow( row ).build();
      InsertAllResponse insertResponse = this.bigQuery.insertAll( insertRequest);
      if (insertResponse.hasErrors()) {
        LOGGER.info("Errors occurred while inserting rows: {}", //
                    insertResponse.getInsertErrors());
      }
    }
  }

  public static class RunStatus {

    private String runId;
    @JsonProperty
    @JsonFormat( shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ" )
    private Date timestamp;
    @JsonProperty
    @JsonFormat( shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ" )
    private Date startDate;
    @JsonProperty
    @JsonFormat( shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ" )
    private Date endDate;
    private Eta eta;
    private long requestNumber;
    private long maxLatency;
    private long minLatency;
    private long aveLatency;
    // well checkstyle do not allow 50 (Abbreviation in name must contain no more than '1')
    @JsonProperty("latency50")
    private long latency5;
    @JsonProperty("latency90")
    private long latency9;
    private long qps;

    public RunStatus() {
    }

    public RunStatus( Eta eta ) {
      this.eta = eta;
      this.timestamp = new Date();
    }

    public RunStatus( String runId, long requestNumber, long maxLatency, long minLatency, //
                      long aveLatency, long latency50, long latency90, Eta eta, long qps ) {
      this(eta);
      this.runId = runId;
      this.requestNumber = requestNumber;
      this.maxLatency = maxLatency;
      this.minLatency = minLatency;
      this.aveLatency = aveLatency;
      this.latency5 = latency50;
      this.latency9 = latency90;
      this.qps = qps;
    }

    public RunStatus startDate( Date startDate ) {
      this.startDate = startDate;
      return this;
    }

    public RunStatus endDate( Date endDate ) {
      this.endDate = endDate;
      return this;
    }

    public long getRequestNumber() {
      return requestNumber;
    }

    public long getMaxLatency() {
      return maxLatency;
    }

    public long getMinLatency() {
      return minLatency;
    }

    public long getAveLatency() {
      return aveLatency;
    }

    public long getLatency50() {
      return latency5;
    }

    public long getLatency90() {
      return latency9;
    }

    public Eta getEta() {
      return eta;
    }

    public long getQps() {
      return qps;
    }

    public Date getTimestamp() {
      return this.timestamp;
    }

    public Date getStartDate() {
      return startDate;
    }

    public Date getEndDate() {
      return endDate;
    }

    public String getRunId() {
      return runId;
    }

    public void setRunId( String runId ) {
      this.runId = runId;
    }
  }

  public enum Eta {
    RUNNING,FINISHED,NOT_STARTED;
  }

  static long fromNanostoMillis(long nanosValue) {
    return TimeUnit.MILLISECONDS.convert( nanosValue, TimeUnit.NANOSECONDS );
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

  public static class LoadGeneratorStarterConfig extends LoadGeneratorStarterArgs {
    @DynamicParameter(names = "-D", description = "Dynamic parameters go here")
    private Map<String,String> params = new HashMap<>(  );

    public Map<String, String> getParams() {
      return params;
    }

    public void setParams( Map<String, String> params ) {
      this.params = params;
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
      runnerArgs.setResourceRate(Integer.parseInt( transactionRate ));
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
      runnerArgs.setIterations(Integer.parseInt( runIteration ));
    }

  }

}
