.PHONY: build copy-sources clean publish inc-version thrift

ANDROID_DEST_BASE=android/SampleApp/tracer/src/main/java/com/lightstep
THRIFT_DIR=SampleApp/tracer/src/main/java

build:
ifndef ANDROID_HOME
    $(error ANDROID_HOME environment variable must be defined)
endif
	cd SampleApp/tracer && gradle assemble

clean:
	cd SampleApp/tracer && gradle clean && rm -rf .gradle

inc-version:
	awk 'BEGIN { FS = "." }; { printf("%d.%d.%d", $$1, $$2, $$3+1) }' SampleApp/tracer/VERSION > SampleApp/tracer/VERSION.incr
	mv SampleApp/tracer/VERSION.incr SampleApp/tracer/VERSION

publish:
	echo "NOT IMPLEMENTED"

# An internal LightStep target for regenerating the thrift protocol files
thrift:
	thrift -r -gen java -out $(THRIFT_DIR) $(LIGHTSTEP_HOME)/go/src/crouton/crouton.thrift
