#!/bin/bash

export CODEARTIFACT_AUTH_TOKEN=`aws codeartifact get-authorization-token --domain tango --domain-owner 802306197541 --query authorizationToken --output text` && \
./mvnw -B clean package 
