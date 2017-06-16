#!/bin/bash

TEST_CLUSTER_EXISTENCE=$(gcloud container clusters list | awk "/$CLUSTER_NAME/")
if [ -z "$TEST_CLUSTER_EXISTENCE" ]; then
  gcloud container clusters create "$CLUSTER_NAME" --num-nodes=1 --disk-size=10
fi