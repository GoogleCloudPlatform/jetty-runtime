#!/bin/bash

gcloud container clusters get-credentials jetty-integration-cluster

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
