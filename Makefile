.PHONY: build publish

build:
	make -C lightstep-tracer-jre  build
	make -C lightstep-tracer-android build

publish:
	make -C common inc-version
	make -C lightstep-tracer-jre publish
	make -C lightstep-tracer-android publish
