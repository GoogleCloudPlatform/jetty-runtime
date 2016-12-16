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

# Create env vars to identify image
ENV JETTY_VERSION ${jetty9.version}
ENV GAE_IMAGE_NAME jetty
ENV GAE_IMAGE_LABEL ${docker.tag.long}

# Create Jetty Home
ENV JETTY_HOME ${jetty.home}
ENV PATH $JETTY_HOME/bin:$PATH
ENV TMPDIR /tmp/jetty
RUN mkdir $TMPDIR
ADD jetty-distribution.tar.gz $JETTY_HOME/..

# Create Jetty Base
ENV JETTY_BASE ${jetty.base}
ADD jetty-base $JETTY_BASE
WORKDIR $JETTY_BASE

# Start Jetty directly
COPY docker-entrypoint.bash /
RUN chmod +rx /docker-entrypoint.bash
ENTRYPOINT ["/docker-entrypoint.bash"]

EXPOSE 8080

CMD ["java","-Djetty.base=${jetty.base}","-jar","${jetty.home}/start.jar"]
