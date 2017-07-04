.PHONY: build publish ci_test clean test inc-version

build: test
	mvn package

clean:
	mvn clean

test: ci_test

# CircleCI test
ci_test: clean
	mvn test
	mvn exec:java -pl examples

# See https://bintray.com/lightstep for published artifacts
# You must have the following entry in your settings.xml of your .m2 directory
# This matches the distributionManagement/repository defined in the pom.xml
#
#    <server>
#        <id>lightstep-bintray</id>
#        <username>xxx</username>
#        <password>xxx</password>
#    </server>
#
publish: build
	@git diff-index --quiet HEAD || (echo "git has uncommitted changes. Refusing to publish." && false)
	./inc-version.sh
	mvn deploy -pl lightstep-tracer-jre
