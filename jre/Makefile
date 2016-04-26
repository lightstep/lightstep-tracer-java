#
# Encapsulate the common tasks in a Makefile so one does not need to be familiar
# with gradle (or any Java-specific build tool) to get useful things done.
#
.PHONY: build clean

build:
	gradle build

clean:
	gradle clean

publish:
ifndef BINTRAY_USER
    $(error Environment varialbe BINTRAY_USER is undefined. This must be set in order to publish the artifact.)
endif
ifndef BINTRAY_KEY
    $(error Environment varialbe BINTRAY_KEY is undefined. This must be set in order to publish the artifact.)
endif
	@echo "Publishing with bintray credentials for: $(BINTRAY_USER)"
	gradle bintray
