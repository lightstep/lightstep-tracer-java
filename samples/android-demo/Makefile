.PHONY: build clean

build:
ifndef ANDROID_HOME
    $(error ANDROID_HOME environment variable must be defined)
endif
	./gradlew assemble

clean:
	./gradlew clean
