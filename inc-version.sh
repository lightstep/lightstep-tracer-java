#!/bin/bash

# To increment the version, use the Makefile
# make publish
# which will call out to this script

CURRENT_BRANCH=`git rev-parse --abbrev-ref HEAD`

CURRENT_VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\['`

# Increment the minor version
NEW_VERSION="${CURRENT_VERSION%.*}.$((${CURRENT_VERSION##*.}+1))"
export NEW_VERSION

# Use maven-help-plugin to update the project.version
mvn versions:set -DnewVersion=$NEW_VERSION -DgenerateBackupPoms=false

# Update the Version.java class
VERSION_SOURCE=lightstep-tracer-jre/src/main/java/com/lightstep/tracer/jre/Version.java
perl -pi -e 's/(\d+)\.(\d+)\.(\d+)/ $ENV{NEW_VERSION} /ge' ${VERSION_SOURCE}

# Commit the changes
git add benchmark/pom.xml
git add examples/pom.xml
git add lightstep-tracer-jre/pom.xml
git add lightstep-tracer-jre-bundle/pom.xml
git add pom.xml
git add shadow/pom.xml
git add lightstep-tracer-jre/src/main/java/com/lightstep/tracer/jre/Version.java

git commit -m "VERSION $NEW_VERSION"
git push --set-upstream origin $CURRENT_BRANCH
