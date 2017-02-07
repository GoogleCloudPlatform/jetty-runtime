## Perf app

### Test webapp
The submodule jetty-load-generator-app contain a simple webapp which can be deploy to gcloud using with activating the gcloud-deploy profile `-Pgcloud-deploy`  

It's a very simple webapp with static resources and basic servlet

To deploy it for top folder:

### Load testing

`mvn clean install -Pperf -Prun-perf -DskipTests -Drunning.time.unit=s -Dwarmup.number=5 -Drunning.time=30 -Dusers=20`

To deploy new version add the profile '-Pgcloud-deploy'




