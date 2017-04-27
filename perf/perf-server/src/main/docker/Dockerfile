FROM ${jetty.test.image}

ENV JETTY_BASE ${jetty.base}
ENV JETTY_HOME ${jetty.home}

ADD ${project.build.finalName}.war ${jetty.base}/webapps/root.war
RUN chown -R jetty:jetty ${jetty.base}