# Development Guide

This document contains instructions on how to build and test this image.

## Building the Jetty image

### Local build
To build the image you need git, docker and maven installed:
```bash
git clone https://github.com/GoogleCloudPlatform/jetty-runtime.git
cd jetty-runtime
mvn clean install
```

### Cloud build
To build using the [Google Cloud Container Builder](https://cloud.google.com/container-builder/docs/overview), you need to have git, maven, and the [Google Cloud SDK](https://cloud.google.com/sdk/) installed locally.
```bash
git clone https://github.com/GoogleCloudPlatform/jetty-runtime.git
cd jetty-runtime

# initiate the cloud build, passing in the docker namespace and tag for the resulting image
PROJECT_ID=my-project
TAG=my-tag                        # optional
./scripts/build.sh -d gcr.io/$PROJECT_ID -t $TAG
```

The configured Cloud Build execution will build the Jetty docker container, then create and teardown various GCP resources for 
integration testing. Before running, make sure you have done the following:
 * enabled the Cloud Container Builder API
 * initialized App Engine for your GCP project (run `gcloud app create`), and successfully deployed to it at least once
 * provided the Container Builder Service account (cloudbuild.gserviceaccount.com) with the appropriate permissions needed to deploy App Engine applications. This includes at least "App Engine Admin" and "Cloud Container Builder", but simply adding the "Project Editor" role works fine as well.

## Running the Jetty image
The resulting image is called jetty (with more specific tags also created)
and can be run with:
```bash
docker run jetty
```

## Running Tests
Integration tests are run as part of the [Google Cloud Container Builder](https://cloud.google.com/container-builder/docs/overview) 
execution that is run in the `build.sh` script.

