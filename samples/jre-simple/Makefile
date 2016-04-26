.PHONY: run build clean

CLASSPATH_LIST=$(shell find ../../jre/dependencies -type f -name *.jar) \
	../../jre/build/libs/lightstep-tracer-jre-0.1.0.jar \
	build/libs/jre-simple.jar
EMPTY :=
SPACE := $(EMPTY) $(EMPTY)
CLASSPATH=$(subst $(SPACE),:,$(CLASSPATH_LIST))

run: Simple.class
	java -cp $(CLASSPATH) com.lightstep.tracer.samples.jre.Simple

clean:
	gradle clean

build: Simple.class
Simple.class: $(shell find src/main/java -type f -name *.java)
	gradle build
