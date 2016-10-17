# Google Cloud Platform Jetty Docker Image

This repository contains the source for the `gcr.io/google_appengine/jetty` [docker](https://docker.com) image. This image can be used as the base image for running Java web applications on [Google App Engine Flexible Environment](https://cloud.google.com/appengine/docs/flexible/java/) and [Google Container Engine](https://cloud.google.com/container-engine). It provides the Jetty Servlet container on top of the [OpenJDK image](https://github.com/GoogleCloudPlatform/openjdk-runtime).

The layout of this image is intended to mostly mimic the official [docker-jetty](https://github.com/appropriate/docker-jetty) image and unless otherwise noted, the official [docker-jetty documentation](https://github.com/docker-library/docs/tree/master/jetty) should apply.

## Building the Jetty image
To build the image you need git, docker and maven installed:
```console
git clone https://github.com/GoogleCloudPlatform/jetty-runtime.git
cd jetty-runtime
mvn clean install
```

## Running the Jetty image
The resulting image is called jetty (with more specific tags also created)
and can be run with:
```console
docker run jetty
```
## Google Modules & Configuration
The jetty base in this image has some additional google specific modules:

Module | Description | enabled
-------|-------------|------- 
 gae   | enables JSON formatted server logging; enables request log; | true  

The `$JETTY_BASE/resources/jetty-logging.properties` file configures the
jetty logging mechanism to use `java.util.logging'.  This is configured
using `$JETTY_BASE/etc/java-util-logging.properties` which set a JSON formatter
for logging to `/var/log/app_engine/app.%g.log.json`.  

The request log also defaults to log into `/var/log/app_engine/` by the 
`gae` module

## Configuring the Jetty image
Arguments passed to the docker run command are passed to Jetty, so the 
configuration of the jetty server can be seen with a command like:
```console
docker run jetty --list-config
```

Alternate commands can also be passed to the docker run command, so the
image can be explored with 
```console
docker run -it --rm jetty bash
```

To update the server configuration in a derived Docker image, the `Dockerfile` may
enable additional modules with `RUN` commands like:
```
WORKDIR $JETTY_BASE
RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=jmx,stats
```
Modules may be configured in a `Dockerfile` by editing the properties in the corresponding mod files in `/var/lib/jetty/start.d/` or the module can be deactivated by removing that file.

## App Engine Flexible Environment
This image works with App Engine Flexible Environment as a custom runtime.
In order to use it, you need to build the image (let's call it `YOUR_BUILT_IMAGE`), (and optionally push it to a Docker registery like gcr.io). Then, you can add to any pure Java EE 7 Web Application projects these 2 configuration files next to the exploded WAR directory:

`Dockerfile` file would be:
      
      FROM YOUR_BUILT_IMAGE
      add . /app
      
That will add the Web App Archive directory in the correct location for the Docker container.

Then, an `app.yaml` file to configure the App Engine Flexible Environment product:

      runtime: custom
      vm: true
      
Once you have this configuration, you can use the Google Cloud SDK to deploy this directory containing the 2 configuration files and the Web App directory using:

     gcloud app deploy app.yaml
     

## Entry Point Features
The entry point for the image is [docker-entrypoint.bash](https://github.com/GoogleCloudPlatform/jetty-runtime/blob/master/jetty9/src/main/docker/docker-entrypoint.bash), which does the processing of the passed command line arguments to look for an executable alternative or arguments to the default command (java).

If the default command (java) is used, then the entry point sources the [setup-env.bash](https://github.com/GoogleCloudPlatform/openjdk-runtime/blob/master/openjdk8/src/main/docker/setup-env.bash), which looks for supported features to be enabled and/or configured.  The following table indicates the environment variables that may be used to enable/disable/configure features, any default values if they are not set: 

|Env Var           | Description         | Type     | Default                                     |
|------------------|---------------------|----------|---------------------------------------------|
|`DBG_ENABLE`      | Stackdriver Debugger| boolean  | `true`                                      |
|`TMPDIR`          | Temporary Directory | dirname  |                                             |
|`JAVA_TMP_OPTS`   | JVM tmpdir args     | JVM args | `-Djava.io.tmpdir=${TMPDIR}`                |
|`GAE_MEMORY_MB`   | Available memory    | size     | Set by GAE or `/proc/meminfo`-400M          |
|`HEAP_SIZE_MB`    | Available heap      | size     | 80% of `${GAE_MEMORY_MB}`                   |
|`JAVA_HEAP_OPTS`  | JVM heap args       | JVM args | `-Xms${HEAP_SIZE_MB}M -Xmx${HEAP_SIZE_MB}M` |
|`JAVA_GC_OPTS`    | JVM GC args         | JVM args | `-XX:+UseG1GC` plus configuration           |
|`JAVA_GC_LOG`     | JVM GC log file     | filename |                                             |
|`JAVA_GC_LOG_OPTS`| JVM GC args         | JVM args | Derived from `$JAVA_GC_LOG`                 |
|`JAVA_USER_OPTS`  | JVM other args      | JVM args |                                             |
|`JAVA_OPTS`       | JVM args            | JVM args | See below                                   |

If not explicitly set, `JAVA_OPTS` is defaulted to 
```
JAVA_OPTS:=-showversion \
           ${JAVA_TMP_OPTS} \
           ${DBG_AGENT} \
           ${JAVA_HEAP_OPTS} \
           ${JAVA_GC_OPTS} \
           ${JAVA_GC_LOG_OPTS} \
           ${JAVA_USER_OPTS}
```

The command line executed is effectively (where $@ are the args passed into the docker entry point):
```
java $JAVA_OPTS \
     -Djetty.base=$JETTY_BASE \
     -jar $JETTY_HOME/start.jar \
     "$@"
```

# Contributing changes

* See [CONTRIBUTING.md](CONTRIBUTING.md)

## Licensing

* See [LICENSE.md](LICENSE)
