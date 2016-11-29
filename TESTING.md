Jetty Testing Design
==

The goal of testing in the context of the vanilla runtime is to provide an extensible framework with two modes, one for local testing and validation of an application and one for remote testing which deploys applications as services into the GCE.  Tests are not a part of the normal build process, they are a separate process with both local and remote components.  Additionally tests are conceptually disconnected from the actual build of the jetty-runtime project and should be considered as something that can pulled into their own github repository with minimal changes required to the core jetty-runtime build or project.


Execution Examples:
===

Below are a series of commands and their expected behaviors.


Standard Execution
====

```
> mvn install
```


This should run the normal build cycle for the jetty-runtime project.  No tests are expected to run during this step.  The docker image being built should be installed locally minimally to the tag jetty:9.x.

```
> mvn deploy
```

This will build the relevant artifacts and additionally deploy them to minimally gcr.io/<project>/jetty:9.x


Test Executions
====

All tests are located under a /tests directory in the jetty-runtime project.  Each group of tests should exist within the scope of a single maven artifact which contains a deployable docker container (local, remote, or both) and the associated test cases which follow the conventions listed below.

> NOTE: The property ‘jetty.test.image’ will be passed in which is analogous to the FROM Docker directive.  This serves as an effective disconnect between the jetty-runtime project and the specific docker image/tag that the respective tests will be run against.

```
> cd tests;
> mvn install -Premote -Djetty.test.image=gcr.io/<project>/jetty9:9.3
```

This will activate a profile and enable remote testing. For each test artifact the appengine-maven-plugin will be used to deploy an instance of the application and the appropriate test cases will be run.  The webapps that will be built will be done through the cloud builder mechanism so the image to be tested (as linked in the jetty.test.image property) will need to be deployed to the appropriate gcr.io location.

```
> cd tests;
> mvn install -Plocal -Djetty.test.image=jetty:9.x
```

This will activate a profile and enable local testing.  The tests activated under this profile will make use of the locally installed image and tag.  The docker-maven-plugin is used to build the test docker container and the io.fabric8 plugin is used to manage the integration test lifecycle..  Local tests have a much smaller scope as they are not able to make use of many of the features of the jetty-runtime project.


Building Test Cases
===

Test cases are logically combined into a single deployable that may or may not be appropriate for remote and local testing.  Where possible the test source should minimize code duplication and convenient utility classes should be located in the ‘gcloud-testing-core’ artifact.  Additionally, the overarching goal is to test live integrations with Google services locally to validate proper container behaviors within the scope of the docker container itself.


The test-war-hello module is a simple example for how local and remote testings can be laid out.

Requirements:
====

* local docker installation
* gcloud installation
* *beta* component required
* remote tests require proper gcloud authentication configured
  * _list required permissions_

Conventions:
====

* testing is disabled by default, activated via -Plocal or -Premote
* local and remote testing should be mutually exclusive
* local integration tests end in ‘LocalITCase’
* remote integration tests end in ‘RemoteITCase’

Properties:
====

* Both: jetty.test.image is the name in the FROM line in the Dockerfile
* Local: app.deploy.port is the localhost port used for http
* Remote: app.deploy.project is the configured gcloud project id
* Remote: app.deploy.version is the version used on deploy

Local Test Process:
====
* -Plocal enables failsafe-maven-plugin processing of *LocalITCase* tests
* com.spotify:docker-maven-plugin builds target container based on value of *jetty.test.image*
* io.fabric8:docker-maven-plugin starts the target container in pre-integration-test phase
  * random local port mapped to 8080 of container and available to test case as system property *app.deploy.port*
* failsafe-maven-plugin runs in integration-test phase
* io.fabric8:docker-maven-plugin stops the target container in  post-integration-test phase

Remote Test Process:
====

* -Premote enables failsafe-maven-plugin process of *RemoteITCase* tests
* maven-antrun-plugin runs to find the gcloud project id and place in properties file
* properties-maven-plugin runs to load properties file
* appengine-maven-plugin runs to build and deploy target application
* failsafe-maven-plugin runs the integration tests
* maven-antrun-plugin used to delete the test version of the application from remote service


