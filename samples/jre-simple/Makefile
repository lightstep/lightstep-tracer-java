.PHONY: run build clean

run: $(TARGET)
	gradle runSample

clean:
	gradle clean

build: $(TARGET)
$(TARGET): $(shell find src/main/java -type f -name *.java)
	gradle build
