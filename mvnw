#!/bin/bash

# Maven Wrapper Script
# This script will download Maven if needed and execute it

set -e

MAVEN_VERSION=3.9.5
BASE_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}
MAVEN_HOME=.mvn/wrapper/apache-maven-${MAVEN_VERSION}

if [ ! -d "$MAVEN_HOME" ]; then
    echo "Downloading Maven ${MAVEN_VERSION}..."
    mkdir -p .mvn/wrapper
    curl -o .mvn/wrapper/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
        ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz
    tar -xzf .mvn/wrapper/apache-maven-${MAVEN_VERSION}-bin.tar.gz -C .mvn/wrapper
    rm .mvn/wrapper/apache-maven-${MAVEN_VERSION}-bin.tar.gz
fi

exec $MAVEN_HOME/bin/mvn "$@"
