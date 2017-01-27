FROM jetty9-compat

ENV GAE_LONG_APP_ID=11111
ENV GAE_PARTITION=dev
ENV GAE_MODULE_INSTANCE=beer
ENV GAE_MODULE_NAME=test
ENV GAE_MODULE_VERSION=1.0.0

RUN rm -rf $JETTY_BASE/webapps/root/*

ADD jetty-load-generator-app-base-webapp.war $JETTY_BASE/webapps/root/root.war

RUN cd $JETTY_BASE/webapps/root/ && unzip $JETTY_BASE/webapps/root/root.war

