#!/bin/bash

# Copyright 2017 Google Inc. All rights reserved.

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at

#     http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -ex

gcloud container clusters get-credentials ${CLUSTER_NAME} --project=${GCP_PROJECT} --zone=${GCP_ZONE}

echo "Waiting for the load balancer to be accessible (expected time: ~1min)"
DEPLOYED_APP_IP=""
while [[ -z "$DEPLOYED_APP_IP" ]]; do
  sleep 5
  DEPLOYED_APP_IP=$(kubectl describe services ${GKE_TEST_APPLICATION} \
                             | awk '/LoadBalancer Ingress/ { print $3 }')
done

# The load balancer service may take some time to expose the application
# (~ 2 min on the cluster creation)
DEPLOYED_APP_URL="http://${DEPLOYED_APP_IP}:8080"
echo "Waiting for the load balancer to serve the application at $DEPLOYED_APP_URL"
until $(curl --output /dev/null --silent --head --fail "${DEPLOYED_APP_URL}"); do
  sleep 2
done

mkdir -p target
echo "$DEPLOYED_APP_URL" > "target/gke-app-ip.txt"
