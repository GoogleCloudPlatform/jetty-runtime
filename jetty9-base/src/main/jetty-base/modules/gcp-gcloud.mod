[provides]
gcloud

[lib]
lib/gcloud/*.jar

[ini]
# Hide the gcloud libraries from deployed webapps
jetty.webapp.addServerClasses+=,${jetty.base.uri}/lib/gcloud/
