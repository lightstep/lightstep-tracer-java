.PHONY: build

build:
	make -C android build
	make -C jre build
