#!/bin/bash

# To publish a build, use the Makefile
# make publish
# which will call out to this script

# Use maven-help-plugin to get the current project.version
VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\['`

git commit -m "VERSION $VERSION"

# Tag the new version
git tag $VERSION

# Push the commit and tag
git push
git push --tags

echo "Publishing $VERSION"

# Build and deploy to Bintray
mvn deploy -pl .,lightstep-tracer-jre,shadow

# Sign the jar and other files in Bintray
curl -H "X-GPG-PASSPHRASE:$BINTRAY_GPG_PASSPHRASE" -u $BINTRAY_USER:$BINTRAY_API_KEY -X POST https://api.bintray.com/gpg/lightstep/maven/lightstep-tracer-jre/versions/$VERSION

# Sync the repository with Maven Central
curl -H "Content-Type: application/json" -u $BINTRAY_USER:$BINTRAY_API_KEY -X POST -d '{"username":"'$MAVEN_CENTRAL_USER_TOKEN'","password":"'$MAVEN_CENTRAL_TOKEN_PASSWORD'","close":"1"}' https://api.bintray.com/maven_central_sync/lightstep/maven/lightstep-tracer-jre/versions/$VERSION
