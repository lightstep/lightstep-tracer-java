.PHONY: build publish ci_test clean

build:
	make -C lightstep-tracer-jre  build
	make -C lightstep-tracer-android build

clean:
	make -C lightstep-tracer-jre  clean
	make -C lightstep-tracer-android clean

publish: clean build
	@git diff-index --quiet HEAD || (echo "git has uncommitted changes. Refusing to publish." && false)
	make -C common inc-version
	git add common/VERSION
	git commit -m "Update VERSION"
	git tag `cat common/VERSION`
	git push
	git push --tags
	make -C lightstep-tracer-jre publish
	make -C lightstep-tracer-android publish

# CircleCI test
ci_test:
	true
