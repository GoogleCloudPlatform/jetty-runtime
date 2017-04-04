## Perf app

### Test webapp
The submodule `perf-server` contains a simple webapp which can be deploy to gcloud using with activating the `gcloud-deploy` profile `-Pgcloud-deploy`:

`mvn install -Pgcloud-deploy -Dgcloud.sdk.path=<path to your gcloud sdk directory>`  

This contains a very simple webapp with static resources and a basic servlet.

### Load testing
Load testing can be performed using the following command:

`mvn clean install -Pperf -Prun-perf -DskipTests -Drunning.time.unit=s -Dwarmup.number=5 -Drunning.time=30 -Dusers=20`

You can define a number of parameters for the testing including the numbers of users (`-Dusers`) and running time (`-Drunning.time`).

To deploy new version add the profile `-Pgcloud-deploy`

### Remote Load testing

You can deploy Docker images running Load testing using:

` mvn clean install -Pperf -pl :perf-runner -am  -P-Pgcloud-deploy -DskipTests -DskipTests`

#### Load Testing Parameters

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
* maxRequestsQueued>410000</maxRequestsQueued>
