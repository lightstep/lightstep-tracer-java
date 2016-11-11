#
# Encapsulate the common tasks in a Makefile so one does not need to be familiar
# with gradle (or any Java-specific build tool) to get useful things done.
#
.PHONY: build clean publish test doc

build:
	../gradlew build

clean:
	../gradlew clean

test:
	../gradlew test

doc:
	../gradlew javadocJar
