# Copyright 2014 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM ${docker.openjdk.image}

# create jetty group and user
RUN groupadd -r jetty \
 && useradd -r -g jetty jetty

# Create env vars to identify image
ENV JETTY_VERSION ${jetty9.version}
ENV GAE_IMAGE_NAME jetty
ENV GAE_IMAGE_LABEL ${docker.tag.long}

# Create Jetty Home
ENV JETTY_HOME ${jetty.home}
ENV PATH $JETTY_HOME/bin:$PATH
ADD jetty-home $JETTY_HOME
RUN chown -R jetty:jetty $JETTY_HOME

# Create Jetty Base
ENV JETTY_BASE ${jetty.base}
ENV TMPDIR /tmp/jetty
ADD jetty-base $JETTY_BASE
ADD 15-debug-env-jetty.bash 50-jetty.bash 60-jetty-dry-run.bash /setup-env.d/
ADD quickstart.sh /scripts/jetty/quickstart.sh
ADD generate-jetty-start.sh /scripts/jetty/generate-jetty-start.sh
WORKDIR $JETTY_BASE
RUN mkdir -p webapps $TMPDIR \
 && java -jar $JETTY_HOME/start.jar --add-to-start=setuid \
 && chown -R jetty:jetty $JETTY_BASE $TMPDIR \
 && chmod +x /setup-env.d/15-debug-env-jetty.bash \
 && chmod +x /setup-env.d/50-jetty.bash \
 && chmod +x /setup-env.d/60-jetty-dry-run.bash \
 && chmod +x /scripts/jetty/quickstart.sh \
 && chmod +x /scripts/jetty/generate-jetty-start.sh

# Set path where apps should be added to the container
ENV APP_WAR root.war
ENV APP_EXPLODED_WAR root/
ENV APP_DESTINATION_PATH $JETTY_BASE/webapps/
ENV APP_DESTINATION_WAR $APP_DESTINATION_PATH$APP_WAR
ENV APP_DESTINATION_EXPLODED_WAR $APP_DESTINATION_PATH$APP_EXPLODED_WAR

# This env var is here to not break users of previous versions where only a .war was expected
ENV APP_DESTINATION $APP_DESTINATION_WAR

EXPOSE 8080
CMD ["java","-Djetty.base=${jetty.base}","-jar","${jetty.home}/start.jar"]
