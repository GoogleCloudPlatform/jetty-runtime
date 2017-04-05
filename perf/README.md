# Performance Testing

Within this directory is a load testing framework based upon the premise of establishing a baseline for nominal behavior for certain metrics and ideally detecting deviations.  Deviations detected may or may not relate directly to Jetty or the execution environment, the underlying premise is that detecting any deviation is worthy of some investigation.

The following modules are involved:

* perf-server: A simple webapp which is deployed into Google Flexible environment with static and dynamic resources.
* perf-runner: A configurable load testing client which is bundled into a Docker container which generates load to the webapp deployed in under perf-server.

The process involved is 2 step, first an instance of the perf-server is deployed into the GCE and started up.  Once this is in place we are able to navigate to all the assets that the load client can through a normal browser, it is for all intents and purposes a standard Google Flexible based application.  We hold the number of instances of this application to a fixed amount of 1 with no intent to enable autoscaling (at this time).  We then deploy the pre-configured perf-runner instances which once deployed will begin a period of warm-up or burn-in by making some initial requests to the perf-server.  This helps ensure the JVM for both the perf-server and perf-runner(s) have compiled all JIT related optimizations and garbage collection events have been normalized.  After this burn-in period the perf-runner instances will begin to offer up a consistent amount of load intended to bring the perf-server into a state of stable load.  Effort is taken to ensure that from the perf-server and perf-client perspective there should be as little variation in performance from one run to another.  Given the same constant request rate and the same amount of queued requests on the perf-client, and the same webapp deployed with the same assets on the perf-server there should be little no variation between muitple runs.  

In this way if a change in the monitored metrics are detected then it may be important to determine what the source the variation is.  Has a new version of Jetty been deployed in the perf-server?  Have there been changes in the underlying docker instance of the perf-server?  Differences in the JDK between runs?

Currently we use a combination of Stackdriver and logging within the perf-server and perf-runner instances to gather data. 

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

<insert example here>

Within the perf-server the following information may be found in the streaming logs:

(Currently disabled)


## Usage

This module is currently configured to be interacted and configured via Maven.  The following sections detail how to configure the environment and get this running.  

NOTE: We assume you already have gcloud configured correctly for your environment.

### perf-server

The submodule `perf-server` contains a simple webapp which can be deploy to gcloud using with activating the appropriate maven profile `-Pgcloud-deploy`:

`mvn install -Pgcloud-deploy -Dgcloud.sdk.path=<path to your gcloud sdk directory>`  

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

` mvn clean install -Pperf -pl :perf-runner -am  -P-Pgcloud-deploy -DskipTests -DskipTests`

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
