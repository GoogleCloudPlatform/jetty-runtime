# Performance Testing

Within this directory is a load testing framework based upon the premise of establishing a baseline for nominal behavior for certain metrics and ideally detecting deviations.  Deviations detected may or may not relate directly to Jetty or the execution environment, the underlying premise is that detecting any deviation is worthy of some investigation.

The following modules are involved:

* perf-server: A simple webapp which is deployed into Google Flexible environment with static and dynamic resources.
* perf-runner: A configurable load testing client which is bundled into a Docker container which generates load to the webapp deployed in under perf-server.
* perf-test: A unit test that will monitoring an executing performance test and then validate the final results against injected criteria.

The process involved is 2 step, first an instance of the perf-server is deployed into the GCE and started up.  Once this is in place we are able to navigate to all the assets that the load client can through a normal browser, it is for all intents and purposes a standard Google Flexible based application.  We hold the number of instances of this application to a fixed amount of 1 with no intent to enable autoscaling (at this time).  We then deploy the pre-configured perf-runner instances which once deployed will begin a period of warm-up or burn-in by making some initial requests to the perf-server.  This helps ensure the JVM for both the perf-server and perf-runner(s) have compiled all JIT related optimizations and garbage collection events have been normalized.  After this burn-in period the perf-runner instances will begin to offer up a consistent amount of load intended to bring the perf-server into a state of stable load.  Effort is taken to ensure that from the perf-server and perf-client perspective there should be as little variation in performance from one run to another.  Given the same constant request rate and the same amount of queued requests on the perf-client, and the same webapp deployed with the same assets on the perf-server there should be little no variation between muitple runs.  

In this way if a change in the monitored metrics are detected then it may be important to determine what the source the variation is.  Has a new version of Jetty been deployed in the perf-server?  Have there been changes in the underlying docker instance of the perf-server?  Differences in the JDK between runs?

In terms of a monitoring dashboard, currently we use a combination of Stackdriver and logging within the perf-server and perf-runner instances to gather data. 

On the Stackdriver side of things we are interested in:

* GAE Module (perf-server): Response Latencies (5%, 95%, Average)
* GAE Module (perf-server): 200 Response Latencies (5%, 95%, Average)
* GAE Module (perf-server): 400 Response Latencies (5%, 95%, Average) - disabled
* GAE Module (perf-server): 200/400 Response Rate
* GAE Module (perf-server): Network Inbound/Outbound Traffic
* GAE Module (perf-server): Flexible VM Network Inbound/Outbound Traffic
* GAE Module (perf-server/perf-runner): Flexible VM CPU Utilization
* GAE Module (perf-server/perf-runner): Flexible VM Memory Usage

Within the perf-runner the following information may be found in the streaming logs:

* estimated qps every 30s for each hosts (grep log on INFO) format:

`
2017-04-05 02:25:22.713:INFO:omjlgl.QpsListenerDisplay:pool-1-thread-1: ----------------------------------------
2017-04-05 02:25:22.713:INFO:omjlgl.QpsListenerDisplay:pool-1-thread-1: --------    QPS estimation    ----------
2017-04-05 02:25:22.713:INFO:omjlgl.QpsListenerDisplay:pool-1-thread-1: ---------------------------------------- 
2017-04-05 02:25:22.713:INFO:omjlgl.QpsListenerDisplay:pool-1-thread-1: host 'cced3cb67b60' estimated QPS : 5534
`
* request currently in queue (grep log on INFO) format:

`
2017-04-05 02:30:22.733:INFO:omjlgl.RequestQueuedListenerDisplay:pool-2-thread-1: ----------------------------------------
2017-04-05 02:30:22.733:INFO:omjlgl.RequestQueuedListenerDisplay:pool-2-thread-1:   Requests in queue: 35631
2017-04-05 02:30:22.733:INFO:omjlgl.RequestQueuedListenerDisplay:pool-2-thread-1: ----------------------------------------
`

* end summary of total QPS and response time (TODO change it to latency?) (grep log on INFO) format:

`
2017-03-31 11:41:23.714:INFO:cgcrjp.PerfRunner:main: ----------------------------------------------------
2017-03-31 11:41:23.714:INFO:cgcrjp.PerfRunner:main: -----------    Result Summary     ------------------
2017-03-31 11:41:23.714:INFO:cgcrjp.PerfRunner:main: ----------------------------------------------------
2017-03-31 11:41:23.714:INFO:cgcrjp.PerfRunner:main: CollectorInformations millis:totalCount=4948895, minValue=0, maxValue=40231, mean=3855, 
stdDeviation=5501, value 50%=899, value 90%=2147, startTimeStamp=2017-03-31T11:26:02 UTC, endTimeStamp=2017-03-31T11:41:23 UTC
2017-03-31 11:41:23.714:INFO:cgcrjp.PerfRunner:main: ---------------------------------------------------- 
2017-03-31 11:41:23.714:INFO:cgcrjp.PerfRunner:main: -----------     Estimated QPS     ------------------
2017-03-31 11:41:23.714:INFO:cgcrjp.PerfRunner:main: ----------------------------------------------------
2017-03-31 11:41:23.714:INFO:cgcrjp.PerfRunner:main: host '2e0ce9d008e0' estimated QPS : 5373
2017-03-31 11:41:23.714:INFO:cgcrjp.PerfRunner:main: ----------------------------------------------------
` 

