## FROM gcr.io/google_appengine/jetty9
#FROM jetty:${docker.tag.short}
#FROM jetty
FROM ${jetty.test.image}

ENV JETTY_BASE ${jetty.base}
ENV JETTY_HOME ${jetty.home}

# we do not install a new jetty distribution
#RUN rm -r /usr/local/jetty
#ADD jetty-home.tar.gz /usr/local/
#RUN mv /usr/local/jetty-home-* $JETTY_HOME \
#&& chown -R jetty:jetty $JETTY_HOME

ADD ${project.build.finalName}.war ${jetty.base}/webapps/root.war
RUN chown -R jetty:jetty ${jetty.base}

#RUN java  -Djetty.base=${jetty.base} -jar ${jetty.home}/start.jar --approve-all-licenses --add-to-start=stats,servlet

#CMD ["java", "-XX:+PrintGCDetails", "-XX:+PrintGCTimeStamps","-Djetty.base=${jetty.base}","-jar","${jetty.home}/start.jar"]
