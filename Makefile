.PHONY: build publish ci_test clean test

build:
	make -C lightstep-tracer-jre  build
	make -C lightstep-tracer-android build
	make -C examples/jre-simple build
	make -C examples/android-simple build
	make -C examples/android-demo build

clean:
	make -C examples/android-demo clean
	make -C examples/android-simple clean
	make -C examples/jre-simple clean
	make -C lightstep-tracer-android clean
	make -C lightstep-tracer-jre  clean

test:
	make -C lightstep-tracer-jre test

# The publish step does a clean and rebuild as the `gradle build` hasn't seemed
# 100% reliable in rebuilding when files are changed (?).  This may very much be
# a setup error -- but for now, a clean build is done just in case.
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
	@echo
	@echo "\033[92mSUCCESS: published v`cat common/VERSION` \033[0m"
	@echo


# CircleCI test
ci_test:
	true
