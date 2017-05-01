FROM ${docker.openjdk.image}

ADD ${project.build.finalName}-uber.jar /perf-runner-uber.jar
ADD loadgenerator_profile.groovy loadgenerator_profile.groovy

EXPOSE ${jettyPort}

# ,"-Xdebug","-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
# "-Dorg.mortbay.jetty.load.generator.LEVEL=DEBUG","-Dorg.mortbay.jetty.load.generator.starter.LEVEL=DEBUG",
# ,"-Dorg.mortbay.jetty.load.generator.listeners.report.LEVEL=DEBUG"

CMD ["java"${debug.args},"-jar","/perf-runner-uber.jar","--threads","${threads}","-DbigQuery=true","-DjettyRun=true","-DjettyPort=${jettyPort}","-DnoSysExit=true","--warmup-number","${warmup.number}","-h","${host}","--port","${port}","--running-time","${running.time}","--running-time-unit","${running.time.unit}","--transaction-rate","${transaction_rate}","--scheme","${scheme}","--users","${users}","--profile-groovy-path","/loadgenerator_profile.groovy", "-s", "${httpClientSelectors}","-mrq","${maxRequestsQueued}"]