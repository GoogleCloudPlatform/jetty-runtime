FROM ${jetty.test.image}
ADD ${project.build.finalName}.war $JETTY_BASE/webapps/root.war

# Uncomment to dump the jetty configuration at startup
# WORKDIR $JETTY_BASE
# RUN java -jar $JETTY_HOME/start.jar --update-ini jetty.server.dumpAfterStart=true
