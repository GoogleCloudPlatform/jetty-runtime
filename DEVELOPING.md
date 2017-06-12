# Development Guide

This document contains instructions on how to build and test this image.

## Building the Jetty image

### Local build
To build the image you need git, docker and maven installed:
```console
git clone https://github.com/GoogleCloudPlatform/jetty-runtime.git
cd jetty-runtime
mvn clean install
```

### Cloud build
To build using the [Google Cloud Container Builder](https://cloud.google.com/container-builder/docs/overview), you need to have git, maven, and the [Google Cloud SDK](https://cloud.google.com/sdk/) installed locally.
```console
git clone https://github.com/GoogleCloudPlatform/jetty-runtime.git
cd jetty-runtime

# initiate the cloud build, passing in the docker namespace and tag for the resulting image
PROJECT_ID=my-project
TAG=my-tag
./scripts/build.sh gcr.io/$PROJECT_ID $TAG
```

## Running the Jetty image
The resulting image is called jetty (with more specific tags also created)
and can be run with:
```console
docker run jetty
```

## Running Tests
Integration tests are run as part of the [Google Cloud Container Builder](https://cloud.google.com/container-builder/docs/overview) 
execution that is run in the `build.sh` script.

