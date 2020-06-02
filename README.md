![Build Status](http://storage.googleapis.com/java-runtimes-kokoro-build-badges/jetty-runtime-master.png)

# Google Cloud Platform Jetty Docker Image

This repository contains the source for the Google-maintained Jetty [docker](https://docker.com) image.
This image can be used as the base image for running Java web applications on
[Google App Engine Flexible Environment](https://cloud.google.com/appengine/docs/flexible/java/)
and [Google Container Engine](https://cloud.google.com/container-engine).
It provides the Jetty Servlet container on top of the
[OpenJDK image](https://github.com/GoogleCloudPlatform/openjdk-runtime).

This image is mirrored at both `launcher.gcr.io/google/jetty` and `gcr.io/google-appengine/jetty`.

The layout of this image is intended to mostly mimic the official [docker-jetty](https://github.com/appropriate/docker-jetty) image and unless otherwise noted, the official [docker-jetty documentation](https://github.com/docker-library/docs/tree/master/jetty) should apply.

## Configuring the Jetty image
Arguments passed to the `docker run` command are passed to Jetty, so the 
configuration of the jetty server can be seen with a command like:
```console
docker run gcr.io/google-appengine/jetty --list-config
```

Alternate commands can also be passed to the `docker run` command, so the
image can be explored with 
```console
docker run -it --rm launcher.gcr.io/google/jetty bash
```

Various environment variables (see below) can also be used to set jetty properties, enable modules and
disable modules.  These variables may be set either in an `app.yaml` or passed in to a docker run
command eg.
```console
docker run -it --rm -e JETTY_PROPERTIES=jetty.http.idleTimeout=10000 launcher.gcr.io/google/jetty 
```

To update the server configuration in a derived Docker image, the `Dockerfile` may
enable additional modules with `RUN` commands like:
```dockerfile
WORKDIR $JETTY_BASE
RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=jmx,stats
```
Modules may be configured in a `Dockerfile` by editing the properties in the corresponding mod files in `/var/lib/jetty/start.d/` or the module can be deactivated by removing that file.

### Enabling gzip compression
The gzip handler is bundled with Jetty but not activated by default. To activate this module you have to set the environment
variable `JETTY_MODULES_ENABLE=gzip`

For example with docker:
```console
docker run -p 8080 -e JETTY_MODULES_ENABLE=gzip gcr.io/yourproject/yourimage
```

Or with GAE (app.yaml):
```yaml
env_variables:
  JETTY_MODULES_ENABLE: gzip
```

### Using Quickstart
Jetty provides [mechanisms](http://www.eclipse.org/jetty/documentation/9.4.x/quickstart-webapp.html) to speed up the start time of your application by pre-scanning its content and generating configuration files.
If you are using an [extended image](https://github.com/GoogleCloudPlatform/jetty-runtime/blob/master/README.md#extending-the-image) you can active quickstart by executing `/scripts/jetty/quickstart.sh` in your Dockerfile, after the application WAR is added.

```dockerfile
FROM launcher.gcr.io/google/jetty
ADD your-application.war $JETTY_BASE/webapps/root.war

# generate quickstart-web.xml
RUN /scripts/jetty/quickstart.sh
```

## App Engine Flexible Environment
When using App Engine Flexible, you can use the runtime without worrying about Docker by specifying `runtime: java` in your `app.yaml`:
```yaml
runtime: java
env: flex
```
The runtime image `launcher.gcr.io/google/jetty` will be automatically selected if you are attempting to deploy a WAR (`*.war` file).

If you want to use the image as a base for a custom runtime, you can specify `runtime: custom` in your `app.yaml` and then
write the Dockerfile like this:

```dockerfile
FROM launcher.gcr.io/google/jetty
ADD your-application.war $APP_DESTINATION_WAR
```
 
That will add the WAR in the correct location for the Docker container.

You can also use exploded-war artifacts:

```dockerfile
ADD your-application $APP_DESTINATION_EXPLODED_WAR
```

Once you have this configuration, you can use the Google Cloud SDK to deploy this directory containing the 2 configuration files and the WAR using:
```
gcloud app deploy app.yaml
```

## Container Engine & other Docker hosts

For other Docker hosts, you'll need to create a Dockerfile based on this image that copies your application code and installs dependencies. For example:

```dockerfile
FROM launcher.gcr.io/google/jetty
COPY your-application.war $APP_DESTINATION_WAR
```
If your artifact is an exploded-war, then use the `APP_DESTINATION_EXPLODED_WAR` environment variable instead. You can then build the docker container using `docker build` or [Google Cloud Container Builder](https://cloud.google.com/container-builder/docs/).
By default, the CMD is set to start the Jetty server. You can change this by specifying your own `CMD` or `ENTRYPOINT`.

## Entry Point Features
The [/docker-entrypoint.bash](https://github.com/GoogleCloudPlatform/openjdk-runtime/blob/master/openjdk8/src/main/docker/docker-entrypoint.bash)
for the image is inherited from the openjdk-runtime and its capabilities are described in the associated 
[README](https://github.com/GoogleCloudPlatform/openjdk-runtime/blob/master/README.md)

This image updates the docker `CMD` and adds the 
[/setup-env.d/50-jetty.bash](https://github.com/GoogleCloudPlatform/openjdk-runtime/blob/master/openjdk8/src/main/docker/50-jetty.bash)
script to include options and arguments to run the Jetty container, unless an executable argument is passed to the docker image.
Additional environment variables are used/set including:

|Env Var           | Maven Prop      | Value/Comment                                        |
|------------------|-----------------|------------------------------------------------------|
|`JETTY_VERSION`   |`jetty9.version` |                                                      |
|`GAE_IMAGE_NAME`  |                 |`jetty`                                               |
|`GAE_IMAGE_LABEL` |`docker.tag.long`|                                                      |
|`JETTY_HOME`      |`jetty.home`     |`/opt/jetty-home`                                     |
|`JETTY_BASE`      |`jetty.base`     |`/var/lib/jetty`                                      |
|`TMPDIR`          |                 |`/tmp/jetty`                                          |
|`JETTY_PROPERTIES`|                 |Comma separated list of `name=value` pairs appended to `$JETTY_ARGS` |
|`JETTY_MODULES_ENABLE`|            |Comma separated list of modules to enable by appending to `$JETTY_ARGS` |
|`JETTY_MODULES_DISABLE`|           |Comma separated list of modules to disable by removing from `$JETTY_BASE/start.d` |
|`JETTY_ARGS`      |                 |Arguments passed to jetty's `start.jar`. Any arguments used for custom jetty configuration should be passed here. |
|`ROOT_WAR`        |                 |`$JETTY_BASE/webapps/root.war`                        |
|`ROOT_DIR`        |                 |`$JETTY_BASE/webapps/root`                            |
|`JAVA_OPTS`       |                 |JVM runtime arguments                                 |

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
## Logging
This image is configured to use [Java Util Logging](https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html)(JUL) to capture all logging from 
the container and its dependencies.  Applications that also use the JUL API will inherit the same logging configuration.

By default JUL is configured to use a [ConsoleHandler](https://docs.oracle.com/javase/8/docs/api/java/util/logging/ConsoleHandler.html) to send logs to the `stderr` of the container process. When run on as a GCP deployment, all output to `stderr` is captured and is available via the Stackdriver logging console, however more detailed and integrated logs are available if the Stackdriver logging mechanism is used directly (see below).

To alter logging configuration a new `logging.properties` file must be provided to the image that among other things can: alter log levels generated by Loggers; alter log levels accepted by handlers; add/remove/configure log handlers. 


### Providing `logging.properties` via the web application
A new logging configuration file can be provided as part of the application (typically at `WEB-INF/logging.properties`)
and the Java System Property `java.util.logging.config.file` updated to reference it.

When running in a GCP environment, the system property can be set in `app.yaml`:
```yaml
env_variables:
  JETTY_ARGS: -Djava.util.logging.config.file=WEB-INF/logging.properties
```

If the image is run directly, then a `-e` argument to the `docker run` command can be used to set the system property:

```bash
docker run \
  -e JETTY_ARGS=-Djava.util.logging.config.file=WEB-INF/logging.properties \
  ...
```

### Providing `logging.properties` via a custom image
If this image is being used as the base of a custom image, then the following `Dockerfile` commands can be used to add either replace the existing logging configuration file or to add a new  `logging.properties` file.

The default logging configuration file is located at `/var/lib/jetty/etc/java-util-logging.properties`, which can be replaced in a custom image is built. The default configuration can be replaced with a `Dockerfile` like:

```Dockerfile
FROM gcr.io/google-appengine/jetty
ADD logging.properties /var/lib/jetty/etc/java-util-logging.properties
...
```

Alternately an entirely new location for the file can be provided and the environment amended in a `Dockerfile` like:

```Dockerfile
FROM gcr.io/google-appengine/jetty
ADD logging.properties /etc/logging.properties
ENV JETTY_ARGS -Djava.util.logging.config.file=/etc/logging.properties
...
```

### Providing `logging.properties` via docker run 
A `logging.properties` file may be added to an existing images using the `docker run` command if the deployment environment allows for the run arguments to be modified. The `-v` option can be used to bind a new `logging.properties` file to the running instance and the `-e` option can be used to set the system property to point to it:
```shell 
docker run -it --rm \
-v /mylocaldir/logging.properties:/etc/logging.properties \
-e JETTY_ARGS="-Djava.util.logging.config.file=/etc/logging.properties" \
...
```

### Enhanced Stackdriver Logging (BETA)
When running on the Google Cloud Platform Flex environment, the Java Util Logging can be configured to send logs to Google Stackdriver Logging by providing a `logging.properties` file that configures a [LoggingHandler](http://googlecloudplatform.github.io/google-cloud-java/0.10.0/apidocs/com/google/cloud/logging/LoggingHandler.html) as follows:
```
handlers=com.google.cloud.logging.LoggingHandler

# Optional configuration
.level=INFO
com.google.cloud.logging.LoggingHandler.level=FINE
com.google.cloud.logging.LoggingHandler.log=gae_app.log
com.google.cloud.logging.LoggingHandler.formatter=java.util.logging.SimpleFormatter
java.util.logging.SimpleFormatter.format=%3$s: %5$s%6$s

```
When deployed on the GCP Flex environment, an image so configured will automatically be configured with:
* a [LabelLoggingEnhancer](https://github.com/GoogleCloudPlatform/google-cloud-java/blob/v0.17.2/google-cloud-logging/src/main/java/com/google/cloud/logging/MonitoredResourceUtil.java#L224) instance, that will add labels from the monitored resource to each log entry.
* a [TraceLoggingEnhancer](https://github.com/GoogleCloudPlatform/google-cloud-java/blob/v0.17.2/google-cloud-logging/src/main/java/com/google/cloud/logging/TraceLoggingEnhancer.java) instance that will add any trace-id set to each log entry.
* the `gcp` module will be enabled that configures jetty so that the [setCurrentTraceId](https://github.com/GoogleCloudPlatform/google-cloud-java/blob/v0.17.2/google-cloud-logging/src/main/java/com/google/cloud/logging/TraceLoggingEnhancer.java#L40) method is called for any thread handling a request.

When deployed in other environments, logging enhancers can be manually configured by setting a comma separated list of class names as the
`com.google.cloud.logging.LoggingHandler.enhancers` property.

When using Stackdriver logging, it is recommended that `io.grpc` and `sun.net` logging level is kept at INFO level, as both these packages are used by Stackdriver internals and can result in verbose and/or initialisation problems. 

## Distributed Session Storage
The Jetty session mechanism is highly customizable and the options presented below are only a subset of meaningful configurations. Consult the [Jetty Sessions](https://www.eclipse.org/jetty/documentation/9.4.x/session-management.html) documentation for more details.
### Google Cloud Session Store
This image can be configured to use [Google Cloud Datastore](https://cloud.google.com/datastore/docs/) for clustered session storage by enabling the `gcp-datastore-sessions` jetty module. You can do this in your app.yaml:
```yaml
env_variables:
  JETTY_MODULES_ENABLE: gcp-datastore-sessions
```

Jetty will use the default namespace in Datastore as the store for all session data, or
`jetty.session.gcloud.namespace` property can be used to set an alternative namespace.   By default gcloud has no request affinity, so all session data will be retrieved and stored from the datastore on every request and no session data will be shared in memory.

Note that the `gcp-datastore-sessions` module is an aggregate module and the same configuration can be achieved by activating it's dependent modules individually:
```yaml
env_variables:
  JETTY_MODULES_ENABLE: session-cache-null,gcp-datastore,session-store-gcloud
```

### Cached Google Cloud Session Store
The Google Load Balancer can support instance affinity for more efficient session usage.  This can be configured in `app.yaml` with:
```yaml
network:
  session_affinity: true

env_variables:
  JETTY_MODULES_ENABLE: session-cache-hash,gcp-datastore,session-store-gcloud
```
Sessions will be retrieved from the in memory session cache and multiple requests can share a session instance. The Google Data Cloud is only accessed for unknown sessions (if affinity changes) or if a session is modified.
Session cache behaviour can be further configured by following the [Jetty Session Cache](https://www.eclipse.org/jetty/documentation/9.4.x/session-configuration-sessioncache.html) documentation.  Note that affinity is achieved by the Google Load Balancer setting a `GCLB` cookie rather than tracking the `JSESSIONID` cookie.

### Memcached Google Cloud Session Store (Alpha)
Sessions can be cached in memcache (without need for affinity) and backed by Google Cloud Datastore.  This can be configured in `app.yaml` with:

```yaml
env_variables:
  JETTY_MODULES_ENABLE: gcp-memcache-datastore-sessions
```
Note that the `gcp-memcache-datastore-sessions` module is an aggregate module and the same configuration can be achieved by activating it's dependent modules individually:

```yaml
env_variables:
  JETTY_MODULES_ENABLE: session-cache-null,gcp-datastore,session-store-gcloud,gcp-xmemcached,session-store-cache
```
The `session-cache-null` module may be replaced with the `session-cache-hash` module to achieve 2 levels of caching (in memory and memcache) prior to accessing the Google Cloud Datastore, and network affinity may also be activated as above. 

## Extending the image
The image produced by this project may be automatically used/extended by the Cloud SDK and/or App Engine maven plugin. 
Alternately it may be explicitly extended with a custom Dockerfile.

The latest released version of this image is available at `launcher.gcr.io/google/jetty`, alternately you may 
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
FROM launcher.gcr.io/google/jetty
COPY your-application.war $APP_DESTINATION_WAR
```

An exploded-war can also be used:
```dockerfile
COPY your-application $APP_DESTINATION_EXPLODED_WAR
```


### Adding the root application to an image
If the application exists as directory (i.e. an expanded war file), then directory must
be placed in the docker build directory and using a `Dockerfile` like: 
```dockerfile
FROM launcher.gcr.io/google/jetty
COPY your-application-dir $JETTY_BASE/webapps/root
```

### Mounting the root application at local runtime
If no root WAR or root directory is found, the `docker-entrypoint.bash` script will link the 
`/app` directory as the root application. Thus the root application can be added to the 
image via a runtime mount:
```bash
docker run -v /some-path/your-application:/app launcher.gcr.io/google/jetty  
```

### Enabling dry-run
The image's default start command will first run the jetty start.jar as a --dry-run to generate the JVM
start command before starting the jetty web server. If you wish to generate the start command in your Dockerfile
rather than at container start-time, you can run the `/scripts/jetty/generate-jetty-start.sh` script to generate it 
for you, i.e. 

```Dockerfile
RUN /scripts/jetty/generate-jetty-start.sh
```

NOTE: Make sure that the web application and any additional custom jetty modules have been added to the container
BEFORE running this script.

## Google Authentication using Jetty OpenID Module

The Jetty `openid` module adds support for the OpenID Connect authentication protocol over OAuth 2.0. This can be set up so that Jetty authenticates users with Google's Identity Platform allowing users to sign in with their Google account.

### Set Up Application on Google API Console

Before your application can use Google's OAuth 2.0 authentication system for user login, 
you must set up a project in the [Google API Console](https://console.developers.google.com/) 
to obtain OAuth 2.0 credentials (clientID and clientSecret), set a redirect URI, and (optionally) customize the 
branding information that your users see on the user-consent screen.

Guide to setting up Application in Google API Console:
https://developers.google.com/identity/protocols/oauth2/openid-connect#appsetup

### Jetty Configuration

The Jetty OpenID configuration is usually set in the `openid.ini` file. In this file we must set the values for the OpenID provider, the clientID and clientSecret. The OpenID provider should be set to `https://accounts.google.com`. The clientID and clientSecret should be obtained from the project which was set up in the Google API Console.

See the Jetty documentation for [OpenID Support](https://www.eclipse.org/jetty/documentation/current/openid-support.html) to get a general overview of how to enable OpenID authentication in your a webapp and how to access the authenticated users information.

### Docker Configuration

Special configuration should be added to the Dockerfile to enable the `openid` module and then to copy the `openid.ini` file to `$JETTY_BASE/start.d/`.

Example Dockerfile:

```Dockerfile
FROM gcr.io/google-appengine/jetty
RUN java -jar "$JETTY_HOME/start.jar" --create-startd
RUN java -jar "$JETTY_HOME/start.jar" --add-to-start=webapp,deploy,http,openid
COPY openid.ini $JETTY_BASE/start.d/
COPY openid-webapp.war $JETTY_BASE/webapps/
```

# Development Guide

* See [instructions](DEVELOPING.md) on how to build and test this image.

# Contributing changes

* See [CONTRIBUTING.md](CONTRIBUTING.md)

## Licensing

* See [LICENSE.md](LICENSE)
