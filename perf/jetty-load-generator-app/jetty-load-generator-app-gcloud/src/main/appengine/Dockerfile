FROM gcr.io/google_appengine/jetty9

RUN rm -r /usr/local/jetty
ADD jetty-distribution.tar.gz /usr/local/
RUN mv /usr/local/jetty-distribution-* $JETTY_HOME \
 && chown -R jetty:jetty $JETTY_HOME

ADD jetty-load-generator-app-base-webapp.war $JETTY_BASE/webapps/root.war

RUN java  -Djetty.base=/var/lib/jetty -jar /usr/local/jetty/start.jar --approve-all-licenses --add-to-start=stats,servlet

CMD ["java", "-XX:+PrintGCDetails", "-XX:+PrintGCTimeStamps","-Djetty.base=/var/lib/jetty","-jar","/usr/local/jetty/start.jar","--module=stats,servlet"]
