.PHONY: build thrift generate-version-source-file

THRIFT_DIR=$(PWD)/src/main/java
VERSION_SOURCE=src/main/java/com/lightstep/tracer/shared/Version.java

build:
	../gradlew build

clean:
	../gradlew clean

test:
	../gradlew test

doc:
	../gradlew javadocJar

generate-version-source-file:
	@echo "Updating $(VERSION_SOURCE)"
	perl -pi -e 's/(\d+)\.(\d+)\.(\d+)/ ($$1) . q{.} . ($$2) . q{.} . ($$3+1) /ge' $(VERSION_SOURCE)

# An internal LightStep target for regenerating the thrift protocol files
thrift:
    ifndef LIGHTSTEP_HOME
        $(error LIGHTSTEP_HOME environment variable must be defined)
    endif
	# thrift -r -gen java -out $(THRIFT_DIR) $(LIGHTSTEP_HOME)/go/src/crouton/crouton.thrift
	docker run -v "$(THRIFT_DIR):/out" -v "$(LIGHTSTEP_HOME)/go/src/crouton:/data" --rm thrift:0.9.2 \
		thrift -r --gen java -out /out /data/crouton.thrift
