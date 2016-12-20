Jetty Testing Design
==

The goal of testing in the context of the vanilla runtime is to provide an extensible framework with two modes, one for local testing and validation of an application and one for remote testing which deploys applications as services into the GCE.  Tests are not a part of the normal build process, they are a separate process with both local and remote components.  Additionally tests are effectively disconnected from the actual build of the jetty-runtime project and should be considered as something that can be pulled into their own github repository with minimal changes required to the core jetty-runtime build or project.


Execution Examples:
===

Below are a series of commands and their expected behaviors.


Standard Execution
====

At the top level of the project.

```
> mvn install
```

This will run a normal build cycle for the jetty-runtime project.  No tests are expected to run during this step.  The docker image being built will be available to use in later local testing steps.  To see what tags are available use the following docker command:

```
> docker images
REPOSITORY                        TAG                        IMAGE ID            CREATED             SIZE
jetty                             9.4                        b6cbab53c076        47 hours ago        359.2 MB
jetty                             9.4-2016-12-13_17_02       b6cbab53c076        47 hours ago        359.2 MB
jetty                             latest                     b6cbab53c076        47 hours ago        359.2 MB
```

In order to make these images available for remote testing you can use the following commands:

```
> docker tag jetty:9.4 gcr.io/{project}/jetty9:9.4
> gcloud docker push gcr.io/{project}/jetty9:9.4 
```

This will take the local artifacts and make them available for remote testing (or general usage for the given {project}).


Test Executions
====

All tests are located under a /tests directory in the jetty-runtime project.  Each group of tests should exist within the scope of a single maven artifact which contains a deployable docker container (local, remote, or both) and the associated test cases which follow the conventions listed further below.

> NOTE: The property ‘jetty.test.image’ being passed is analogous to the FROM Docker directive.  This serves as an effective disconnect between the jetty-runtime project and the specific docker image/tag that the respective tests will be run against.  It is absolutely possible to run tests under the /tests directory against an arbitrary image.  It is up to the user to ensure they are testing from a valid container image.

Local Testing
=====

From the jetty-runtime/tests directory:

```
> mvn install -Plocal -Djetty.test.image=jetty:9.4
```

This will activate the 'local' profile and enable testing.  The tests activated under this profile will make use of the locally installed image and tag referenced in the jetty.test.image property.  The spotify docker-maven-plugin is used to build the test docker container and the io.fabric8 docker plugin is used to manage the integration test lifecycle. Local tests may have a smaller scope as they are not intended to the complete Google Flex environment.  Local testing is intended to test and validate configuration of Jetty and basic environment. 

Remote Testing
=====

Again from the jetty-runtime/tests directory:

```
> mvn install -Premote -Djetty.test.image=gcr.io/{project}/jetty9:9.4
```

This will activate the remote profile and enable testing. Under this scenario, for each test artifact the appengine-maven-plugin is used to deploy an instance of the application to the Google Flex environment and then run appropriate test cases.  The containers for each webapp will be built through using the cloud builder mechanism.  This means the image to be tested (as referenced in the jetty.test.image property) will need to be deployed to the appropriate gcr.io location.  Remote testing can make use of the entire scope of services available to Google Flex.  



Test Case Requirements and Conventions
===

Both local and remote test cases are logically combined into a single deployable container that may or may not be appropriate for remote and local testing.  Where possible the test source should minimize code duplication and convenient utility classes should be located in the ‘gcloud-testing-core’ artifact.

Annotations are used to indicate if a test method is appropriate to run for the mode selected. Both annotations may be used on the same method but the test will only be run in the startup mode selected.

* @Local
* @Remote

The test-war-smoke module is a simple example for how local and remote testings can be laid out.

Requirements:
====

* local docker installation
* local gcloud installation configured with authenticated user and project


Conventions:
====

* testing is disabled by default, activated via -Plocal or -Premote
* local and remote testing are mutually exclusive
* a custom ContainerRunner junit test runner is used to find tests to run
* test classes should extend the AbstractIntegrationTest from gcloud-testing-core
* local integration tests have the @Local annotation
* remote integration tests have the @Remote annotation
* the junit @Ignore annotation is respected

Properties:
====

* Both: jetty.test.image translates to the name in the FROM line of the Dockerfile
* Local: app.deploy.port is the localhost port used for http
* Remote: app.deploy.project is the configured gcloud project id
* Remote: app.deploy.version is the version used on deploy

Local Test Process:
====
* -Plocal enables failsafe-maven-plugin processing of @Local annotations
* com.spotify:docker-maven-plugin builds target container based on value of *jetty.test.image*
* io.fabric8:docker-maven-plugin starts the target container in pre-integration-test phase
  * random local port mapped to 8080 of container and available to test case as system property *app.deploy.port*
* failsafe-maven-plugin runs in integration-test phase
* io.fabric8:docker-maven-plugin stops the target container in  post-integration-test phase

Remote Test Process:
====

* -Premote enables failsafe-maven-plugin processing of @Remote annotations
* maven-antrun-plugin runs to find the gcloud project id and place in properties file
* properties-maven-plugin runs to load properties file
* appengine-maven-plugin runs to build and deploy target application
* failsafe-maven-plugin runs the integration tests
* maven-antrun-plugin used to delete the test version of the application from remote service


Developing Tests
===

Writing test cases is fairly straight forward.  The server portion of a test case is created as a standard webapp and the actual test portion is typically comprised of querying the running server and validating output.  Simple environment tests can be handled entirely within the response from a servlet or filter.  More complex tests like validating logging configuration or session management features may require additional dependencies and more complex configuration and teardown.

To interact with a local test under development the following docker command may be useful:

```
> cd tests/test-war-smoke
> mvn install
> docker run --rm -it -p 8088:8080 test-war-smoke:latest
```

This will deploy the test-war-smoke container and you will be able to query the service with your browser by nagivating to (http://localhost:8088/).