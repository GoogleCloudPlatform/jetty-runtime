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

# initiate the cloud build, passing it the docker namespace for the resulting image
PROJECT_ID=my-project
./scripts/cloudbuild.sh gcr.io/$PROJECT_ID
```

If you would like to simulate the cloud build locally, pass in the `--local` argument.
```
./scripts/cloudbuild.sh gcr.io/$PROJECT_ID --local
```

## Running the Jetty image
The resulting image is called jetty (with more specific tags also created)
and can be run with:
```console
docker run jetty
```