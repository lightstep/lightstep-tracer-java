.PHONY: build thrift

build:
	@echo "Nothing is built directly in this directory!"

# The version of the Android and JRE libraries is kept in lock-step as the
# majority of the code is the same.
inc-version:
	awk 'BEGIN { FS = "." }; { printf("%d.%d.%d", $$1, $$2, $$3+1) }' VERSION > VERSION.incr
	mv VERSION.incr VERSION

# An internal LightStep target for regenerating the thrift protocol files
thrift:
	thrift -r -gen java -out $(THRIFT_DIR) $(LIGHTSTEP_HOME)/go/src/crouton/crouton.thrift
