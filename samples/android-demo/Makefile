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
