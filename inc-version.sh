#!/bin/bash


# To increment the version and publish a build, use the Makefile
# make publish
# which will call out to this script


# Use maven-help-plugin to get the current project.version
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
git add pom.xml
git add shadow/pom.xml
git add lightstep-tracer-jre/src/main/java/com/lightstep/tracer/jre/Version.java
git commit -m "VERSION $NEW_VERSION"

# Tag the new version
git tag $NEW_VERSION

# Push the commit and tag
git push
git push --tags

echo "Updated from $CURRENT_VERSION to $NEW_VERSION"

# Build and deploy to Bintray
mvn deploy -pl ligthstep-tracer-java,lightstep-tracer-jre

# Sign the jar and other files in Bintray
curl -H "X-GPG-PASSPHRASE:$BINTRAY_GPG_PASSPHRASE" -u $BINTRAY_USER:$BINTRAY_API_KEY -X POST https://api.bintray.com/gpg/lightstep/maven/lightstep-tracer-java/versions/$NEW_VERSION
curl -H "X-GPG-PASSPHRASE:$BINTRAY_GPG_PASSPHRASE" -u $BINTRAY_USER:$BINTRAY_API_KEY -X POST https://api.bintray.com/gpg/lightstep/maven/lightstep-tracer-jre/versions/$NEW_VERSION

# Sync the repository with Maven Central
curl -H "Content-Type: application/json" -u $BINTRAY_USER:$BINTRAY_API_KEY -X POST -d '{"username":"'$MAVEN_CENTRAL_USER_TOKEN'","password":"'$MAVEN_CENTRAL_TOKEN_PASSWORD'","close":"1"}' https://api.bintray.com/maven_central_sync/lightstep/maven/lightstep-tracer-java/versions/$NEW_VERSION
curl -H "Content-Type: application/json" -u $BINTRAY_USER:$BINTRAY_API_KEY -X POST -d '{"username":"'$MAVEN_CENTRAL_USER_TOKEN'","password":"'$MAVEN_CENTRAL_TOKEN_PASSWORD'","close":"1"}' https://api.bintray.com/maven_central_sync/lightstep/maven/lightstep-tracer-jre/versions/$NEW_VERSION
