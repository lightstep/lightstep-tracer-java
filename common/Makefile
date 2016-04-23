.PHONY: build clean thrift

THRIFT_DIR=src/main/java

build:
	gradle jar

clean:
	gradle clean
	rm -rf build

# An internal LightStep target for regenerating the thrift protocol files
thrift:
	thrift -r -gen java -out $(THRIFT_DIR) $(LIGHTSTEP_HOME)/go/src/crouton/crouton.thrift
