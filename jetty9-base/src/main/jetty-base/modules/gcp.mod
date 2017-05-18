#
# GCP Module for Jetty 9 Google Cloud Platform Image
#
# Enabling this module will:
#  * apply the $JETTY_BASE/etc/gcp-web.xml file to each deployed webapplication
#  * add an com.google.cloud.runtimes.jetty9.DeploymentCheck instance to the server 
#    that checks for a deployed web application and listening connector.
#
# The $JETTY_BASE/etc/gcp-web.xml when applied to a webapplication will:
#  * set throwUnavailableOnStartupException to true
#  * add /app.yaml to the protected targets
#  * set $JETTY_BASE/etc/gcp-override-web.xml as the override descriptor.
#  
# The $JETTY_BASE/etc/gcp-override-web.xml override descriptor will:
#  * set the dirAllowed init-param to false
#

[depend]
server
http-forwarded

[optional]
resources
deploy

[xml]
etc/gcp.xml

[lib]
lib/gcp/*.jar

[ini]
# Configure secure port redirection to 443 if not explicitly set
jetty.httpConfig.securePort?=443

# Disable hot deploy of webapps if not explicitly set
jetty.deploy.scanInterval?=0

# Disable support for RFC7239 header
jetty.httpConfig.forwardedHeader=

# Send Server version
jetty.httpConfig.sendServerVersion?=true

# Don't send date header
jetty.httpConfig.sendDateHeader?=false

# Hide the gcloud libraries from deployed webapps
jetty.webapp.addServerClasses+=,${jetty.base.uri}/lib/gcp/
