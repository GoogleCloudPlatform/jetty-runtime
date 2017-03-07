# Google Cloud Platform Jetty Docker Image

This repository contains the source for the `gcr.io/google-appengine/jetty` [docker](https://docker.com) image. This image can be used as the base image for running Java web applications on [Google App Engine Flexible Environment](https://cloud.google.com/appengine/docs/flexible/java/) and [Google Container Engine](https://cloud.google.com/container-engine). It provides the Jetty Servlet container on top of the [OpenJDK image](https://github.com/GoogleCloudPlatform/openjdk-runtime).

The layout of this image is intended to mostly mimic the official [docker-jetty](https://github.com/appropriate/docker-jetty) image and unless otherwise noted, the official [docker-jetty documentation](https://github.com/docker-library/docs/tree/master/jetty) should apply.

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
docker run gcr.io/google-appengine/jetty --list-config
```

Alternate commands can also be passed to the docker run command, so the
image can be explored with 
```console
docker run -it --rm gcr.io/google-appengine/jetty bash
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

The [/docker-entrypoint.bash](https://github.com/GoogleCloudPlatform/openjdk-runtime/blob/master/openjdk8/src/main/docker/docker-entrypoint.bash)
for the image is inherited from the openjdk-runtime and its capabilities are described in the associated 
[README](https://github.com/GoogleCloudPlatform/openjdk-runtime/blob/master/README.md)

This image updates the docker `CMD` and appends to the
[setup-env.bash](https://github.com/GoogleCloudPlatform/openjdk-runtime/blob/master/openjdk8/src/main/docker/setup-env.bash)
script to include options and arguments to run the Jetty container, unless an executable argument is passed to the docker image.
Additional environment variables are set including:

|Env Var           | Maven Prop      | Value                                                |
|------------------|-----------------|------------------------------------------------------|
|`JETTY_VERSION`   |`jetty9.version` |                                                      |
|`GAE_IMAGE_NAME`  |                 |`jetty`                                               |
|`GAE_IMAGE_LABEL` |`docker.tag.long`|                                                      |
|`JETTY_HOME`      |`jetty.home`     |`/opt/jetty-home`                                     |
|`JETTY_BASE`      |`jetty.base`     |`/var/lib/jetty`                                      |
|`TMPDIR`          |                 |`/tmp/jetty                                           |
|`JETTY_ARGS`      |                 |`-Djetty.base=$JETTY_BASE -jar $JETTY_HOME/start.jar` |
|`ROOT_WAR`        |                 |`$JETTY_BASE/webapps/root.war`                        |
|`ROOT_DIR`        |                 |`$JETTY_BASE/webapps/root`                            |
|`JAVA_OPTS`       |                 |`$JAVA_OPTS $JETTY_ARGS`                              |

If a WAR file is found at `$ROOT_WAR`, it is unpacked to `$ROOT_DIR` if it is newer than the directory or the directory
does not exist.  If there is no `$ROOT_WAR` or `$ROOT_DIR`, then `/app` is symbolic linked to `$ROOT_DIR`. If 
a `$ROOT_DIR` is discovered or made by this script, then it is set as the working directory. 
See [Extending the image](#extending-the-image) below for some examples of adding an application as a WAR file or directory.


The command line executed is effectively (where $@ are the args passed into the docker entry point):
```
java $JAVA_OPTS \
     -Djetty.base=$JETTY_BASE \
     -jar $JETTY_HOME/start.jar \
     "$@"
```
### Extending logging
The `java.util.logging` configuration may be changed at runtime by providing an alternate
properties file. This can be done either by extending the image and replacing the
default configuration at `$JETTY_BASE/etc/java-util-logging.properties`, or a new
configuration can be provided as part of the application (eg `WEB-INF/logging.properties`)
and setting the Java System Property `java.util.logging.config.file` to point to it.
System properties can be set by setting the unix environment variable `JAVA_USER_OPTS`.

An example of running the image locally with a mounted application is:
```bash
docker run \
  -e JAVA_USER_OPTS=-Djava.util.logging.config.file=WEB-INF/logging.properties \
  -v /some-path/your-application:/app gcr.io/google_appengine/jetty
```

When deploying via the Cloud SDK and/or plugin, the `JAVA_USER_OPTS` may be set in
the `app.yaml` file:
```yaml
env_variables:
  JAVA_USER_OPTS: -Djava.util.logging.config.file=WEB-INF/logging.properties
```

The default logging.properties file contains the following to configure `java.util.logging`
to use the gcloud stackdriver logging mechanism:
```
.level=INFO

handlers=com.google.cloud.logging.LoggingHandler
com.google.cloud.logging.LoggingHandler.level=FINE
com.google.cloud.logging.LoggingHandler.log=gae_app.log
com.google.cloud.logging.LoggingHandler.resourceType=gae_app
com.google.cloud.logging.LoggingHandler.enhancers=com.google.cloud.logging.GaeFlexLoggingEnhancer
com.google.cloud.logging.LoggingHandler.formatter=java.util.logging.SimpleFormatter
java.util.logging.SimpleFormatter.format=%3$s: %5$s%6$s
```

The configuration of the jetty container in this image can be viewed by running the image locally:
```
docker run --rm -it gcr.io/google-appengine/jetty --list-config --list-modules
```

## Extending the image

The image produced by this project may be automatically used/extended by the Cloud SDK and/or App Engine maven plugin. 
Alternately it may be explicitly extended with a custom Dockerfile.  

The latest released verion of this image is available at `gcr.io/google-appengine/jetty`, alternately you may 
build and push your own version with the shell commands:
```bash
mvn clean install
docker tag jetty:latest gcr.io/your-project-name/jetty:your-label
gcloud docker -- push gcr.io/your-project-name/jetty:your-label
```

### Adding the root WAR application to an image
A standard war file may be deployed as the root context in an extended image by placing the war file 
in the docker build directory and using a `Dockerfile` like:
```dockerfile
FROM gcr.io/google-appengine/jetty
COPY your-application.war $JETTY_BASE/webapps/root.war
```

### Adding the root application to an image
If the application exists as directory (i.e. an expanded war file), then directory must
be placed in the docker build directory and using a `Dockerfile` like: 
```dockerfile
FROM gcr.io/google-appengine/jetty
COPY your-application-dir $JETTY_BASE/webapps/root
```

### Mounting the root application at local runtime
If no root WAR or root directory is found, the `docker-entrypoint.bash` script will link the 
`/app` directory as the root application. Thus the root application can be added to the 
image via a runtime mount:
```bash
docker run -v /some-path/your-application:/app gcr.io/google-appengine/jetty  
```

# Development Guide

* See [instructions](DEVELOPING.md) on how to build and test this image.

# Contributing changes

* See [CONTRIBUTING.md](CONTRIBUTING.md)

## Licensing

* See [LICENSE.md](LICENSE)
