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

If you would like to simulate the cloud build locally, pass in the `--local` argument.
```
./scripts/build.sh gcr.io/$PROJECT_ID $TAG --local
```

## Running the Jetty image
The resulting image is called jetty (with more specific tags also created)
and can be run with:
```console
docker run jetty
```

## Running Tests
Integration tests can be run via [Google Cloud Container Builder](https://cloud.google.com/container-builder/docs/overview). 
These tests deploy a sample test application to App Engine and to Google Container Engine using the provided runtime image, and 
exercise various integrations with other GCP services. Note that the image under test must be pushed 
to a gcr.io repository before the integration tests can run.

```bash
RUNTIME_IMAGE=gcr.io/my-project-id/jetty:my-tag
gcloud docker -- push $RUNTIME_IMAGE
./scripts/integration_test.sh $RUNTIME_IMAGE
```

You also have the possibility to run the tests only on App Engine or only on Google Container Engine.

* For App Engine:
```bash
$ ./scripts/gae_integration_test.sh $RUNTIME_IMAGE
```

* For Google Container Engine:
```bash
$ ./scripts/gke_integration_test.sh $RUNTIME_IMAGE
```
Note that these tests are different and complementary to the integration tests in the `/tests` 
directory. See [tests/README.md](tests/README.md) for more detail.

