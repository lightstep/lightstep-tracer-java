#
# Encapsulate the common tasks in a Makefile so one does not need to be familiar
# with gradle (or any Java-specific build tool) to get useful things done.
#
.PHONY: build clean publish test doc

build:
	./gradlew build

clean:
	./gradlew clean

test:
	./gradlew test

doc:
	./gradlew javadocJar

# NOTE: this is intended to be called by the top-level Makefile. That will
# increment the version, run all tests, etc.
publish:
	@test -n "$$BINTRAY_USER" || echo "BINTRAY_USER must be defined to publish"
	@test -n "$$BINTRAY_API_KEY" || echo "BINTRAY_API_KEY must be defined to publish"
	@echo "Publishing with bintray credentials for: $(BINTRAY_USER)"
	./gradlew bintrayUpload