Within the perf-server the following information may be found in the streaming logs:

(Currently disabled)


## Usage

This module is currently configured to be interacted and configured via either a shell script or Maven.   Following the shell script usage section are details on how to configure the environment and get this running with Maven.    

NOTE: We assume you already have gcloud configured correctly for your environment.

### perf.sh 

The _perf_ directory has a perf.sh shell script which handles the bulk of logic related to setting up and running the performance testing scenario and is broke into three phases upon execution.

* First we determine if a perf-server application needs to be deployed, if it does then we descend into the perf-server artifact and build and deploy a new perf server.
* Second we determine if a perf-client application needs of be deployed, if it does then we do the same in perf-client artifact.  Otherwise we check if there is a live perf-client instance that we can leverage to run the performance test.  If there is then we use curl to push it a json configuration file that details the parameters of the performance test to run.
* Lastly we move into the perf-test artifact and execute a standard junit test which will pull the perf-client instance looking for a FINISHED state.  When a FINISHED state is found the unit test will then validate the final statistics of the unit test to ensure the reported statistics fall within the established boundries.  

Over time the metrics being monitored and validated will become broader.

To find the available parameters for the perf.sh script simply run it once for output akin to this:
`
Usage: perf.sh --app-name <name> --app-project <project> --target-name <name> --running-time # (in minutes)
  required settings
   [ --app-name <name> ]
   [ --app-project <project> ]
   [ --target-name <name> ]
   [ -rt # | --running-time # ]
  client settings
   [ -tr # | --transaction-rate # ]
   [ -ri # | --runner-instances # ]
  test settings
   [ -tlr #-# | --test-latency-range #-# ]
   [ -tqr #-# | --test-qps-range #-# ]
  runtime settings
   [ -scd | --skip-client-deploy ]
   [ -ssd | --skip-server-deploy ]
   [ --verbose ]
   [ --log-file <filename> ] (perf.log)
`

### perf-server

The submodule `perf-server` contains a simple webapp which can be deploy to gcloud using with activating the appropriate maven profile `-Pgcloud-deploy`:

`mvn install -Pgcloud-deploy`  

The docker image will be based on the jetty image containing the war file.

The app.yaml contains some configuration which can be overridden with command line:
* resources.cpu: number of cpu (default 2)
* resources.memory_gb: memory to user (default 8gb)
* resources.disk_size_gb: disk size (default: 10gb)
* automatic_scaling.min_num_instances: minimum number of instances (default: 1)
* automatic_scaling.max_num_instances: maximum number of instances (default: 1)
`

### perf-runner (local)

Load testing can be performed using the following command:

`mvn clean install -Pperf -Prun-perf -DskipTests -Drunning.time.unit=s -Dwarmup.number=5 -Drunning.time=30 -Dusers=20`

You can define a number of parameters for the testing including the numbers of users (`-Dusers`) and running time (`-Drunning.time`).

To deploy new version add the profile `-Pgcloud-deploy`

### perf-runner (remote)

You can deploy Docker images running Load testing using:

` mvn clean install -Pperf -pl :perf-runner -am  -Pgcloud-deploy -DskipTests`

### Load Testing Parameters

The load testing is done using the [load generator library](https://github.com/jetty-project/jetty-load-generator).

Some parameters are available to configure the load:
* host: host with the target App
* port: port
* running.time: the time to run the load test (default: 120)
* running.time.unit: the unit of time (default: m) (possible values:h,m,s,ms)
* transaction_rate: the number of transaction to send per second (transaction means the whole tree from the profile file)
* scheme: the scheme to use (default: https)
* users: number of users/httpclient (default: 14)
* profile.json: path to groovy profile to use (default: ${basedir}/src/main/appengine/loadgenerator_profile.groovy)
* warmup.number: number of warmup iterations to run (default: 10)
* runner.instances: number of instances to use (default: 2)
* threads: number of threads to use (default: 1) (note each thread will start the same number of users/httpclient)
* debug.args: debug arguments to pass to jvm start (default: none)
* resources.cpu: number of cpu per instances (default: 12) (see app.yaml file)
* resources.memory_gb: total memory (defaut: 16) (see app.yaml file)
* httpClientSelectors: number of selector per http client transport/client (default: 2)
* maxRequestsQueued: (default: 410000) 

## Example Loads:

The following are some configurations and their results from our testing.

[width="100%", frame="topbot", options="header"]
| perf-server | perf-runner | qps | latency |
| resources.cpu=2, resources.memory_gb=8 | users=?, runner.instances=2, etc | 8-9k | ? |
