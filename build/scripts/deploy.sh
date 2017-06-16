#!/bin/bash

gcloud container clusters get-credentials jetty-integration-cluster

kubectl run ${GKE_TEST_APPLICATION} --image=${STAGING_IMAGE} --port=8080 --expose=true \
            --service-overrides='{ "apiVersion": "v1", "spec": { "type":  "LoadBalancer" } }' \
            --requests 'cpu=50m,memory=128Mi'