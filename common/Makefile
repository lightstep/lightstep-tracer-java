.PHONY: build thrift inc-version generate-version-source-file

THRIFT_DIR=src/main/java
VERSION_SOURCE=src/main/java/com/lightstep/tracer/shared/Version.java

build:
	@echo "Nothing is built directly in this directory!"

# The version of the Android and JRE libraries is kept in lock-step as the
# majority of the code is the same.
inc-version:
	awk 'BEGIN { FS = "." }; { printf("%d.%d.%d", $$1, $$2, $$3+1) }' VERSION > VERSION.incr
	mv VERSION.incr VERSION
	make generate-version-source-file

generate-version-source-file:
	@echo "Updating $(VERSION_SOURCE)"
	@echo "// GENERATED FILE: DO NOT EDIT DIRECTLY\n\
package com.lightstep.tracer.shared;\n\
\n\
public class Version {\n\
  public static final String LIGHTSTEP_TRACER_VERSION = \"$(shell cat VERSION)\";\n\
}\n\
\n" > $(VERSION_SOURCE)

# An internal LightStep target for regenerating the thrift protocol files
thrift:
	thrift -r -gen java -out $(THRIFT_DIR) $(LIGHTSTEP_HOME)/go/src/crouton/crouton.thrift
