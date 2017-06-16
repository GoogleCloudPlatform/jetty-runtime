#!/bin/bash

DEPLOYED_APP_URL=$(cat target/gke-app-ip.txt)

/testsuite/driver.py --url=${DEPLOYED_APP_URL} $@