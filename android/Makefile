.PHONY: build copy-sources clean publish inc-version

ANDROID_DEST_BASE=android/SampleApp/tracer/src/main/java/com/lightstep

build: copy-sources
	cd SampleApp/tracer && gradle assemble

copy-sources:
	cd .. && mkdir -p $(ANDROID_DEST_BASE)
	cd .. && cp -R jre/src/com/lightstep/tracer/. $(ANDROID_DEST_BASE)/tracer

clean:
	cd SampleApp/tracer && gradle clean && rm -rf .gradle

publish:
	echo "NOT IMPLEMENTED"
