#
# GAE Module for Jetty 9 Google Cloud Platform Image
#
# Enabling this module will:
#  * apply the $JETTY_BASE/etc/gae-web.xml file to each deployed webapplication
#  * add an com.google.cloud.runtimes.jetty9.DeploymentCheck instance to the server 
#    that checks for a deployed web application and listening connector.
#
# The $JETTY_BASE/etc/gae-web.xml when applied to a webapplication will:
#  * set throwUnavailableOnStartupException to true
#  * add /app.yaml to the protected targets
#  * set $JETTY_BASE/etc/gae-override-web.xml as the override descriptor.
#  
# The $JETTY_BASE/etc/gae-override-web.xml override descriptor will:
#  * set the dirAllowed init-param to false
#

[depend]
server

[optional]
resources
deploy

[xml]
etc/gae.xml

[lib]
lib/gae/*.jar
