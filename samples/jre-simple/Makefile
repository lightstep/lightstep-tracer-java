.PHONY: run clean

# TODO: fix this JAR dependency tangle
DEP=../../jre/dependencies
CLASSPATH=.:../../common/build/libs/common.jar:../../jre/build/libs/jre.jar:$(DEP)/libthrift-0.9.2.jar:$(DEP)/jackson-core-2.6.0.jar:$(DEP)/jackson-databind-2.6.0.jar:$(DEP)/jackson-annotations-2.6.0.jar

run: Simple.class
	java -cp $(CLASSPATH) Simple

clean:
	rm *.class

Simple.class: Simple.java
	javac -cp $(CLASSPATH) Simple.java
