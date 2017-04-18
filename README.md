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
ADD your-application.war $JETTY_BASE/webapps/root.war
```
      
That will add the WAR in the correct location for the Docker container.
      
Once you have this configuration, you can use the Google Cloud SDK to deploy this directory containing the 2 configuration files and the WAR using:
```
gcloud app deploy app.yaml
```

## Container Engine & other Docker hosts

For other Docker hosts, you'll need to create a Dockerfile based on this image that copies your application code and installs dependencies. For example:

```dockerfile
FROM launcher.gcr.io/google/jetty
COPY your-application.war $JETTY_BASE/webapps/root.war
```
You can then build the docker container using `docker build` or [Google Cloud Container Builder](https://cloud.google.com/container-builder/docs/).
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
|`JETTY_MODULES_ENABLED`|            |Comma separated list of modules to enable by appending to `$JETTY_ARGS` |
|`JETTY_MODULES_DISABLED`|           |Comma separated list of modules to disable by removing from `$JETTY_BASE/start.d` |
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

The configuration of the jetty container in this image can be viewed by running the image locally:
```
docker run --rm -it launcher.gcr.io/google/jetty --list-config --list-modules
```

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
COPY your-application.war $JETTY_BASE/webapps/root.war
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

# Development Guide

* See [instructions](DEVELOPING.md) on how to build and test this image.

# Contributing changes

* See [CONTRIBUTING.md](CONTRIBUTING.md)

## Licensing

* See [LICENSE.md](LICENSE)
