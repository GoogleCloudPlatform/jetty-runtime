## Docker - gcloud

Build the docker image using `mvn clean install -pl :jetty-load-generator-app-gcloud -am -Pdocker`

Run the image using `docker run -it -p 8080:8080  webtide/jetty-load-generator-app-gcloud:0.0.1-SNAPSHOT`

Build and deploy to gcloud: `mvn clean deploy -pl :jetty-load-generator-app-gcloud -am -Pgcloud`

Build: `mvn clean package -pl :jetty-load-generator-app-embedded -am -DskipTests -Djetty9.version=9.4.0.v20161208`

Note: it works only for 9.4+ versions





