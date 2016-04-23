.PHONY: build

build:
	make -C common build
	make -C android build
	make -C jre build
