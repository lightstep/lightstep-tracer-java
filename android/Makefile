.PHONY: build copy-sources clean publish inc-version thrift

ANDROID_DEST_BASE=android/SampleApp/tracer/src/main/java/com/lightstep
THRIFT_DIR=SampleApp/tracer/src/main/java

build: copy-sources
ifndef ANDROID_HOME
    $(error ANDROID_HOME environment variable must be defined)
endif
	cd SampleApp/tracer && gradle assemble

copy-sources:
	cd .. && mkdir -p $(ANDROID_DEST_BASE)
	cd .. && cp -R jre/src/com/lightstep/tracer/. $(ANDROID_DEST_BASE)/tracer

clean:
	cd SampleApp/tracer && gradle clean && rm -rf .gradle

publish:
	echo "NOT IMPLEMENTED"

# An internal LightStep target for regenerating the thrift protocol files
thrift:
	rm -rf $(THRIFT_DIR)
	mkdir -p $(THRIFT_DIR)
	thrift -r -gen java -out $(THRIFT_DIR) $(LIGHTSTEP_HOME)/go/src/crouton/crouton.thrift
