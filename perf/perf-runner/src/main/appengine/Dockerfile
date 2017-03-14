FROM ${docker.openjdk.image}

ADD ${project.build.finalName}-uber.jar /perf-runner-uber.jar
ADD loadgenerator_profile.groovy loadgenerator_profile.groovy

# ,"-Xdebug","-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

CMD ["java"${debug.args},"-jar","/perf-runner-uber.jar","-DnoSysExit=true","--warmup-number","${warmup.number}","-h","${host}","--port","${port}","--running-time","${running.time}","--running-time-unit","${running.time.unit}","--transaction-rate","${transaction_rate}","--scheme","${scheme}","--users","${users}","--profile-groovy-path","/loadgenerator_profile.groovy", "-s", "${httpClientSelectors}","-mrq","${maxRequestsQueued}"]