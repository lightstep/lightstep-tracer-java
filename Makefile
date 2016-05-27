.PHONY: build publish pre-publish ci_test clean test

build:
	make -C lightstep-tracer-jre  build
	make -C lightstep-tracer-android build
	make -C examples/jre-simple build
	make -C examples/android-simple build
	make -C examples/android-demo build

# gradle can fail on clean, thus the "|| true"
clean:
	make -C examples/android-demo clean || true
	make -C examples/android-simple clean || true
	make -C examples/jre-simple clean || true
	make -C lightstep-tracer-android clean || true
	make -C lightstep-tracer-jre  clean || true

test:
	make -C lightstep-tracer-jre test

# The publish step does a clean and rebuild as the `gradle build` hasn't seemed
# 100% reliable in rebuilding when files are changed (?).  This may very much be
# a setup error -- but for now, a clean build is done just in case.
publish: pre-publish clean build test
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

pre-publish:
	@test -n "$$BINTRAY_USER" || echo "BINTRAY_USER must be defined to publish"
	@test -n "$$BINTRAY_API_KEY" || echo "BINTRAY_API_KEY must be defined to publish"
	@echo "\033[92mPublishing as $$BINTRAY_USER with key <HIDDEN> \033[0m"

# CircleCI test
ci_test:
	true
