FROM ${docker.openjdk.image}

ADD ${project.build.finalName}-uber.jar /perf-runner-uber.jar
ADD loadgenerator_profile.groovy loadgenerator_profile.groovy

CMD ["java","-jar","/perf-runner-uber.jar","-Dwarmup.number=${warmup.number}","-h","${host}","--port","${port}","--running-time","${running.time}","--running-time-unit","${running.time.unit}","--transaction-rate","${transaction_rate}","--transport","${transport}","--users","${users}","--profile-groovy-path","/loadgenerator_profile.groovy"]